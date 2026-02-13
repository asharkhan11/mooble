package in.ashar.mooble.controller;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.service.Admin2Service;
import in.ashar.mooble.utility.message.MapObjects;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {


    private final MapObjects mapObjects;
    private final Admin2Service adminService;
    private final GetCurrentUser currentUser;


    @GetMapping("/count")
    public ResponseEntity<AllCount> getAllCount() {
        return ResponseEntity.ok(adminService.getAllCount(currentUser.getCurrentAdmin().getAdminId()));
    }


    @GetMapping("/my-profile")
    public ResponseEntity<Admin2ResponseDto> myProfile() {
        Admin2 admin = adminService.myProfile();
        Admin2ResponseDto response = mapObjects.mapAdminResponse(admin);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-profile")
    public ResponseEntity<Admin2ResponseDto> updateProfile(@RequestBody @Valid Admin2Dto request) {
        Admin2 admin = adminService.updateProfile(request);
        Admin2ResponseDto response = mapObjects.mapAdminResponse(admin);
        return ResponseEntity.ok(response);
    }

    /* --------- Tuition endpoints --------- */

    @PostMapping("/tuition")
    public ResponseEntity<Tuition2ResponseDto> createTuition(@RequestBody TuitionRequestDto dto) {
        Tuition2 tuition = adminService.createTuition(dto);
        return ResponseEntity.ok(mapObjects.mapTuitionResponse(tuition));
    }

    @DeleteMapping("/tuition/{tuitionId}")
    public ResponseEntity<Void> deleteTuitionById(@PathVariable("tuitionId") int tuitionId) {
        adminService.deleteTuitionById(tuitionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/tuition")
    public ResponseEntity<List<Tuition2ResponseDto>> getAllTuitionOfAdmin() {
        return ResponseEntity.ok(adminService.getAllTuitionOfAdmin(currentUser.getCurrentAdmin().getAdminId()));
    }


    @GetMapping("/tuition/{tuitionId}")
    public ResponseEntity<Tuition2ResponseDto> getTuitionById(@PathVariable @Positive(message = "tuitionId must be positive") int tuitionId) {
        return ResponseEntity.ok(mapObjects.mapTuitionResponse(adminService.getTuitionById(tuitionId)));
    }

    @PutMapping("/tuition/{tuitionId}")
    public ResponseEntity<Tuition2ResponseDto> updateTuition(@PathVariable @Positive int tuitionId, @Valid @RequestBody Tuition2UpdateDto tuitionDto) {
        return ResponseEntity.ok(adminService.updateTuition(tuitionId, tuitionDto));
    }

    /* Tuition Join Request */

    @GetMapping("/join-requests/{tuitionId}")
    public ResponseEntity<List<JoinRequestTuition>> getTuitionJoinRequests(@PathVariable("tuitionId") int tuitionId) {
        return ResponseEntity.ok(adminService.getTuitionJoinRequests(tuitionId));
    }

    @GetMapping("/approved/student/tuition/{tuitionId}")
    public ResponseEntity<List<Student2ResponseDto>> getApprovedStudents(@PathVariable("tuitionId") int tuitionId) {
        List<Student2> students = adminService.getApprovedStudents(tuitionId);
        List<Student2ResponseDto> response = students.stream().map(mapObjects::mapStudentResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/approved/teacher/tuition/{tuitionId}")
    public ResponseEntity<List<TeacherResponseDto>> getApprovedTeachers(@PathVariable("tuitionId") int tuitionId) {
        List<Teacher2> teachers = adminService.getApprovedTeachers(tuitionId);
        List<TeacherResponseDto> response = teachers.stream().map(mapObjects::mapTeacherResponse).toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/process-request")
    public ResponseEntity<JoinRequestTuition> processRequest(@RequestBody ProcessJoinRequest request) {
        return ResponseEntity.ok(adminService.processRequest(request));
    }

    @GetMapping("/student-detail/{requestId}")
    public ResponseEntity<StudentDetail> getStudentDetail(@PathVariable int requestId) {

        StudentDetail studentDetail = adminService.getStudentDetail(requestId);
        return ResponseEntity.ok(studentDetail);
    }

    @GetMapping("/teacher-detail/{requestId}")
    public ResponseEntity<TeacherDetail> getTeacherDetail(@PathVariable int requestId) {

        TeacherDetail teacherDetail = adminService.getTeacherDetail(requestId);
        return ResponseEntity.ok(teacherDetail);
    }


    /* Enroll Teacher */

    @PostMapping("/enroll/teacher")
    public ResponseEntity<TeacherResponseDto> enrollTeacher(@RequestBody @Valid EnrollTeacherRequest request) {

        Teacher2 teacher = adminService.enrollTeacher(request);

        return ResponseEntity.ok(mapObjects.mapTeacherResponse(teacher));
    }

    /* Enroll Student */

    @PostMapping("/enroll/student")
    public ResponseEntity<Student2ResponseDto> enrollStudent(@RequestBody @Valid EnrollStudentRequest request) {

        Student2 student = adminService.enrollStudent(request);

        return ResponseEntity.ok(mapObjects.mapStudentResponse(student));
    }

    /* --------- TuitionClass endpoints --------- */

    @GetMapping("/tuition/{tuitionId}/class")
    public ResponseEntity<List<TuitionClassResponseDto>> getAllTuitionClassesOfTuition(@PathVariable("tuitionId") int tuitionId) {
        return ResponseEntity.ok(adminService.getAllTuitionClassesOfTuition(tuitionId));
    }

    @GetMapping("/class/{classId}")
    public ResponseEntity<TuitionClassResponseDto> getTuitionClassById(@PathVariable("classId") int classId) {
        return ResponseEntity.ok(mapObjects.mapTuitionClassResponse(adminService.getTuitionClassById(classId)));
    }

    @GetMapping("/class/all")
    public ResponseEntity<List<TuitionClassResponseDto>> getTuitionClassesByIds(@RequestParam String tuitionClassIds) {
        List<Integer> list = Arrays.stream(tuitionClassIds.split(",")).map(Integer::parseInt).toList();
        List<TuitionClass> tuitionClassResponseDtoList = adminService.getTuitionClassesByIds(list);
        return ResponseEntity.ok(tuitionClassResponseDtoList.stream().map(mapObjects::mapTuitionClassResponse).toList());
    }

    @PostMapping("/class")
    public ResponseEntity<TuitionClassResponseDto> createTuitionClass(@RequestBody @Valid TuitionClassDto tuitionClassDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createTuitionClass(tuitionClassDto));
    }

    @PutMapping("/class/{classId}")
    public ResponseEntity<TuitionClassResponseDto> updateTuitionClass(@PathVariable @Positive int classId, @RequestBody @Valid TuitionClassUpdateDto tuitionClassDto) {
        TuitionClass tuitionClass = adminService.updateTuitionClass(classId, tuitionClassDto);
        TuitionClassResponseDto tuitionClassResponseDto = mapObjects.mapTuitionClassResponse(tuitionClass);
        return ResponseEntity.ok(tuitionClassResponseDto);
    }

    @DeleteMapping("/class/{classId}")
    public ResponseEntity<Void> deleteTuitionClass(@PathVariable @Positive int classId) {
        adminService.deleteTuitionClass(classId);
        return ResponseEntity.noContent().build();
    }

    /// ////////////////////////////////////////////////////////////////////////////////////

    /* --------- Student endpoints (scoped to admin's tuition) --------- */
    @GetMapping("/tuition/{tuitionId}/student")
    public ResponseEntity<List<Student2ResponseDto>> getStudentsInTuition(@PathVariable @Positive int tuitionId) {
        List<Student2> students = adminService.getAllStudentsInTuition(tuitionId);
        List<Student2ResponseDto> list = students.stream().map(mapObjects::mapStudentResponse).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tuition/student")
    public ResponseEntity<List<Student2ResponseDto>> getAllStudentsOfAdmin() {
        List<Student2> students = adminService.getAllStudentsOfAdmin();
        List<Student2ResponseDto> list = students.stream().map(mapObjects::mapStudentResponse).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/tuition/{tuitionId}/student")
    public ResponseEntity<Student2ResponseDto> createStudent(@PathVariable @Positive int tuitionId, @Valid @RequestBody Student2Dto studentDto) {

        Student2ResponseDto response = mapObjects.mapStudentResponse(adminService.createStudent2(tuitionId, studentDto));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/student/{studentId}")
    public ResponseEntity<Student2ResponseDto> updateStudent(@PathVariable @Positive int studentId, @Valid @RequestBody Student2UpdateDto dto) {


        Student2 updated = adminService.updateStudent(studentId, dto);
        Student2ResponseDto response = mapObjects.mapStudentResponse(updated);

        return ResponseEntity.ok(response);
    }

    /* --------- Teacher endpoints --------- */

    @GetMapping("/tuition/{tuitionId}/teacher")
    public ResponseEntity<List<TeacherResponseDto>> getTeachersInTuition(@PathVariable @Positive int tuitionId) {
        List<Teacher2> teachers = adminService.getAllTeachersInTuition(tuitionId);
        List<TeacherResponseDto> list = teachers.stream().map(mapObjects::mapTeacherResponse).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tuition/teacher")
    public ResponseEntity<List<TeacherResponseDto>> getAllTeachersOfAdmin() {
        List<Teacher2> teachers = adminService.getAllTeachersOfAdmin();
        List<TeacherResponseDto> list = teachers.stream().map(mapObjects::mapTeacherResponse).toList();
        return ResponseEntity.ok(list);
    }

    @PostMapping("/tuition/{tuitionId}/teacher")
    public ResponseEntity<TeacherResponseDto> createTeacher(
            @PathVariable @Positive int tuitionId,
            @Valid @RequestBody Teacher2Dto teacherDto) {

        Teacher2 saved = adminService.createTeacher2(tuitionId, teacherDto);

        TeacherResponseDto response = mapObjects.mapTeacherResponse(saved);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/teacher/{teacherId}")
    public ResponseEntity<TeacherResponseDto> updateTeacher(
            @PathVariable @Positive int teacherId,
            @Valid @RequestBody Teacher2UpdateDto dto) {

        Teacher2 updated = adminService.updateTeacher(teacherId, dto);

        TeacherResponseDto response = mapObjects.mapTeacherResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tuition/{tuitionId}/teacher/{teacherId}")
    public ResponseEntity<Void> removeTeacherFromTuition(@PathVariable @Positive int tuitionId, @PathVariable @Positive int teacherId) {
        adminService.removeTeacherFromTuition(tuitionId, teacherId);
        return ResponseEntity.noContent().build();
    }

    /* --------- Assign / enroll endpoints (class membership) --------- */

    @PostMapping("/class/{classId}/student/{studentEmail}")
    public ResponseEntity<Void> addStudentToClass(
            @PathVariable @Positive int classId,
            @PathVariable @Email String studentEmail) {

        adminService.addStudentToTuitionClass(studentEmail, classId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/tuition/{tuitionId}/student/{studentId}")
    public ResponseEntity<Void> removeStudentFromTuition(
            @PathVariable @Positive int tuitionId,
            @PathVariable @Positive int studentId) {

        adminService.removeStudentFromTuition(tuitionId, studentId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/class/{classId}/student/{studentId}")
    public ResponseEntity<Void> removeStudentFromClass(
            @PathVariable @Positive int classId,
            @PathVariable @Positive int studentId) {

        adminService.removeStudentFromClass(classId, studentId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/class/{classId}/teacher/{teacherEmail}")
    public ResponseEntity<Void> addTeacherToClass(
            @PathVariable @Positive int classId,
            @PathVariable @Email String teacherEmail) {

        adminService.addTeacherToTuitionClass(teacherEmail, classId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/class/{classId}/teachers/{teacherId}")
    public ResponseEntity<Void> removeTeacherFromClass(
            @PathVariable @Positive int classId,
            @PathVariable @Positive int teacherId) {

        adminService.removeTeacherFromClass(teacherId, classId);
        return ResponseEntity.noContent().build();
    }

    /* --------- Subject / Course CRUD (scoped) --------- */

    @GetMapping("/tuition/{tuitionId}/subject")
    public ResponseEntity<List<Subject2ResponseDto>> getAllSubjectsOfTuition(@PathVariable @Positive int tuitionId) {

        List<Subject2> subjects = adminService.getAllSubjectsOfTuition(tuitionId);

        List<Subject2ResponseDto> response = subjects.stream().map(mapObjects::mapSubjectResponse).toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/subject/{subjectId}")
    public ResponseEntity<Subject2ResponseDto> getSubjectById(@PathVariable @Positive int subjectId) {

        Subject2 subject = adminService.getSubjectById(subjectId);

        Subject2ResponseDto response = mapObjects.mapSubjectResponse(subject);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping(value = "/subject/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Subject2ResponseDto> createSubjectWithFile(
            @RequestParam("subjectName") String subjectName,
            @RequestParam("tuitionClassId") int tuitionClassId,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        // Convert to DTO manually if needed
        Subject2Dto dto = new Subject2Dto();
        dto.setSubjectName(subjectName);
        dto.setTuitionClassId(tuitionClassId);
        dto.setFiles(files);

        Subject2 created = adminService.createSubject(dto);
        Subject2ResponseDto response = mapObjects.mapSubjectResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/subject")
    public ResponseEntity<Subject2ResponseDto> createSubject(@RequestBody Subject2Dto dto) {
        Subject2 created = adminService.createSubject(dto);
        Subject2ResponseDto response = mapObjects.mapSubjectResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/subject/{subjectId}")
    public ResponseEntity<Subject2ResponseDto> updateSubject(
            @PathVariable @Positive int subjectId,
            @Valid @RequestBody Subject2UpdateDto subjectDto) {

        Subject2 updated = adminService.updateSubject(subjectId, subjectDto);

        Subject2ResponseDto response = mapObjects.mapSubjectResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/subject/{subjectId}")
    public ResponseEntity<Void> deleteSubject(@PathVariable @Positive int subjectId) {
        adminService.deleteSubject(subjectId);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/tuition/{tuitionId}/course")
    public ResponseEntity<List<CourseResponseDto>> getAllCoursesOfTuition(@PathVariable @Positive int tuitionId) {

        List<Course2> courses = adminService.getAllCoursesOfTuition(tuitionId);

        List<CourseResponseDto> response = courses.stream().map(mapObjects::mapCourseResponse).toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PostMapping(value = "/tuition/{tuitionId}/course", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseResponseDto> createCourse(
            @PathVariable @Positive int tuitionId,
            @RequestParam("courseName") String courseName,
            @RequestParam("courseDuration") String courseDuration,
            @RequestParam(value = "subjectIds", required = false) String subjectIds,
            @RequestPart(value = "resources", required = false) List<MultipartFile> resources) {


        //converting comma separated string of subject ids into list

        List<Integer> list;

        if (subjectIds != null) {
            list = Arrays.stream(subjectIds.split(",")).map(Integer::parseInt).toList();
        } else {
            list = new ArrayList<>();
        }

        // Manually create DTO
        Course2Dto dto = new Course2Dto();
        dto.setCourseName(courseName);
        dto.setCourseDuration(courseDuration);
        dto.setSubjectIds(list);
        dto.setTuitionId(tuitionId);
        dto.setResources(resources);

        // Call service
        Course2 created = adminService.createCourse(tuitionId, dto);
        CourseResponseDto response = mapObjects.mapCourseResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PutMapping("/course/{courseId}")
    public ResponseEntity<CourseResponseDto> updateCourse(
            @PathVariable @Positive int courseId,
            @Valid @RequestBody Course2UpdateDto payload) {

        Course2 updated = adminService.updateCourse(courseId, payload);

        CourseResponseDto response = mapObjects.mapCourseResponse(updated);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<Void> deleteCourse(@PathVariable @Positive int courseId) {
        adminService.deleteCourse(courseId);
        return ResponseEntity.noContent().build();
    }

    /* --------- Attendance (view) --------- */

    @GetMapping("/tuition/{tuitionId}/attendance")
    public ResponseEntity<List<Attendance>> getAttendanceForTuition(
            @PathVariable @Positive int tuitionId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        // Admin can view attendance across its tuitions - your admin service may need a method to fetch
        // For now, fetch student and aggregate; or implement a dedicated adminService.getAttendanceForTuition(...)
        throw new UnsupportedOperationException("Implement adminViewAttendance in service if needed");
    }


    /// /////////  Resources ////////////////

    @PostMapping("/folder/create")
    public ResponseEntity<ResourceFolderResponse> createFolder(
            @RequestBody ResourceFolderRequest request
    ) {
        ResourceFolder2 folder = adminService.createFolder2(request);
        return ResponseEntity.ok(mapObjects.mapFolderResponse(folder));
    }


    @PostMapping(
            value = "/resource/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResourceResponse> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam("tuitionId") int tuitionId,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "folderId", required = false) Integer folderId
    ) throws Exception {

        Resource2 resource = adminService.uploadResource(
                file, tuitionId, courseId, subjectId, folderId
        );

        return ResponseEntity.ok(mapObjects.mapResourceResponse2(resource));
    }


    @GetMapping("/resource/{resourceId}/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @PathVariable int resourceId
    ) {
        String url = adminService.getPresignedUrl(resourceId);
        return ResponseEntity.ok(new PresignedUrlResponse(url));
    }

    @GetMapping("/browse")
    public ResponseEntity<FolderBrowseResponse> browse(
            @RequestParam("tuitionId") int tuitionId,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "folderId", required = false) Integer folderId
    ) {
        return ResponseEntity.ok(
                adminService.browse(tuitionId, courseId, subjectId, folderId)
        );
    }

    @DeleteMapping("/resource/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable int resourceId
    ) {
        adminService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/folder/{folderId}")
    public ResponseEntity<Void> deleteFolder(
            @PathVariable int folderId
    ) {
        adminService.deleteFolder(folderId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/folder/{folderId}/rename")
    public ResponseEntity<Void> renameFolder(
            @PathVariable int folderId,
            @RequestParam("name") String newName
    ) {
        adminService.renameFolder(folderId, newName);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/resource/{resourceId}/rename")
    public ResponseEntity<Void> renameResource(
            @PathVariable int resourceId,
            @RequestParam("name") String newName
    ) {
        adminService.renameResource(resourceId, newName);
        return ResponseEntity.ok().build();
    }


    /* Subscription */

    @GetMapping("/subscription/plans")
    public ResponseEntity<List<SubscriptionPlanResponse>> getSubscriptionPlans() {
        List<SubscriptionPlan> plans = adminService.getSubscriptionPlans();

        return ResponseEntity.ok(plans.stream().map(mapObjects::mapSubscriptionResponse).toList());
    }

    @GetMapping("/subscription/current")
    public ResponseEntity<SubscriptionAdminResponse> getCurrentSubscriptions(){
        return  ResponseEntity.ok(adminService.getCurrentSubscription());
    }

}
