package in.ashar.mooble.service;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.InvalidOptionException;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.helpers.TeacherHelper;
import in.ashar.mooble.utility.message.MapObjects;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Teacher2Service {

    private final TeacherHelper helper;
    private final GetCurrentUser currentUser;

    private final Teacher2Repository teacherRepository;
    private final TuitionClassRepository tuitionClassRepository;
    private final Student2Repository studentRepository;
    private final AssignmentRepository assignmentRepository;
    private final GradeRepository gradeRepository;
    private final Tuition2Repository tuitionRepository;
    private final SubmissionService submissionService;
    private final JoinRequestTuitionRepository requestTuitionRepository;
    private final Course2Repository courseRepository;
    private final Subject2Repository subjectRepository;
    private final ResourceFolderRepository2 folderRepository2;
    private final MinioService minioService;
    private final ResourceRepository2 resourceRepository2;
    private final MapObjects mapObjects;
    private final SubscriptionGuardService subscriptionGuardService;
    private final AttendanceRepository attendanceRepository;



    /* -------------------------
       Authorization / Helpers
       ------------------------- */

    private String loggedEmail() {
        return currentUser.getLoggedInUserEmail();
    }

    /**
     * Find teacher by currently logged in user's email.
     * Assumes Teacher2Repository has findByTeacherEmail(String email).
     */
    private Teacher2 getLoggedInTeacher() {
        String email = loggedEmail();
        return teacherRepository.findByTeacherCredentialEmail(email)
                .orElseThrow(() -> new NotFoundException("Teacher not found for logged in email"));
    }


    private TuitionClass getTuitionClassById(int classId) {

        return tuitionClassRepository.findById(classId)
                .orElseThrow(() -> new NotFoundException("TuitionClass not found"));
    }


    private Assignment getAssignmentById(int assignmentId) {
        return assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new NotFoundException("Assignment not found"));
    }

    /* -------------------------
       Profile operations
       ------------------------- */


    public Teacher2 getProfile() {
        return getLoggedInTeacher();
    }


    public void removeFromClass(@Positive int classId) {

        Teacher2 teacher = getLoggedInTeacher();
        TuitionClass tuitionClass = getTuitionClassById(classId);

        if (teacher.getTuitionClasses().contains(tuitionClass)) {

            teacher.getTuitionClasses().remove(tuitionClass);
            tuitionClass.getTeachers().remove(teacher);

            Tuition2 tuition = tuitionClass.getTuition();

            Optional<TuitionClass> isPresentInOtherClassOfSameTuition = tuition.getTuitionClasses().stream().filter(tc -> tc.getTeachers().contains(teacher)).findAny();

            if (isPresentInOtherClassOfSameTuition.isEmpty()) {
                tuition.getTeacherIds().remove(Integer.valueOf(teacher.getTeacherId()));
                tuitionRepository.save(tuition);
            } else {
                tuitionClassRepository.save(tuitionClass);
            }

            teacherRepository.save(teacher);
        }

    }


    public void deleteMyProfile() {

        Teacher2 teacher = getLoggedInTeacher();

        List<TuitionClass> tuitionClasses = teacher.getTuitionClasses();

        tuitionClasses.forEach(tc -> tc.getTeachers().removeIf(t -> t.getTeacherId() == teacher.getTeacherId()));

        Set<Tuition2> tuitions = tuitionClasses.stream().map(TuitionClass::getTuition).collect(Collectors.toSet());

        tuitions.forEach(t -> t.getTeacherIds().removeIf(tId -> tId == teacher.getTeacherId()));

        List<Attendance> attendance = attendanceRepository.findAllByMarkedByTeacherId(teacher.getTeacherId());

        tuitionRepository.saveAll(tuitions);

        teacherRepository.delete(teacher);

    }

    /* -------------------------
       Listing assigned items
       ------------------------- */

    public List<TuitionClass> getAssignedTuitionClasses() {
        Teacher2 t = getLoggedInTeacher();
        return Optional.ofNullable(t.getTuitionClasses()).orElse(Collections.emptyList());
    }


    public List<Tuition2> getAssignedTuition() {
        Teacher2 teacher = getLoggedInTeacher();
        return Optional.of(new ArrayList<>(teacher.getTuitionClasses().stream().map(TuitionClass::getTuition).collect(Collectors.toSet()))).orElse(new ArrayList<>());
    }


    public List<Subject2> getAssignedSubjects() {
        Teacher2 t = getLoggedInTeacher();
        return Optional.ofNullable(t.getSubjects()).orElse(Collections.emptyList());
    }


    public List<Course2> getAssignedCourses() {
        Teacher2 t = getLoggedInTeacher();
        return Optional.ofNullable(t.getCourses()).orElse(Collections.emptyList());
    }

    /* -------------------------
       Students And Teachers
       ------------------------- */

    public List<Student2> getStudentsInTuitionClass(int tuitionClassId) {

        TuitionClass tuitionClass = helper.getOwnTuitionClass(tuitionClassId);

        return tuitionClass.getStudents();
    }



    public List<Student2> getStudentsInSubject(int subjectId) {
        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {

            Subject2 subject = teacher.getSubjects().stream()
                    .filter(s -> s.getSubjectId() == subjectId)
                    .findAny()
                    .orElseThrow(() -> new UnAuthorizedException("Access denied"));


            return studentRepository.findAllBySubjectsContaining(subject);

        } else if (user instanceof Admin2) {
            return Collections.emptyList();
        } else {
            throw new UnAuthorizedException("Access denied");
        }
    }



    public List<Student2> getStudentsInCourse(int courseId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {

            Course2 course = teacher.getCourses().stream()
                    .filter(c -> c.getCourseId() == courseId)
                    .findAny()
                    .orElseThrow(() -> new UnAuthorizedException("Access denied"));

            return studentRepository.findAllByCoursesContaining(course);

        } else if (user instanceof Admin2) {
            return Collections.emptyList();
        } else {
            throw new UnAuthorizedException("Access denied");
        }

    }



    public List<Teacher2> getTeachersInTuitionClass(@Positive int classId) {

        TuitionClass tuitionClass = helper.getOwnTuitionClass(classId);

        return tuitionClass.getTeachers();
    }

    /* -------------------------
       Assignment management
       ------------------------- */


    public List<Assignment> getAssignmentsOfTeacher() {

        Teacher2 teacher = getLoggedInTeacher();

        return assignmentRepository.findByTeacher(teacher);

    }

    public List<Assignment> getAssignmentsBySubjectId(int subjectId) {

        Teacher2 teacher = getLoggedInTeacher();

        teacher.getSubjects().stream().filter(s -> s.getSubjectId() == subjectId).findAny().orElseThrow(() -> new UnAuthorizedException("Invalid subject id"));

        return assignmentRepository.findBySubjectSubjectIdAndTeacher(subjectId, teacher);

    }


    public List<Assignment> getAssignmentsByCourseId(int courseId) {

        Teacher2 teacher = getLoggedInTeacher();

        teacher.getCourses().stream().filter(c -> c.getCourseId() == courseId).findAny().orElseThrow(() -> new UnAuthorizedException("Invalid course id"));

        return assignmentRepository.findBySubjectSubjectIdAndTeacher(courseId, teacher);

    }

    @Transactional

    public Assignment createAssignment(@Valid AssignmentRequestDto assignmentDto) {

        int subjectId = assignmentDto.getSubjectId();
        int courseId = assignmentDto.getCourseId();
        Teacher2 teacher = getLoggedInTeacher();

        if (subjectId == 0 && courseId == 0) {
            throw new InvalidOptionException("Either subject id or course id must be provided");
        }


        Assignment assignment = new Assignment();
        assignment.setTeacher(teacher);

        if (subjectId != 0) {

            Subject2 subject = teacher.getSubjects().stream().filter(s -> s.getSubjectId() == subjectId).findAny().orElse(null);
            if (subject == null) {
                throw new UnAuthorizedException("Invalid subject id");
            }
            assignment.setSubject(subject);

        } else {

            Course2 course = teacher.getCourses().stream().filter(c -> c.getCourseId() == courseId).findAny().orElse(null);
            if (course == null) {
                throw new UnAuthorizedException("Invalid course id");
            }
            assignment.setCourse(course);
        }

        assignment.setAssignedDate(LocalDate.now());
        assignment.setTitle(assignmentDto.getTitle());
        assignment.setDescription(assignmentDto.getDescription());
        assignment.setResourceIds(assignmentDto.getResourceIds());
        assignment.setDueDate(assignmentDto.getDueDate());
        assignment.setMaxMarks(assignmentDto.getMaxMarks());

        return assignmentRepository.save(assignment);
    }


    @Transactional

    public Assignment updateAssignment(int assignmentId, AssignmentUpdateDto assignmentDto) {
        Assignment existing = getAssignmentById(assignmentId);
        Teacher2 teacher = getLoggedInTeacher();

        if (existing.getTeacher().getTeacherId() != teacher.getTeacherId()) {
            throw new UnAuthorizedException("Invalid assignment id");
        }

        existing.setTitle(assignmentDto.getTitle());
        existing.setDescription(assignmentDto.getDescription());
        existing.setDueDate(assignmentDto.getDueDate());
        existing.setMaxMarks(assignmentDto.getMaxMarks());
        if (assignmentDto.getResourceIds() != null && !assignmentDto.getResourceIds().isEmpty()) {
            existing.setResourceIds(assignmentDto.getResourceIds());
        }

        return assignmentRepository.save(existing);
    }

    @Transactional

    public void deleteAssignment(int assignmentId) {
        Assignment existing = getAssignmentById(assignmentId);
        Teacher2 teacher = getLoggedInTeacher();
        if (existing.getTeacher().getTeacherId() != teacher.getTeacherId()) {
            throw new UnAuthorizedException("Invalid assignment id");
        }

        submissionService.deleteSubmissionByAssignmentId(assignmentId);
        assignmentRepository.delete(existing);
    }

