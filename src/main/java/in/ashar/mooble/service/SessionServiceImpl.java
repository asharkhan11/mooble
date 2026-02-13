package in.ashar.mooble.service;

import in.ashar.mooble.dto.SessionRequest;
import in.ashar.mooble.dto.SessionResponse;
import in.ashar.mooble.dto.SessionUpdate;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.ConflictException;
import in.ashar.mooble.exception.InvalidOptionException;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.enums.SessionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl {

    private final SessionRepository sessionRepository;
    private final TuitionClassRepository tuitionClassRepository;
    private final Subject2Repository subjectRepository;
    private final Course2Repository courseRepository;
    private final Teacher2Repository teacherRepository;
    private final GetCurrentUser currentUser;


    @Transactional
    public List<Session> createSession(SessionRequest request) {

        LocalDate startDate = request.getDate();
        LocalDate endDate = request.getRecurrenceEndDate() != null ? request.getRecurrenceEndDate() : startDate;

        // validate recurrence period
        if (endDate.isBefore(startDate)) {
            throw new ConflictException("end date cannot be before start date");
        }

        // load referenced entities
        TuitionClass tc = tuitionClassRepository.findById(request.getTuitionClassId())
                .orElseThrow(() -> new NotFoundException("TuitionClass not found"));


        Teacher2 teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new NotFoundException("Teacher not found"));


        Admin2 admin = currentUser.getCurrentAdmin();

        // decide and load subject or course once
        Subject2 subject = null;
        Course2 course = null;
        Tuition2 tuition;

        if (request.getSubjectId() != null) {

            Optional<Subject2> optSubject = tc.getSubjects().stream().filter(s -> s.getSubjectId() == request.getSubjectId()).findAny();

            if (optSubject.isEmpty()) {
                throw new NotFoundException("Subject not found");
            }

            subject = optSubject.get();
            int subjectId = subject.getSubjectId();
            tuition = subject.getTuitionClass().getTuition();

            if (teacher.getSubjects().stream().noneMatch(s -> s.getSubjectId() == subjectId)) {
                throw new UnAuthorizedException("Teacher is not enrolled in given subject");
            }

        } else {

            course = courseRepository.findById(request.getCourseId())
                    .orElseThrow(() -> new NotFoundException("Course Not Found"));
            int courseId = course.getCourseId();
            tuition = course.getTuition();

            if (teacher.getCourses().stream().noneMatch(c -> c.getCourseId() == courseId)) {
                throw new UnAuthorizedException("Teacher is not enrolled in given course");
            }
        }

        if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == tuition.getTuitionId())) {
            throw new UnAuthorizedException("Invalid subject/course");
        }


        if (request.getRecurrenceType() == SessionRequest.RecurrenceType.NONE) {

            Session s = new Session();
            s.setDate(startDate);
            s.setStartTime(request.getStartTime());
            s.setEndTime(request.getEndTime());
            s.setTuitionClass(tc);
            s.setTeacher(teacher);
            s.setCreatedBy(admin.getAdminId());

            if (subject != null) {
                s.setSubject(subject);
            } else {
                s.setCourse(course);
            }

            s.setStatus(SessionStatus.PLANNED);
            s.setRecurrenceGroupId(null);

            // ✅ NEW CONFLICT CHECK
            validateSessionConflict(s, null);

            return List.of(sessionRepository.save(s));
        } else {

            String recId = UUID.randomUUID().toString();
            List<Session> sessions = new ArrayList<>();

            LocalDate current = startDate;

            while (!current.isAfter(endDate)) {

                // recurrence filters (unchanged)
                if (request.getRecurrencePattern() == SessionRequest.RecurrencePattern.MON_SAT
                        && current.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    current = current.plusDays(1);
                    continue;
                }

                if (request.getRecurrencePattern() == SessionRequest.RecurrencePattern.CUSTOM
                        && (request.getRecurrenceDays() == null
                        || !request.getRecurrenceDays().contains(current.getDayOfWeek()))) {
                    current = current.plusDays(1);
                    continue;
                }

                Session s = new Session();
                s.setDate(current);
                s.setStartTime(request.getStartTime());
                s.setEndTime(request.getEndTime());
                s.setTuitionClass(tc);
                s.setTeacher(teacher);
                s.setCreatedBy(admin.getAdminId());

                if (subject != null) {
                    s.setSubject(subject);
                } else {
                    s.setCourse(course);
                }

                s.setStatus(SessionStatus.PLANNED);
                s.setRecurrenceGroupId(recId);

                // ✅ NEW CONFLICT CHECK (per occurrence)
                validateSessionConflict(s, null);

                sessions.add(s);
                current = current.plusDays(1);
            }

            return sessionRepository.saveAll(sessions);


        }

    }


    @Transactional
    public List<Session> updateSession(int sessionId, SessionUpdate request) {

        Session baseSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        Admin2 admin = currentUser.getCurrentAdmin();

        TuitionClass tc = baseSession.getTuitionClass();
        Tuition2 tuition = tc.getTuition();

        // ---------- TEACHER ----------
        Teacher2 teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new NotFoundException("Teacher not found"));

        // ---------- SUBJECT / COURSE ----------
        Subject2 subject;
        Course2 course;

        if (request.getSubjectId() != null && request.getCourseId() != null) {
            throw new ConflictException("Either subject or course must be provided, not both");
        }

        if (request.getSubjectId() != null) {
            course = null;

            subject = tc.getSubjects().stream()
                    .filter(s -> s.getSubjectId() == request.getSubjectId())
                    .findAny()
                    .orElseThrow(() -> new NotFoundException("Subject not found"));

            if (teacher.getSubjects().stream()
                    .noneMatch(s -> s.getSubjectId() == subject.getSubjectId())) {
                throw new UnAuthorizedException("Teacher is not enrolled in given subject");
            }

            tuition = subject.getTuitionClass().getTuition();

        } else {
            subject = null;
            if (request.getCourseId() != null) {

                course = courseRepository.findById(request.getCourseId())
                        .orElseThrow(() -> new NotFoundException("Course not found"));

                if (teacher.getCourses().stream()
                        .noneMatch(c -> c.getCourseId() == course.getCourseId())) {
                    throw new UnAuthorizedException("Teacher is not enrolled in given course");
                }

                tuition = course.getTuition();
            } else {
                course = null;
            }
        }

        // ---------- ADMIN AUTH ----------
        Tuition2 finalTuition = tuition;
        if (admin.getAdminTuition().stream()
                .noneMatch(t -> t.getTuitionId() == finalTuition.getTuitionId())) {
            throw new UnAuthorizedException("Invalid subject/course");
        }

        // ---------- TARGET SESSIONS ----------
        List<Session> targets;

        if (request.isUpdateAll()) {

            if (baseSession.getRecurrenceGroupId() == null) {
                throw new ConflictException("Session is not part of a recurrence group");
            }

            targets = sessionRepository.findAllByRecurrenceGroupId(
                    baseSession.getRecurrenceGroupId()
            );

        } else {
            targets = List.of(baseSession);
        }

        // ---------- APPLY UPDATES + CONFLICT CHECK ----------
        for (Session s : targets) {

            s.setStartTime(request.getStartTime());
            s.setEndTime(request.getEndTime());
            s.setTeacher(teacher);
            s.setUpdatedBy(admin.getAdminId());

            s.setSubject(null);
            s.setCourse(null);

            if (subject != null) {
                s.setSubject(subject);
            } else if (course != null) {
                s.setCourse(course);
            }

            if (!request.isUpdateAll()) {
                s.setRecurrenceGroupId(null); // break recurrence
            }

            // ✅ CRITICAL: exclude self
            validateSessionConflict(s, s.getId());

            s.setUpdatedBy(admin.getAdminId());
        }

        return sessionRepository.saveAll(targets);
    }


    @Transactional
    public void deleteSession(int sessionId, boolean deleteAll) {

        Session baseSession = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        Admin2 admin = currentUser.getCurrentAdmin();

        // ---------- AUTHORIZATION ----------
        if (!Objects.equals(admin.getAdminId(), baseSession.getCreatedBy())) {
            throw new UnAuthorizedException("You are not authorized to delete this session");
        }

        // ---------- SINGLE DELETE ----------
        if (!deleteAll) {
            baseSession.setStatus(SessionStatus.CANCELLED);
            baseSession.setUpdatedBy(admin.getAdminId());
            sessionRepository.save(baseSession);
            return;
        }

        // ---------- RECURRENCE DELETE ----------
        if (baseSession.getRecurrenceGroupId() == null) {
            throw new ConflictException("Session is not part of a recurrence group");
        }

        List<Session> sessions =
                sessionRepository.findAllByRecurrenceGroupId(baseSession.getRecurrenceGroupId());

        for (Session s : sessions) {
            s.setStatus(SessionStatus.CANCELLED);
            s.setUpdatedBy(admin.getAdminId());
        }

        sessionRepository.saveAll(sessions);
    }


    public Session getSession(int sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            Subject2 subject = session.getSubject();
            Course2 course = session.getCourse();

            if (subject != null) {
                if (student.getSubjects().stream().noneMatch(s -> s.getSubjectId() == subject.getSubjectId())) {
                    throw new UnAuthorizedException("You are not enrolled to this subject");
                }
            } else {
                if (student.getCourses().stream().noneMatch(c -> c.getCourseId() == course.getCourseId())) {
                    throw new UnAuthorizedException("You are not enrolled to this course");
                }
            }

        } else if (user instanceof Teacher2 teacher) {

            if (session.getTeacher().getTeacherId() != teacher.getTeacherId()) {
                throw new UnAuthorizedException("You are not authorized to access this session");
            }

        } else if (user instanceof Admin2 admin) {

            if (!Objects.equals(admin.getAdminId(), session.getCreatedBy())) {
                throw new UnAuthorizedException("You are not authorized to access this session");
            }

        } else {
            throw new UnAuthorizedException("Access Denied");
        }


        return session;
    }

    public List<Session> getAdminSessions(LocalDate startDate, LocalDate endDate) {
        Admin2 admin = currentUser.getCurrentAdmin();
        return sessionRepository.findByDateRangeAndAdmin(startDate, endDate, admin.getAdminId());
    }

    public List<Session> getTeacherSessions(LocalDate startDate, LocalDate endDate) {
        Teacher2 teacher = currentUser.getCurrentTeacher();
        return sessionRepository.findByDateRangeAndTeacher(startDate, endDate, teacher.getTeacherId());
    }


    public List<Session> getStudentSessions(LocalDate startDate, LocalDate endDate) {

        Student2 student = currentUser.getCurrentStudent();

        List<Integer> subjectIds = student.getSubjects().stream().map(Subject2::getSubjectId).toList();

        List<Integer> courseIds = student.getCourses().stream().map(Course2::getCourseId).toList();

        if (subjectIds.isEmpty() && courseIds.isEmpty()) {
            return List.of();
        }

        return sessionRepository.findByDateRangeAndStudent(startDate, endDate,
                subjectIds.isEmpty() ? List.of(-1) : subjectIds,
                courseIds.isEmpty() ? List.of(-1) : courseIds
        );


    }


