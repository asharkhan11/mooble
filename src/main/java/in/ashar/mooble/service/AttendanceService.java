package in.ashar.mooble.service;


import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.enums.SessionStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepo;
    private final AttendanceEntryRepository entryRepo;
    private final SessionRepository sessionRepo;
    private final Student2Repository studentRepo;
    private final GetCurrentUser currentUser;

    /* ---------------- OPEN ---------------- */


    @Transactional
    public AttendanceResponse openAttendance(AttendanceCreateRequest request) {

        Session session = sessionRepo.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalStateException("Session not found"));

        Teacher2 teacher = currentUser.getCurrentTeacher();

        if(session.getTeacher().getTeacherId() != teacher.getTeacherId()){
            throw new UnAuthorizedException("Session does not belongs to you");
        }

        Optional<Attendance> optAttendance = attendanceRepo.findBySessionId(session.getId());

        if(optAttendance.isPresent()){
            Attendance attendance = optAttendance.get();
            List<AttendanceEntry> entries = entryRepo.findByAttendanceAttendanceId(attendance.getAttendanceId());
            return map(attendance,entries);
        }


        Attendance attendance = Attendance.builder()
                .session(session)
                .markedBy(teacher)
                .markedAt(LocalDateTime.now())
                .status(Attendance.AttendanceStatus.DRAFT)
                .build();

        attendanceRepo.save(attendance);

        // preload students (default ABSENT)
        List<Student2> students;

        if (session.getSubject() != null) {
            students = studentRepo.findBySubjectId(
                    session.getSubject().getSubjectId()
            );
        } else {
            students = studentRepo.findByCourseId(
                    session.getCourse().getCourseId()
            );
        }

        List<AttendanceEntry> entries = students.stream()
                .map(s -> AttendanceEntry.builder()
                        .attendance(attendance)
                        .student(s)
                        .mark(AttendanceEntry.AttendanceMark.ABSENT)
                        .build())
                .toList();

        entryRepo.saveAll(entries);

        return map(attendance, entries);
    }

    /* ---------------- UPDATE ---------------- */


    @Transactional
    public AttendanceResponse updateAttendance(
            int attendanceId,
            AttendanceUpdateRequest request
    ) {

        Attendance attendance = getEditableAttendance(attendanceId);

        Teacher2 teacher = currentUser.getCurrentTeacher();

        if(attendance.getMarkedBy().getTeacherId() != teacher.getTeacherId()){
            throw new UnAuthorizedException("You are not authorized to update/mark Attendance");
        }

        Map<Integer, AttendanceEntry> entryMap =
                entryRepo.findByAttendanceAttendanceId(attendanceId)
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e.getStudent().getStudentId(),
                                e -> e
                        ));

        for (AttendanceEntryRequest r : request.getEntries()) {
            AttendanceEntry entry = entryMap.get(r.getStudentId());
            if (entry != null) {
                entry.setMark(r.getMark());
            }
        }

        attendance.setMarkedAt(LocalDateTime.now());
        attendanceRepo.save(attendance);

        return map(attendance, entryRepo.findByAttendanceAttendanceId(attendanceId));
    }

    /* ---------------- FINALIZE ---------------- */


    @Transactional
    public AttendanceResponse finalizeAttendance(int attendanceId) {

        Attendance attendance = getEditableAttendance(attendanceId);

        Teacher2 teacher = currentUser.getCurrentTeacher();

        if(attendance.getMarkedBy().getTeacherId() != teacher.getTeacherId()){
            throw new UnAuthorizedException("You are not authorized to update/mark Attendance");
        }

        attendance.setStatus(Attendance.AttendanceStatus.FINALIZED);
        attendance.setMarkedAt(LocalDateTime.now());

        attendance.getSession().setStatus(SessionStatus.COMPLETED);

        attendanceRepo.save(attendance);

        return map(attendance,
                entryRepo.findByAttendanceAttendanceId(attendanceId));
    }

    /* ---------------- FETCH ---------------- */


    public AttendanceResponse getAttendanceBySession(int sessionId) {

        Attendance attendance = attendanceRepo.findBySessionId(sessionId)
                .orElseThrow(() -> new IllegalStateException("Attendance not found"));

        Teacher2 teacher = currentUser.getCurrentTeacher();

        if(attendance.getMarkedBy().getTeacherId() != teacher.getTeacherId()){
            throw new UnAuthorizedException("You are not authorized to Access this Attendance");
        }

        return map(attendance, entryRepo.findByAttendanceAttendanceId(attendance.getAttendanceId()));
    }



    /* ---------------- HELPERS ---------------- */

    private Attendance getEditableAttendance(int id) {
        Attendance a = attendanceRepo.findById(id)
                .orElseThrow(() -> new IllegalStateException("Attendance not found"));

        if (a.getStatus() == Attendance.AttendanceStatus.FINALIZED) {
            throw new IllegalStateException("Attendance already finalized");
        }
        return a;
    }

    private AttendanceResponse map(
            Attendance attendance,
            List<AttendanceEntry> entries
    ) {
        return AttendanceResponse.builder()
                .attendanceId(attendance.getAttendanceId())
                .sessionId(attendance.getSession().getId())
                .status(attendance.getStatus())
                .markedAt(attendance.getMarkedAt())
                .entries(
                        entries.stream()
                                .map(e -> AttendanceEntryResponse.builder()
                                        .studentId(e.getStudent().getStudentId())
                                        .studentName(e.getStudent().getStudentName())
                                        .mark(e.getMark())
                                        .build())
                                .toList()
                )
                .build();
    }


}