//    public List<Submission> viewSubmissionsForAssignment(int assignmentId) {
//        Assignment a = getAssignmentById(assignmentId);
//        Teacher2 logged = getLoggedInTeacher();
////        if (a.getCreatedBy() == null || a.getCreatedBy().getTeacherId() != logged.getTeacherId()) {
////            throw new NotFoundException("Unauthorized: you cannot view submissions for this assignment");
////        }
//        return submissionRepository.findByAssignmentAssignmentId(assignmentId);
//    }

    /* -------------------------
       Grading
       ------------------------- */

//    @Transactional
//    public Grade gradeSubmission(int submissionId, Grade grade) {
//        Submission s = submissionRepository.findById(submissionId)
//                .orElseThrow(() -> new NotFoundException("Submission not found"));
//        Teacher2 logged = getLoggedInTeacher();
//
//        Assignment a = s.getAssignment();

    /// /        if (a == null || a.getCreatedBy() == null || a.getCreatedBy().getTeacherId() != logged.getTeacherId()) {
    /// /            throw new NotFoundException("Unauthorized: you cannot grade this submission");
    /// /        }
//
//        grade.setSubmission(s);
//        grade.setGradedBy(logged);
//        grade.setGradedAt(Optional.ofNullable(grade.getGradedAt()).orElse(LocalDateTime.now()));
//        return gradeRepository.save(grade);
//    }

    /* -------------------------
       Resource management
       ------------------------- */

