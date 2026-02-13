package in.ashar.mooble.controller;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.service.Admin2Service;
import in.ashar.mooble.service.SubmissionService;
import in.ashar.mooble.service.Teacher2Service;
import in.ashar.mooble.utility.message.MapObjects;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/teacher")
@PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
@RequiredArgsConstructor
public class TeacherController {

    private final MapObjects mapObjects;
    private final Admin2Service adminService;
    private final Teacher2Service teacherService;
    private final SubmissionService submissionService;


    /* Profile */

    @GetMapping("/profile")
    public ResponseEntity<TeacherResponseDto> myProfile() {
        Teacher2 t = teacherService.getProfile();
        TeacherResponseDto response = mapObjects.mapTeacherResponse(t);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/{teacherId}")
    public ResponseEntity<TeacherResponseDto> updateProfile(@PathVariable @Positive int teacherId, @Valid @RequestBody Teacher2UpdateDto incoming) {
        Teacher2 updated = adminService.updateTeacher(teacherId, incoming);
        return ResponseEntity.ok(mapObjects.mapTeacherResponse(updated));
    }

    @DeleteMapping("/profile")
    public ResponseEntity<Void> deleteProfile(){
        teacherService.deleteMyProfile();
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/remove/class/{classId}")
    public ResponseEntity<Void> removeFromClass(@PathVariable @Positive int classId){

        teacherService.removeFromClass(classId);
        return ResponseEntity.noContent().build();

    }


    @GetMapping("/class")
    public ResponseEntity<List<TuitionClassResponseDto>> myClasses() {

        List<TuitionClass> tuitionClasses = teacherService.getAssignedTuitionClasses();
        List<TuitionClassResponseDto> list = tuitionClasses.stream().map(mapObjects::mapTuitionClassResponse).toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tuition")
    public ResponseEntity<List<Tuition2ResponseDto>> myTuition(){
        List<Tuition2> tuition = teacherService.getAssignedTuition();

        List<Tuition2ResponseDto> response = tuition.stream().map(mapObjects::mapTuitionResponse).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/class/{classId}/student")
    public ResponseEntity<List<Student2ResponseDto>> studentsInClass(@PathVariable @Positive int classId) {
        List<Student2> students = teacherService.getStudentsInTuitionClass(classId);

        List<Student2ResponseDto> response = students.stream().map(mapObjects::mapStudentResponse).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/subject/{subjectId}/student")
    public ResponseEntity<List<Student2ResponseDto>> studentsInSubject(@PathVariable @Positive int subjectId) {
        List<Student2> students = teacherService.getStudentsInSubject(subjectId);

        List<Student2ResponseDto> response = students.stream().map(mapObjects::mapStudentResponse).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/course/{courseId}/student")
    public ResponseEntity<List<Student2ResponseDto>> studentsInCourse(@PathVariable @Positive int courseId) {
        List<Student2> students = teacherService.getStudentsInCourse(courseId);

        List<Student2ResponseDto> response = students.stream().map(mapObjects::mapStudentResponse).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/class/{classId}/teacher")
    public ResponseEntity<List<TeacherResponseDto>> teachersInClass(@PathVariable @Positive int classId) {
        List<Teacher2> teachers = teacherService.getTeachersInTuitionClass(classId);

        List<TeacherResponseDto> response = teachers.stream().map(mapObjects::mapTeacherResponse).toList();

        return ResponseEntity.ok(response);
    }


    @GetMapping("/subject")
    public ResponseEntity<List<Subject2ResponseDto>> mySubjects() {

        List<Subject2> subjects = teacherService.getAssignedSubjects();
        List<Subject2ResponseDto> response = subjects.stream().map(mapObjects::mapSubjectResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/course")
    public ResponseEntity<List<CourseResponseDto>> myCourses() {

        List<Course2> courses = teacherService.getAssignedCourses();
        List<CourseResponseDto> response = courses.stream().map(mapObjects::mapCourseResponse).toList();
        return ResponseEntity.ok(response);
    }


    /* Find Tuition */

    @GetMapping("/find/{tuitionCode}")
    public ResponseEntity<TuitionDetails> findTuitionByCode(@PathVariable int tuitionCode){

        TuitionDetails tuitionDetails = teacherService.findTuitionByTuitionCode(tuitionCode);

        return ResponseEntity.ok(tuitionDetails);
    }

    /* Request Tuition */

    @GetMapping("/join/{tuitionCode}")
    public ResponseEntity<JoinRequestTuition> requestToJoin(@PathVariable int tuitionCode){

        JoinRequestTuition joinRequestTuition = teacherService.requestToJoin(tuitionCode);

        return ResponseEntity.ok(joinRequestTuition);
    }

    @GetMapping("/re-join/{requestId}")
    public ResponseEntity<JoinRequestTuition> reRequestToJoin(@PathVariable int requestId){

        JoinRequestTuition joinRequestTuition = teacherService.reRequestToJoin(requestId);

        return ResponseEntity.ok(joinRequestTuition);
    }

    @GetMapping("/join-status/{tuitionCode}")
    public ResponseEntity<JoinRequestTuition> requestJoinStatus(@PathVariable int tuitionCode){

        JoinRequestTuition joinRequestTuition = teacherService.requestStatus(tuitionCode);

        return ResponseEntity.ok(joinRequestTuition);
    }

    /* Assignments */

    @GetMapping("/assignment/teacher")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsOfTeacher(){
        List<Assignment> assignments =  teacherService.getAssignmentsOfTeacher();

        List<AssignmentResponseDto> response = assignments.stream().map(mapObjects::mapAssignmentResponse).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignment/subject/{subjectId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentBySubjectId(@PathVariable int subjectId){
        List<Assignment> assignments =  teacherService.getAssignmentsBySubjectId(subjectId);

        List<AssignmentResponseDto> response = assignments.stream().map(mapObjects::mapAssignmentResponse).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignment/course/{courseId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentByCourseId(@PathVariable int courseId){

        List<Assignment> assignments =  teacherService.getAssignmentsByCourseId(courseId);

        List<AssignmentResponseDto> response = assignments.stream().map(mapObjects::mapAssignmentResponse).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/assignment")
    public ResponseEntity<AssignmentResponseDto> createAssignment(
            @Valid @RequestBody AssignmentRequestDto assignmentDto) {

        Assignment created = teacherService.createAssignment(assignmentDto);

        AssignmentResponseDto response = mapObjects.mapAssignmentResponse(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/assignment/{assignmentId}")
    public ResponseEntity<AssignmentResponseDto> updateAssignment(
            @PathVariable int assignmentId,
            @Valid @RequestBody AssignmentUpdateDto payload) {

        Assignment updated = teacherService.updateAssignment(assignmentId, payload);
        AssignmentResponseDto response = mapObjects.mapAssignmentResponse(updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/assignment/{assignmentId}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable int assignmentId) {
        teacherService.deleteAssignment(assignmentId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/assignment/{assignmentId}/submission")
    public ResponseEntity<List<SubmissionResponseDto>> viewSubmissions(@PathVariable int assignmentId) {
        List<Submission> subs = submissionService.getSubmissionByAssignmentId(assignmentId);
        List<SubmissionResponseDto> response = subs.stream().map(mapObjects::mapSubmissionResponse).toList();
        return ResponseEntity.ok(response);
    }


    @PostMapping("/submission/grade")
    public ResponseEntity<SubmissionResponseDto> gradeSubmission(@Valid @RequestBody GradeRequestDto requestDto) {

        Submission submission = submissionService.gradeSubmission(requestDto);
        SubmissionResponseDto response = mapObjects.mapSubmissionResponse(submission);
        return ResponseEntity.ok(response);
    }

    /* Resources */

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

        Resource2 resource = teacherService.uploadResource(
                file, tuitionId, courseId, subjectId, folderId
        );

        return ResponseEntity.ok(mapObjects.mapResourceResponse2(resource));
    }


    @GetMapping("/resource/{resourceId}/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @PathVariable int resourceId
    ) {
        String url = teacherService.getPresignedUrl(resourceId);
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
                teacherService.browse(tuitionId, courseId, subjectId, folderId)
        );
    }


    @DeleteMapping("/resource/{resourceId}")
    public ResponseEntity<Void> deleteResource(
            @PathVariable int resourceId
    ) {
        teacherService.deleteResource(resourceId);
        return ResponseEntity.noContent().build();
    }


    @PatchMapping("/resource/{resourceId}/rename")
    public ResponseEntity<Void> renameResource(
            @PathVariable int resourceId,
            @RequestParam("name") String newName
    ) {
        teacherService.renameResource(resourceId, newName);
        return ResponseEntity.ok().build();
    }

    /* Attendance: teacher marks attendance for a class */
//    @PostMapping("/subject/{subjectId}/attendance")
//    public ResponseEntity<AttendanceResponseDto> markAttendance( @PathVariable @Positive int subjectId, @RequestBody List<Integer> studentIds) {
//
//        Attendance a = teacherService.markAttendance(subjectId, studentIds);
//
//        AttendanceResponseDto response = mapper.mapAttendanceResponse(a);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
//
//
//    @PutMapping("/attendance/{attendanceId}")
//    public ResponseEntity<AttendanceResponseDto> UpdateAttendance( @PathVariable @Positive int attendanceId, @RequestBody List<Integer> studentIds) {
//
//        Attendance a = teacherService.updateAttendance(attendanceId, studentIds);
//
//        AttendanceResponseDto response = mapper.mapAttendanceResponse(a);
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(response);
//    }
}
