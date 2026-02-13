package in.ashar.mooble.controller;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.service.Admin2Service;
import in.ashar.mooble.service.Student2Service;
import in.ashar.mooble.utility.message.MapObjects;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/student")
@PreAuthorize("hasRole('STUDENT')")
@RequiredArgsConstructor
public class StudentController {

    private final Student2Service studentService;
    private final Admin2Service adminService;
    private final MapObjects mapObjects;

    /* Profile */

    @GetMapping("/profile")
    public ResponseEntity<Student2ResponseDto> myProfile() {
        Student2 s = studentService.getProfile();
        Student2ResponseDto response = mapObjects.mapStudentResponse(s);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile/{studentId}")
    public ResponseEntity<Student2ResponseDto> updateProfile(
            @PathVariable @Positive int studentId,
            @Valid @RequestBody Student2UpdateDto dto) {

        Student2 updated = adminService.updateStudent(studentId, dto);
        Student2ResponseDto response = mapObjects.mapStudentResponse(updated);
        return ResponseEntity.ok(response);

    }



    /* Assignments & Submissions */

    @PostMapping("/assignment")
    public ResponseEntity<SubmissionResponseDto> submitAssignment(@RequestBody SubmissionRequestDto requestDto){
        Submission submission = studentService.submitAssignment(requestDto);

        SubmissionResponseDto response = mapObjects.mapSubmissionResponse(submission);

        return ResponseEntity.ok(response);

    }

    @GetMapping("/assignment")
    public ResponseEntity<List<AssignmentResponseDto>> myAssignments() {
        List<Assignment> assignments = studentService.getMyAssignments();
        List<AssignmentResponseDto> response = assignments.stream().map(mapObjects::mapAssignmentResponse).toList();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/assignment/subject/{subjectId}")
    public ResponseEntity<List<AssignmentResponseDto>> getAssignmentsBySubjectId(@PathVariable int subjectId) {
        List<Assignment> assignments = studentService.getAssignmentsBySubjectId(subjectId);
        List<AssignmentResponseDto> response = assignments.stream().map(mapObjects::mapAssignmentResponse).toList();
        return ResponseEntity.ok(response);
    }

    /* Find Tuition */

    @GetMapping("/find/{tuitionCode}")
    public ResponseEntity<TuitionDetails> findTuitionByCode(@PathVariable int tuitionCode){

        TuitionDetails tuitionDetails = studentService.findTuitionByTuitionCode(tuitionCode);

        return ResponseEntity.ok(tuitionDetails);
    }

    /* Request Tuition */

    @GetMapping("/join/{tuitionCode}")
    public ResponseEntity<JoinRequestTuition> requestToJoin(@PathVariable int tuitionCode){

        JoinRequestTuition joinRequestTuition = studentService.requestToJoin(tuitionCode);

        return ResponseEntity.ok(joinRequestTuition);
    }

    @GetMapping("/re-join/{requestId}")
    public ResponseEntity<JoinRequestTuition> reRequestToJoin(@PathVariable int requestId){

        JoinRequestTuition joinRequestTuition = studentService.reRequestToJoin(requestId);

        return ResponseEntity.ok(joinRequestTuition);
    }

    @GetMapping("/join-status/{tuitionCode}")
    public ResponseEntity<JoinRequestTuition> requestJoinStatus(@PathVariable int tuitionCode){

        JoinRequestTuition joinRequestTuition = studentService.requestStatus(tuitionCode);

        return ResponseEntity.ok(joinRequestTuition);
    }
    

    /* Tuition and Tuition Classes */

    @GetMapping("/my-tuition")
    public ResponseEntity<List<Tuition2ResponseDto>> myTuition(){
        List<Tuition2> tuition = studentService.getMyTuition();
        List<Tuition2ResponseDto> response = tuition.stream().map(mapObjects::mapTuitionResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my-tuition-classes")
    public ResponseEntity<List<TuitionClassResponseDto>> myTuitionClasses(){
        List<TuitionClass> tuitionClasses = studentService.getMyTuitionClasses();
        List<TuitionClassResponseDto> response = tuitionClasses.stream().map(mapObjects::mapTuitionClassResponse).toList();
        return ResponseEntity.ok(response);
    }

    /* Subjects and Courses */

    @GetMapping("/my-subjects")
    public ResponseEntity<List<Subject2ResponseDto>> mySubjects(){
        List<Subject2> subjects = studentService.getMySubjects();
        List<Subject2ResponseDto> response = subjects.stream().map(mapObjects::mapSubjectResponse).toList();
        return ResponseEntity.ok(response);
    }


    @GetMapping("/my-courses")
    public ResponseEntity<List<CourseResponseDto>> myCourses(){
        List<Course2> courses = studentService.getMyCourses();
        List<CourseResponseDto> response = courses.stream().map(mapObjects::mapCourseResponse).toList();
        return ResponseEntity.ok(response);
    }



    /* Resources */
    @GetMapping("/my-resources")
    public ResponseEntity<List<ResourceResponseDto>> getAllResources() {
        List<Resource> allResources = studentService.getAllResources();
        List<ResourceResponseDto> list = allResources.stream()
                .map(mapObjects::mapResourceResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/my-folders")
    public ResponseEntity<List<ResourceFolderDto>> getAllFolders() {
        List<ResourceFolder> folders = studentService.myFolders();
        List<ResourceFolderDto> list = folders.stream()
                .map(mapObjects::mapResourceFolderDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/resources/subject/{subjectId}")
    public ResponseEntity<List<ResourceResponseDto>> getResourcesBySubject(@PathVariable int subjectId) {
        List<Resource> subjectResources = studentService.getResourcesBySubjectId(subjectId);

        List<ResourceResponseDto> list = subjectResources.stream()
                .map(mapObjects::mapResourceResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/resources/course/{courseId}")
    public ResponseEntity<List<ResourceResponseDto>> getResourcesByCourse(@PathVariable int courseId) {
        List<Resource> courseResources = studentService.getResourcesByCourseId(courseId);

        List<ResourceResponseDto> list = courseResources.stream()
                .map(mapObjects::mapResourceResponse)
                .toList();
        return ResponseEntity.ok(list);
    }


    @GetMapping("/resources/folder/{folderId}")
    public ResponseEntity<List<ResourceResponseDto>> getResourcesByFolder(@PathVariable int folderId) {
        List<Resource> folderResources = studentService.getResourcesByFolderId(folderId);

        List<ResourceResponseDto> list = folderResources.stream()
                .map(mapObjects::mapResourceResponse)
                .toList();
        return ResponseEntity.ok(list);
    }


    @GetMapping("/folders/subject/{subjectId}")
    public ResponseEntity<List<ResourceFolderDto>> getFoldersBySubject(@PathVariable int subjectId) {
        List<ResourceFolder> folders = studentService.getFoldersBySubjectId(subjectId);

        List<ResourceFolderDto> list = folders.stream()
                .map(mapObjects::mapResourceFolderDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/folders/course/{courseId}")
    public ResponseEntity<List<ResourceFolderDto>> getFoldersByCourses(@PathVariable int courseId) {
        List<ResourceFolder> folders = studentService.getFoldersByCourseId(courseId);

        List<ResourceFolderDto> list = folders.stream()
                .map(mapObjects::mapResourceFolderDto)
                .toList();
        return ResponseEntity.ok(list);
    }

    /* New Resources */

    @GetMapping("/resource/{resourceId}/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
            @PathVariable int resourceId
    ) {
        String url = studentService.getPresignedUrl(resourceId);
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
                studentService.browse(tuitionId, courseId, subjectId, folderId)
        );
    }


    /* ATTENDANCE */
    @PostMapping("/attendance")
    public ResponseEntity<StudentAttendanceResponse> getMyAttendance( @RequestBody StudentAttendanceRequest request) {
        return ResponseEntity.ok( studentService.getStudentAttendance( request));
    }


}