//    @Transactional
//    public Resource addResourceToSubject(int subjectId, MultipartFile file) {
//
//        Subject2 s = getSubjectById(subjectId);
//        Teacher2 logged = getLoggedInTeacher();
//
//
//        Resource resource = helper.addResourceToSubject(s, logged, file);
//
//        List<Resource> list = Optional.ofNullable(s.getResources()).orElse(new ArrayList<>());
//        list.add(resource);
//        s.setResources(list);
//        subjectRepository.save(s);
//
//        return resource;
//    }

//    @Transactional
//    public void deleteResource(long resourceId) {
//        Resource r = resourceRepository.findById(resourceId)
//                .orElseThrow(() -> new NotFoundException("Resource not found"));
//        Teacher2 logged = getLoggedInTeacher();
//
//
//        if(!r.getUploadedBy().equals(logged)){
//            throw new UnAuthorizedException("Invalid resource id : "+ resourceId );
//        }
//        resourceRepository.delete(r);
//    }

    /* -------------------------
       Attendance
       ------------------------- */

//    @Transactional
//    public Attendance markAttendance(int subjectId, List<Integer> studentIds) {
//
//        Subject2 subject = getSubjectById(subjectId);
//
//        Teacher2 logged = getLoggedInTeacher();
//
//        List<Student2> studentsInSubject = getStudentsForSubject(subjectId);
//
//        List<Student2> presentStudents = studentsInSubject.stream().filter(s -> studentIds.contains(s.getStudentId())).toList();
//
//        Attendance attendance = Attendance.builder()
//                .teacher(logged)
//                .students(presentStudents)
//                .subject(subject)
//                .date(LocalDateTime.now())
//                .build();
//
//        return attendanceRepository.save(attendance);
//    }

    /* -------------------------
       Misc helpers
       ------------------------- */
    public List<Grade> getGradesGivenByLoggedInTeacher() {
        Teacher2 t = getLoggedInTeacher();
        return gradeRepository.findByGradedByTeacherId(t.getTeacherId());
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

        if (user instanceof Teacher2 teacher) {
            tuitionRepository.findByTuitionCode(tuitionCode).orElseThrow(() -> new NotFoundException("Tuition not found"));

            JoinRequestTuition request = new JoinRequestTuition();

            request.setTuitionCode(tuitionCode);
            request.setTeacher(true);
            request.setStatus(JoinRequestTuition.JoinStatus.PENDING);
            request.setUserId(teacher.getTeacherId());

            return requestTuitionRepository.save(request);
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    public JoinRequestTuition requestStatus(int tuitionCode) {

        Object user = currentUser.getLoggedInUser();
        if (user instanceof Teacher2 teacher) {
            return requestTuitionRepository.findByTuitionCodeAndUserIdAndIsTeacher(tuitionCode, teacher.getTeacherId(),true).orElse(null);
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    public JoinRequestTuition reRequestToJoin(int requestId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {
            JoinRequestTuition joinRequestTuition = requestTuitionRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));
            if (joinRequestTuition.getUserId() != teacher.getTeacherId()) {
                throw new UnAuthorizedException("Invalid Request id");
            }
            joinRequestTuition.setStatus(JoinRequestTuition.JoinStatus.PENDING);
            joinRequestTuition.setRequestedOn(LocalDateTime.now());
            return requestTuitionRepository.save(joinRequestTuition);
        } else {
            throw new UnAuthorizedException("Access denied");
        }

    }


    /* Resource */

    public Resource2 uploadResource(MultipartFile file, int tuitionId, Integer courseId, Integer subjectId, Integer folderId
    ) throws Exception {

        // ----- course / subject exclusivity -----
        if ((courseId == null && subjectId == null) ||
                (courseId != null && subjectId != null)) {
            throw new IllegalArgumentException("Provide either courseId or subjectId");
        }

        // ----- validate tuition -----
        Teacher2 teacher = currentUser.getCurrentTeacher();

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found"));

        // 🔒 SUBSCRIPTION STORAGE CHECK
        subscriptionGuardService.assertCanUpload(tuition.getTuitionAdmin(), file.getSize());


        if(tuition.getTeacherIds().stream().noneMatch(tId-> tId == teacher.getTeacherId())){
            throw new UnAuthorizedException("Teacher does not belongs to this tuition");
        }

        // ----- validate scope entity -----
        if (courseId != null) {
            courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException("Course not found"));

            if (teacher.getCourses().stream().noneMatch(c-> c.getCourseId() == courseId)) {
                throw new UnAuthorizedException("you are not authorized to add resource in this course");
            }
        }

        if (subjectId != null) {
            subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new NotFoundException("Subject not found"));

            if(teacher.getSubjects().stream().noneMatch(s -> s.getSubjectId() == subjectId)){
                throw new UnAuthorizedException("you are not authorized to add resource in this subject");
            }

        }

        // ----- validate folder -----
        ResourceFolder2 folder = null;
        if (folderId != null) {
            folder = folderRepository2.findById(folderId)
                    .orElseThrow(() -> new NotFoundException("Folder not found"));

            if (!Objects.equals(folder.getTuitionId(), tuitionId)) {
                throw new IllegalArgumentException("Folder belongs to another tuition");
            }

            if (courseId != null) {
                if (!Objects.equals(folder.getCourseId(), courseId)) {
                    throw new IllegalArgumentException("Folder does not belong to this course");
                }
            } else { // subjectId != null
                if (!Objects.equals(folder.getSubjectId(), subjectId)) {
                    throw new IllegalArgumentException("Folder does not belong to this subject");
                }
            }

        }

        // ----- generate MinIO object key -----
        String objectKey =
                "tuition-" + tuitionId + "/" +
                        UUID.randomUUID() + "-" + file.getOriginalFilename();

        // ----- upload -----
        try (InputStream is = file.getInputStream()) {
            minioService.uploadToMinio(
                    is,
                    file.getSize(),
                    file.getContentType(),
                    objectKey
            );
        }

        // ----- persist metadata -----
        Resource2 resource = new Resource2();
        resource.setCredentialId(teacher.getTeacherCredential().getUserId());
        resource.setName(file.getOriginalFilename());
        resource.setType(file.getContentType());
        resource.setSize(file.getSize());
        resource.setFileName(objectKey);
        resource.setUploadedAt(LocalDateTime.now());
        resource.setTuitionId(tuitionId);
        resource.setCourseId(courseId);
        resource.setSubjectId(subjectId);
        resource.setFolder(folder);

        Resource2 saved = resourceRepository2.save(resource);

        // ✅ INCREMENT STORAGE USAGE
        subscriptionGuardService.onFileUploaded(tuition.getTuitionAdmin(), file.getSize());

        return saved;

    }


    public String getPresignedUrl(int resourceId) {

        Resource2 resource = resourceRepository2.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Teacher2 teacher = currentUser.getCurrentTeacher();

        Integer subjectId = resource.getSubjectId();
        Integer courseId = resource.getCourseId();

        if(subjectId != null){

            if(teacher.getSubjects().stream().noneMatch(s-> s.getSubjectId() == subjectId)){
                throw new UnAuthorizedException("you can't access this resource");
            }

        } else {

            if(teacher.getCourses().stream().noneMatch(c-> c.getCourseId() == courseId)){
                throw new UnAuthorizedException("you can't access this resource");
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

        Teacher2 teacher = currentUser.getCurrentTeacher();

        if (courseId != null) {

            Optional<Course2> optCourse = teacher.getCourses().stream().filter(c -> c.getCourseId() == courseId).findAny();

            if(optCourse.isEmpty()){
                throw new NotFoundException("Course not found");
            }

            Course2 course = optCourse.get();

            if(course.getTuition().getTuitionId() != tuitionId){
                throw new UnAuthorizedException("Invalid tuition id is provided");
            }

        }

        if (subjectId != null) {
            Optional<Subject2> optSubject= teacher.getSubjects().stream().filter(s -> s.getSubjectId() == subjectId).findAny();

            if(optSubject.isEmpty()){
                throw new NotFoundException("Subject not found");
            }

            Subject2 subject = optSubject.get();

            if(subject.getTuitionClass().getTuition().getTuitionId() != tuitionId){
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


    public void deleteResource(int resourceId) {

        Resource2 resource = resourceRepository2.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Tuition2 tuition = tuitionRepository.findById(resource.getTuitionId()).orElseThrow(() -> new NotFoundException("tuition not found"));

        Teacher2 teacher = currentUser.getCurrentTeacher();

        if (resource.getCredentialId() != teacher.getTeacherCredential().getUserId()) {
            throw new UnAuthorizedException("You can't delete this Resource");
        }

        String objectKey = resource.getFileName();

        // 1️⃣ delete from MinIO first
        try {
            minioService.deleteObject(objectKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from storage", e);
        }

        // 2️⃣ delete metadata
        resourceRepository2.delete(resource);

        subscriptionGuardService.onFileDeleted(tuition.getTuitionAdmin(), resource.getSize());

    }


    @Transactional
    public void renameResource(int resourceId, String newName) {

        Resource2 resource = resourceRepository2.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Teacher2 teacher = currentUser.getCurrentTeacher();

        if (resource.getCredentialId() != teacher.getTeacherCredential().getUserId()) {
            throw new UnAuthorizedException("You can't rename this Resource");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        resource.setName(newName.trim());
        resourceRepository2.save(resource);
    }


//    public Attendance updateAttendance(@Positive int attendanceId, List<Integer> studentIds) {
//
//        Attendance attendance = attendanceRepository.findById(attendanceId).orElseThrow(() -> new NotFoundException("Attendance not found with id : " + attendanceId));
//
//        List<Student2> studentsInSubject = getStudentsForSubject(attendance.getSubject().getSubjectId());
//
//        List<Student2> presentStudents = studentsInSubject.stream().filter(s -> studentIds.contains(s.getStudentId())).toList();
//
//        attendance.setStudents(presentStudents);
//
//        return  attendanceRepository.save(attendance);
//    }



    // ---------- mappers ----------
    private ResourceFolderDto2 toFolderDto(ResourceFolder2 f) {
        return new ResourceFolderDto2(
                f.getFolderId(),
                f.getName()
        );
    }
}
