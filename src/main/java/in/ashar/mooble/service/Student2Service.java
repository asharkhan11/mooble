package in.ashar.mooble.service;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.helpers.SubmissionHelper;
import in.ashar.mooble.utility.message.MapObjects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Student2Service {

    private final GetCurrentUser currentUser;
    private final Student2Repository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final ResourceRepository resourceRepository;
    private final ResourceFolderRepository folderRepository;
    private final SubmissionHelper submissionHelper;
    private final ResourceRepository2 resourceRepository2;
    private final MinioService minioService;
    private final ResourceFolderRepository2 folderRepository2;
    private final MapObjects mapObjects;
    private final Tuition2Repository tuitionRepository;
    private final JoinRequestTuitionRepository requestTuitionRepository;
    private final AttendanceEntryRepository attendanceRepository;



    /* -------------------------
       Helpers / auth
       ------------------------- */

    private String loggedEmail() {
        return currentUser.getLoggedInUserEmail();
    }

    /**
     * Finds the Student2 corresponding to the currently logged-in user.
     * Requires Student2Repository.findByStudentEmail(String email)
     */
    private Student2 getLoggedInStudent() {
        String email = loggedEmail();
        return studentRepository.findByStudentCredentialEmail(email)
                .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    private Assignment getAssignmentById(int assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
    }

    /* -------------------------
       Profile
       ------------------------- */

    
    public Student2 getProfile() {
        return getLoggedInStudent();
    }


    /* -------------------------
       Enrollments & listings
       ------------------------- */

    public List<TuitionClass> getEnrolledTuitionClasses() {
        Student2 s = getLoggedInStudent();
        return Optional.ofNullable(s.getTuitionClasses()).orElse(Collections.emptyList());
    }

    public List<Subject2> getEnrolledSubjects() {
        Student2 s = getLoggedInStudent();
        return Optional.ofNullable(s.getSubjects()).orElse(Collections.emptyList());
    }

    public List<Course2> getEnrolledCourses() {
        Student2 s = getLoggedInStudent();
        return Optional.ofNullable(s.getCourses()).orElse(Collections.emptyList());
    }


    /**
     * Get all assignments relevant to the logged-in student.
     * Strategy: collect assignments from student's subjects + courses.
     */
    
    public List<Assignment> getMyAssignments() {
        Student2 s = getLoggedInStudent();

        Set<Assignment> assignments = new HashSet<>();

        List<Subject2> subjects = s.getSubjects();
        if (subjects != null && !subjects.isEmpty()) {

            List<Assignment> subAssign = assignmentRepository.findBySubjectSubjectIdIn(subjects.stream().map(Subject2::getSubjectId).toList());
            assignments.addAll(subAssign);

        }

        List<Course2> courses = s.getCourses();
        if (courses != null && !courses.isEmpty()) {

            List<Assignment> courseAssign = assignmentRepository.findByCourseCourseIdIn(courses.stream().map(Course2::getCourseId).toList());
            assignments.addAll(courseAssign);

        }

        return new ArrayList<>(assignments);
    }

    public List<Assignment> getAssignmentsBySubjectId(int subjectId) {
        Student2 s = getLoggedInStudent();

        boolean enrolled = s.getSubjects().stream().anyMatch(sub -> sub.getSubjectId() == subjectId);

        if (!enrolled) {
            throw new NotFoundException("You are not enrolled in this subject");
        }
        return assignmentRepository.findBySubjectSubjectId(subjectId);
    }

    @Transactional
    public Submission submitAssignment(SubmissionRequestDto requestDto) {
        Student2 student = getLoggedInStudent();
        Assignment assignment = getAssignmentById(requestDto.getAssignmentId());

        if (!submissionHelper.isMyAssignment(assignment.getId())) {
            throw new UnAuthorizedException("Access denied to submit assignment");
        }

        Optional<Submission> alreadySubmitted =
                submissionRepository.findByStudentStudentIdAndAssignmentId(
                        student.getStudentId(), assignment.getId()
                );

        if (alreadySubmitted.isPresent()) {
            return alreadySubmitted.get();
        }

        Submission submission = new Submission();

        submission.setStudent(student);
        submission.setAssignment(assignment);   // <-- MISSING, CRITICAL FIX
        submission.setSubmittedOn(LocalDateTime.now());
        submission.setResourceIds(requestDto.getResourceIds());

        if (assignment.getDueDate().isAfter(LocalDate.now())) {
            submission.setStatus(Submission.SubmissionStatus.SUBMITTED);
        } else {
            submission.setStatus(Submission.SubmissionStatus.LATE);
        }

        return submissionRepository.save(submission);
    }


    
    public Submission getMySubmission(int submissionId) {
        Student2 s = getLoggedInStudent();
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new NotFoundException("Submission not found"));
        if (sub.getStudent() == null || sub.getStudent().getStudentId() != s.getStudentId()) {
            throw new NotFoundException("Submission does not belong to you");
        }
        return sub;
    }

    /* -------------------------
       Grades & results
       ------------------------- */
    
    public List<Grade> getMyGrades() {
        Student2 s = getLoggedInStudent();
        return gradeRepository.findBySubmissionStudentStudentId(s.getStudentId());
    }

    
    public Grade getGradeForSubmission(long submissionId) {
        // fetch grade (expect repository or single result)
        return gradeRepository.findBySubmissionSubmissionId(submissionId)
                .orElseThrow(() -> new NotFoundException("Grade not found"));
    }

    
    public List<Tuition2> getMyTuition() {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            return student.getTuitionClasses().stream().map(TuitionClass::getTuition).distinct().collect(Collectors.toList());

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }

    
    public List<TuitionClass> getMyTuitionClasses() {
        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            return student.getTuitionClasses();

        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    
    public List<Subject2> getMySubjects() {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            return student.getSubjects();

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }

    
    public List<Course2> getMyCourses() {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            return student.getCourses();

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }

    
    public List<Resource> getAllResources() {
        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            List<Course2> courses = student.getCourses();
            List<Subject2> subjects = student.getSubjects();

            return resourceRepository.findAllByCourseInOrSubjectIn(courses, subjects);

        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }


    public List<Resource> getResourcesBySubjectId(int subjectId) {
        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            Subject2 subject = student.getSubjects().stream().filter(s -> s.getSubjectId() == subjectId).findAny().orElseThrow(() -> new NotFoundException("Subject id not found"));

            return resourceRepository.findAllBySubject(subject);

        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }


    public List<Resource> getResourcesByCourseId(int courseId) {
        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            Course2 course = student.getCourses().stream().filter(c -> c.getCourseId() == courseId).findAny().orElseThrow(() -> new NotFoundException("Subject id not found"));

            return resourceRepository.findAllByCourse(course);

        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }


    public List<Resource> getResourcesByFolderId(int folderId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            ResourceFolder folder = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder id not found"));

            return folder.getResources().stream().filter(r -> student.getSubjects().contains(r.getSubject()) || student.getCourses().contains(r.getCourse())).toList();

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }

    public List<ResourceFolder> getFoldersBySubjectId(int subjectId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            if (student.getSubjects().stream().noneMatch(s -> s.getSubjectId() == subjectId)) {
                throw new UnAuthorizedException("Invalid subject id");
            }

            return folderRepository.findAllBySubjectId(subjectId);

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }


    public List<ResourceFolder> getFoldersByCourseId(int courseId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            if (student.getCourses().stream().noneMatch(c -> c.getCourseId() == courseId)) {
                throw new UnAuthorizedException("Invalid course id");
            }

            return folderRepository.findAllByCourseId(courseId);

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }

    
    public List<ResourceFolder> myFolders() {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            List<Integer> resourceIds = getAllResources().stream().map(Resource::getResourceId).toList();

            return folderRepository.findAllByResourceIds(resourceIds);

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }


    public String getPresignedUrl(int resourceId) {

        Resource2 resource = resourceRepository2.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Student2 student = currentUser.getCurrentStudent();

        Integer subjectId = resource.getSubjectId();
        Integer courseId = resource.getCourseId();

        if (subjectId != null) {

            if (student.getSubjects().stream().noneMatch(s -> s.getSubjectId() == subjectId)) {
                throw new UnAuthorizedException("you can't access the resources of this subject");
            }

        } else {

            if (student.getCourses().stream().noneMatch(c -> c.getCourseId() == courseId)) {
                throw new UnAuthorizedException("you can't access the resources of this course");
            }
        }

        return minioService.generatePresignedGetUrl(
                resource.getFileName(),
                60 * 60
        );
    }


    public FolderBrowseResponse browse(
            int tuitionId,
            Integer courseId,
            Integer subjectId,
            Integer folderId
    ) {

        if ((courseId == null && subjectId == null) ||
                (courseId != null && subjectId != null)) {
            throw new IllegalArgumentException("Provide either courseId or subjectId");
        }

        Student2 student = currentUser.getCurrentStudent();

        if (courseId != null) {

            Optional<Course2> optCourse = student.getCourses().stream().filter(c -> c.getCourseId() == courseId).findAny();

            if (optCourse.isEmpty()) {
                throw new NotFoundException("Course not found");
            }

            Course2 course = optCourse.get();

            if (course.getTuition().getTuitionId() != tuitionId) {
                throw new UnAuthorizedException("Invalid tuition id is provided");
            }

        }

        if (subjectId != null) {
            Optional<Subject2> optSubject = student.getSubjects().stream().filter(s -> s.getSubjectId() == subjectId).findAny();

            if (optSubject.isEmpty()) {
                throw new NotFoundException("Subject not found");
            }

            Subject2 subject = optSubject.get();

            if (subject.getTuitionClass().getTuition().getTuitionId() != tuitionId) {
                throw new UnAuthorizedException("Invalid tuition id is provided");
            }
        }

        List<ResourceFolderDto2> folderDtos;
        List<ResourceResponse> resourceDtos;

        if (folderId == null) {
            if (courseId != null) {
                folderDtos = folderRepository2
                        .findByTuitionIdAndParentFolderIsNullAndCourseId(tuitionId, courseId)
                        .stream().map(this::toFolderDto).toList();

                resourceDtos = resourceRepository2
                        .findByTuitionIdAndCourseIdAndFolderIsNull(tuitionId, courseId)
                        .stream().map(mapObjects::mapResourceResponse2).toList();
            } else {
                folderDtos = folderRepository2
                        .findByTuitionIdAndParentFolderIsNullAndSubjectId(tuitionId, subjectId)
                        .stream().map(this::toFolderDto).toList();

                resourceDtos = resourceRepository2
                        .findByTuitionIdAndSubjectIdAndFolderIsNull(tuitionId, subjectId)
                        .stream().map(mapObjects::mapResourceResponse2).toList();
            }
        } else {
            folderDtos = folderRepository2
                    .findByParentFolder_FolderId(folderId)
                    .stream().map(this::toFolderDto).toList();

            resourceDtos = resourceRepository2
                    .findByFolder_FolderId(folderId)
                    .stream().map(mapObjects::mapResourceResponse2).toList();
        }

        return new FolderBrowseResponse(folderDtos, resourceDtos);
    }


    // ---------- mappers ----------
    private ResourceFolderDto2 toFolderDto(ResourceFolder2 f) {
        return new ResourceFolderDto2(
                f.getFolderId(),
                f.getName()
        );
    }

    public TuitionDetails findTuitionByTuitionCode(int tuitionCode) {

        Tuition2 tuition = tuitionRepository.findByTuitionCode(tuitionCode).orElse(null);

        if (tuition == null) return null;

        return TuitionDetails.builder()
                .tuitionCode(tuition.getTuitionCode())
                .tuitionName(tuition.getTuitionName())
                .branch(tuition.getBranch())
                .address(tuition.getTuitionAddress())
                .tuitionPhone(tuition.getTuitionPhoneNumber())
                .email(tuition.getTuitionEmail())
                .adminName(tuition.getTuitionAdmin().getAdminName())
                .adminPhone(tuition.getTuitionAdmin().getAdminPhoneNumber())
                .totalStudents(tuition.getStudentIds().size())
                .totalTeacher(tuition.getTeacherIds().size())
                .build();

    }


    public JoinRequestTuition requestToJoin(int tuitionCode) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {
            tuitionRepository.findByTuitionCode(tuitionCode).orElseThrow(() -> new NotFoundException("Tuition not found"));

            JoinRequestTuition request = new JoinRequestTuition();

            request.setTuitionCode(tuitionCode);
            request.setTeacher(false);
            request.setStatus(JoinRequestTuition.JoinStatus.PENDING);
            request.setUserId(student.getStudentId());

            return requestTuitionRepository.save(request);
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    public JoinRequestTuition requestStatus(int tuitionCode) {

        Object user = currentUser.getLoggedInUser();
        if (user instanceof Student2 student) {
            return requestTuitionRepository.findByTuitionCodeAndUserIdAndIsTeacher(tuitionCode, student.getStudentId(), false).orElse(null);
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    public JoinRequestTuition reRequestToJoin(int requestId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {
            JoinRequestTuition joinRequestTuition = requestTuitionRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));
            if (joinRequestTuition.getUserId() != student.getStudentId()) {
                throw new UnAuthorizedException("Invalid Request id");
            }
            joinRequestTuition.setStatus(JoinRequestTuition.JoinStatus.PENDING);
            joinRequestTuition.setRequestedOn(LocalDateTime.now());
            return requestTuitionRepository.save(joinRequestTuition);
        } else {
            throw new UnAuthorizedException("Access denied");
        }

    }


    public StudentAttendanceResponse getStudentAttendance(StudentAttendanceRequest request) {

        Student2 student = currentUser.getCurrentStudent();
        int studentId = student.getStudentId();

        List<AttendanceEntry> entries = attendanceRepository.findStudentAttendance(studentId, request.getStartDate(), request.getEndDate(), request.getSubjectId(), request.getCourseId());

        int[] counts = new int[4];
        // 0 = present, 1 = absent, 2 = late, 3 = excused

        List<StudentAttendanceSessionResponse> sessions = entries.stream().map(ae -> {

                    switch (ae.getMark()) {
                        case PRESENT -> counts[0]++;
                        case ABSENT -> counts[1]++;
                        case LATE -> counts[2]++;
                        case EXCUSED -> counts[3]++;
                    }

                    Session s = ae.getAttendance().getSession();

                    return StudentAttendanceSessionResponse.builder()
                            .sessionId(s.getId())
                            .date(s.getDate())
                            .startTime(s.getStartTime())
                            .endTime(s.getEndTime())
                            .subjectName(
                                    s.getSubject() != null ? s.getSubject().getSubjectName() : null
                            )
                            .courseName(
                                    s.getCourse() != null ? s.getCourse().getCourseName() : null
                            )
                            .mark(ae.getMark())
                            .build();
                }).toList();

        int present = counts[0];
        int absent = counts[1];
        int late = counts[2];
        int excused = counts[3];

        int total = present + absent + late + excused;

        double percentage = total == 0 ? 0 : ((present + late) * 100.0) / total;

        return StudentAttendanceResponse.builder()
                .summary(
                        StudentAttendanceSummaryResponse.builder()
                                .totalSessions(total)
                                .presentCount(present)
                                .absentCount(absent)
                                .lateCount(late)
                                .excusedCount(excused)
                                .attendancePercentage(
                                        Math.round(percentage * 100.0) / 100.0
                                )
                                .build()
                )
                .sessions(sessions)
                .build();
    }


}
