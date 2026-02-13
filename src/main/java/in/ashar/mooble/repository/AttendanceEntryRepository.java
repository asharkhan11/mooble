package in.ashar.mooble.repository;

import in.ashar.mooble.entity.AttendanceEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceEntryRepository extends JpaRepository<AttendanceEntry, Integer> {

    List<AttendanceEntry> findByAttendanceAttendanceId(int attendanceId);

    @Query("""
                SELECT ae
                FROM AttendanceEntry ae
                JOIN FETCH ae.attendance a
                JOIN FETCH a.session s
                JOIN FETCH s.subject sub
                LEFT JOIN FETCH s.course c
                WHERE ae.student.studentId = :studentId
                  AND s.date BETWEEN :startDate AND :endDate
                  AND a.status = in.ashar.mooble.entity.Attendance.AttendanceStatus.FINALIZED
                  AND (:subjectId IS NULL OR sub.subjectId = :subjectId)
                  AND (:courseId IS NULL OR c.courseId = :courseId)
                ORDER BY s.date, s.startTime
            """)
    List<AttendanceEntry> findStudentAttendance(
            int studentId,
            LocalDate startDate,
            LocalDate endDate,
            Integer subjectId,
            Integer courseId
    );


}