//    public List<SessionResponse> getSessionsForDateAndClass(LocalDate date, int tuitionClassId) {
//
//        Object user = currentUser.getLoggedInUser();
//
//        if (user instanceof Student2 student) {
//            if (student.getTuitionClasses().stream().noneMatch(tc -> tc.getTuitionClassId() == tuitionClassId)) {
//                throw new UnAuthorizedException("Invalid Tuition class id");
//            }
//        } else if (user instanceof Teacher2 teacher) {
//            if (teacher.getTuitionClasses().stream().noneMatch(tc -> tc.getTuitionClassId() == tuitionClassId)) {
//                throw new UnAuthorizedException("Invalid Tuition class id");
//            }
//        } else if (user instanceof Admin2 admin) {
//
//            int tuitionId = tuitionClassRepository.findById(tuitionClassId).orElseThrow(() -> new NotFoundException("Tuition class not found")).getTuition().getTuitionId();
//
//            if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == tuitionId)) {
//                throw new UnAuthorizedException("Invalid Tuition class id");
//            }
//        } else {
//            throw new UnAuthorizedException("Access Denied");
//        }
//
//        return sessionRepository.findAllByDateAndTuitionClassTuitionClassId(date, tuitionClassId)
//                .stream().map(this::toResponse).collect(Collectors.toList());
//    }
//
//
//    public List<SessionResponse> getUpcomingSessions(LocalDate date) {
//
//        Object user = currentUser.getLoggedInUser();
//
//        if (user instanceof Teacher2 teacher) {
//            List<Session> sessions = sessionRepository
//                    .findUpcomingSessionsForTeacher(teacher.getTeacherId(), date);
//            return sessions.stream().map(this::toResponse).toList();
//        } else if (user instanceof Student2 student) {
//
//            List<Integer> subjectIds = student.getSubjects()
//                    .stream()
//                    .map(Subject2::getSubjectId)
//                    .toList();
//
//            if (subjectIds.isEmpty()) {
//                return List.of();
//            }
//
//
//            List<Session> sessions =
//                    sessionRepository.findUpcomingSessionsForStudentMultipleSubjects(
//                            subjectIds,
//                            date
//                    );
//
//            return sessions.stream().map(this::toResponse).toList();
//        } else {
//            throw new UnAuthorizedException("Access Desnied");
//        }
//
//    }


    @Transactional(readOnly = true)
    private void validateSessionConflict(
            Session session,
            Integer excludeSessionId
    ) {
        LocalDate date = session.getDate();
        LocalTime start = session.getStartTime();
        LocalTime end = session.getEndTime();

        if (session.getTeacher() != null &&
                sessionRepository.existsTeacherConflict(
                        date, start, end,
                        session.getTeacher().getTeacherId(),
                        excludeSessionId
                )) {
            throw new ConflictException("Teacher is already busy");
        }

        if (session.getSubject() != null &&
                sessionRepository.existsTuitionClassConflict(
                        date, start, end,
                        session.getTuitionClass().getTuitionClassId(),
                        excludeSessionId
                )) {
            throw new ConflictException("Class already has a session");
        }

        if (session.getCourse() != null &&
                sessionRepository.existsCourseConflict(
                        date, start, end,
                        session.getCourse().getCourseId(),
                        session.getCourse().getSubjectIds(),
                        excludeSessionId
                )) {
            throw new ConflictException("Course subject conflict detected");
        }
    }


}
