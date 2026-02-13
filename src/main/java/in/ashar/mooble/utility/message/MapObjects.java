package in.ashar.mooble.utility.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.ashar.mooble.configuration.AppProperties;
import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.repository.Student2Repository;
import in.ashar.mooble.repository.Subject2Repository;
import in.ashar.mooble.repository.Teacher2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MapObjects {


    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final Subject2Repository subjectRepository;
    private final Student2Repository studentRepository;
    private final Teacher2Repository teacherRepository;

    public Admin2ResponseDto mapAdminResponse(Admin2 admin) {


        Admin2ResponseDto response = new Admin2ResponseDto();

        response.setAdminId(admin.getAdminId());
        response.setAdminName(admin.getAdminName());
        response.setAdminEmail(admin.getAdminEmail());
        response.setAdminPhoneNumber(admin.getAdminPhoneNumber());
        response.setAdminAddress(admin.getAdminAddress());


        if (admin.getAdminTuition() != null && !admin.getAdminTuition().isEmpty()) {

            List<Tuition2ResponseDtoAdmin> trList = new ArrayList<>();

            for (Tuition2 tuition : admin.getAdminTuition()) {
                Tuition2ResponseDtoAdmin tr = new Tuition2ResponseDtoAdmin();
                tr.setTuitionId(tuition.getTuitionId());
                tr.setTuitionName(tuition.getTuitionName());
                tr.setBranch(tuition.getBranch());
                trList.add(tr);
            }

            response.setAdminTuition(trList);
        }

        return response;
    }


    public Tuition2ResponseDto mapTuitionResponse(Tuition2 tuition) {

        Tuition2ResponseDto response = Tuition2ResponseDto.builder()
                .tuitionId(tuition.getTuitionId())
                .tuitionCode(tuition.getTuitionCode())
                .tuitionName(tuition.getTuitionName())
                .tuitionEmail(tuition.getTuitionEmail())
                .tuitionPhoneNumber(tuition.getTuitionPhoneNumber())
                .tuitionAddress(tuition.getTuitionAddress())
                .branch(tuition.getBranch())
                .build();

        //map tuition class and admin
        Admin2 admin = tuition.getTuitionAdmin();
        response.setAdminId(admin.getAdminId());
        response.setAdminName(admin.getAdminName());
        response.setAdminEmail(admin.getAdminEmail());

        List<TuitionClass> tuitionClasses = tuition.getTuitionClasses();

        if (tuitionClasses != null && !tuitionClasses.isEmpty()) {
            List<Integer> tuitionClassList = tuitionClasses.stream()
                    .map(TuitionClass::getTuitionClassId)
                    .toList();
            response.setTuitionClassIds(tuitionClassList);
        }
        return response;

    }

    public Student2ResponseDto mapStudentResponse(Student2 student) {

        List<Subject2> subjects = student.getSubjects();
        List<Map<Integer, Integer>> classIdsAndSubjectIds = subjects.stream().map(s -> Map.of(s.getTuitionClass().getTuitionClassId(), s.getSubjectId())).toList();

        List<Course2> courses = student.getCourses();
        List<Map<Integer, Integer>> tuitionIdsAndCourseIds = courses.stream().map(c -> Map.of(c.getTuition().getTuitionId(), c.getCourseId())).toList();

        List<Map<Integer, LocalDateTime>> classIdsAndJoinedDate =
                student.getClassJoined()
                        .stream()
                        .map(cj -> Map.of(cj.getTuitionId(), cj.getJoined()))
                        .toList();


        ParentsDetail pd = student.getParentsDetail();
        ParentsDetailDto parentsDetailDto = ParentsDetailDto.builder()
                .name(pd.getName())
                .relation(pd.getRelation())
                .address(pd.getAddress())
                .phone(pd.getPhone())
                .occupation(pd.getOccupation())
                .build();


        return Student2ResponseDto.builder()
                .studentId(student.getStudentId())
                .studentName(student.getStudentName())
                .studentEmail(student.getStudentCredential().getEmail())
                .studentAddress(student.getStudentAddress())
                .studentPhoneNumber(student.getStudentPhoneNumber())
                .dateOfBirth(student.getBirthDate())
                .parentsDetail(parentsDetailDto)
                .joined(student.getJoined())
                .classIdsAndSubjectIds(classIdsAndSubjectIds)
                .tuitionIdsAndCourseIds(tuitionIdsAndCourseIds)
                .tuitionIdsAndJoinedDate(classIdsAndJoinedDate)
                .build();
    }

    public TeacherResponseDto mapTeacherResponse(Teacher2 teacher) {

        List<Subject2> subjects = teacher.getSubjects();
        List<Map<Integer, Integer>> classIdsAndSubjectIds = subjects.stream().map(s -> Map.of(s.getTuitionClass().getTuitionClassId(), s.getSubjectId())).toList();

        List<Course2> courses = teacher.getCourses();
        List<Map<Integer, Integer>> tuitionIdsAndCourseIds = courses.stream().map(c -> Map.of(c.getTuition().getTuitionId(), c.getCourseId())).toList();

        List<Map<Integer, LocalDateTime>> classIdsAndJoinedDate =
                teacher.getClassJoined()
                        .stream()
                        .map(cj -> Map.of(cj.getTuitionId(), cj.getJoined()))
                        .toList();

        return TeacherResponseDto.builder()
                .teacherId(teacher.getTeacherId())
                .teacherName(teacher.getTeacherName())
                .teacherEmail(teacher.getTeacherCredential().getEmail())
                .teacherAddress(teacher.getTeacherAddress())
                .teacherPhoneNumber(teacher.getTeacherPhoneNumber())
                .dateOfBirth(teacher.getBirthDate())
                .experience(teacher.getExperience())
                .knownSubjects(teacher.getKnownSubjects())
                .joined(teacher.getJoined())
                .classIdsAndSubjectIds(classIdsAndSubjectIds)
                .tuitionIdsAndCourseIds(tuitionIdsAndCourseIds)
                .tuitionIdsAndJoinedDate(classIdsAndJoinedDate)
                .build();

    }


    public TuitionClassResponseDto mapTuitionClassResponse(TuitionClass tuitionClass) {

        TuitionClassResponseDto response = objectMapper.convertValue(tuitionClass, TuitionClassResponseDto.class);

        Tuition2 tuition = tuitionClass.getTuition();
        List<Teacher2> teachers = tuitionClass.getTeachers();
        List<Student2> students = tuitionClass.getStudents();
        List<Subject2> subjects = tuitionClass.getSubjects();
//        List<Course2> courses = tuitionClass.getCourses();

        response.setTuitionId(tuition.getTuitionId());
        response.setTuitionName(tuition.getTuitionName());

        List<Map<Integer, String>> teachersMap = teachers.stream().map(teacher -> Map.of(teacher.getTeacherId(), teacher.getTeacherName())).toList();
        response.setTeachers(teachersMap);

        List<Map<Integer, String>> studentMap = students.stream().map(student -> {
                    System.out.println(student.getStudentId() + " " + student.getStudentName());
                    return Map.of(student.getStudentId(), student.getStudentName());
                }
        ).toList();

        response.setStudents(studentMap);

        List<Map<Integer, String>> subjectMap = subjects.stream().map(subject -> Map.of(subject.getSubjectId(), subject.getSubjectName())).toList();
        response.setSubjects(subjectMap);


//        List<Map<Integer,String>> courseMap = courses.stream().map(course -> Map.of(course.getCourseId(), course.getCourseName())).toList();
//        response.setCourses(courseMap);

        return response;

    }

    public Student2 mapStudent(Student2Dto student2Dto) {

        Student2 student = objectMapper.convertValue(student2Dto, Student2.class);
        student.setBirthDate(student2Dto.getDateOfBirth());
        if (student2Dto.getSubjectIds() != null) {
            List<Subject2> subjects = subjectRepository.findAllById(student2Dto.getSubjectIds());
            student.setSubjects(subjects);
        }

        return student;

        /// credentials and tuition classes haven't mapped. (means do something with email, standard and section)
    }


    public Subject2ResponseDto mapSubjectResponse(Subject2 subject) {

        Subject2ResponseDto response = new Subject2ResponseDto();

        response.setSubjectId(subject.getSubjectId());
        response.setSubjectName(subject.getSubjectName());
        response.setStandard(subject.getTuitionClass().getStandard().name());
        response.setSection(subject.getTuitionClass().getSection());

        List<Integer> studentIds = studentRepository.findAllBySubjectsContaining(subject).stream().map(Student2::getStudentId).toList();
        response.setStudentIds(studentIds);

        List<Integer> teacherIds = teacherRepository.findAllBySubjectsContaining(subject).stream().map(Teacher2::getTeacherId).toList();
        response.setTeacherIds(teacherIds);

        response.setResourceIds(subject.getResources().stream().map(Resource::getResourceId).toList());
        response.setTuitionClassId(subject.getTuitionClass().getTuitionClassId());

        return response;

    }


    public ResourceResponseDto mapResourceResponse(Resource resource) {


        // Generate view URL dynamically
        String viewUrl = appProperties.getBaseUrl() + "/api/resource/" + resource.getResourceId() + "/view";

        ResourceResponseDto response = new ResourceResponseDto();

        response.setResourceId(resource.getResourceId());
        response.setName(resource.getName());
        response.setType(resource.getType());
        response.setFileName(resource.getFileName());
        response.setUploadedAt(resource.getUploadedAt());
        response.setUploaderId(resource.getUploaderId());

        if (resource.getSubject() != null) {
            response.setSubjectId(resource.getSubject().getSubjectId());
        }

        if (resource.getCourse() != null) {
            response.setCourseId(resource.getCourse().getCourseId());
        }

        if (resource.getTuition() != null) {
            response.setTuitionId(resource.getTuition().getTuitionId());
        }

        if (resource.getFolder() != null) {
            response.setFolderId(resource.getFolder().getFolderId());
        }

        response.setUrl(viewUrl);

        return response;
    }

    public ResourceFolderDto mapResourceFolderDto(ResourceFolder folder) {

        ResourceFolderDto folderDto = new ResourceFolderDto();

        folderDto.setName(folder.getName());
        folderDto.setFolderId(folder.getFolderId());
        if (folder.getParentFolder() != null) {
            folderDto.setParentFolderId(folder.getParentFolder().getFolderId());
        }
        List<ResourceResponseDto> resourceResponseDtos = folder.getResources().stream().map(this::mapResourceResponse).toList();
        folderDto.setResources(resourceResponseDtos);

        return folderDto;

    }

//    public AttendanceResponseDto mapAttendanceResponse(Attendance attendance) {
//
//        AttendanceResponseDto response = objectMapper.convertValue(attendance, AttendanceResponseDto.class);
//
//        response.setSubjectId(attendance.getSubject().getSubjectId());
//        response.setSubjectName(attendance.getSubject().getSubjectName());
//
//        response.setStudentIds(attendance.getStudents().stream().map(Student2::getStudentId).toList());
//        response.setStudentNames(attendance.getStudents().stream().map(Student2::getStudentName).toList());
//
//        response.setTeacherId(attendance.getTeacher().getTeacherId());
//        response.setTeacherName(attendance.getTeacher().getTeacherName());
//
//        return response;
//    }

    public CourseResponseDto mapCourseResponse(Course2 course) {

        CourseResponseDto response = objectMapper.convertValue(course, CourseResponseDto.class);

        response.setTuitionId(course.getTuition().getTuitionId());
        if (course.getResources() != null && !course.getResources().isEmpty()) {
            response.setResourceIds(course.getResources().stream().map(Resource::getResourceId).toList());
        }

        return response;
    }


    public Course2 mapCourse(Course2Dto courseDto) {

        return Course2.builder()
                .courseName(courseDto.getCourseName())
                .courseDuration(courseDto.getCourseDuration())
                .subjectIds(courseDto.getSubjectIds())
                .build();

    }

    public AssignmentResponseDto mapAssignmentResponse(Assignment assignment) {

        return AssignmentResponseDto.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .assignedDate(assignment.getAssignedDate())
                .dueDate(assignment.getDueDate())
                .maxMarks(assignment.getMaxMarks())
                .resourceIds(assignment.getResourceIds())
                .teacherId(assignment.getTeacher().getTeacherId())
                .teacherName(assignment.getTeacher().getTeacherName())
                .subjectId(assignment.getSubject() != null ? assignment.getSubject().getSubjectId() : 0)
                .subjectName(assignment.getSubject() != null ? assignment.getSubject().getSubjectName() : "")
                .courseId(assignment.getCourse() != null ? assignment.getCourse().getCourseId() : 0)
                .courseName(assignment.getCourse() != null ? assignment.getCourse().getCourseName() : "")
                .build();

    }

    public SubmissionResponseDto mapSubmissionResponse(Submission submission) {

        SubmissionResponseDto response = new SubmissionResponseDto();

        response.setSubmissionId(submission.getSubmissionId());
        response.setStatus(submission.getStatus().name());
        response.setStudentId(submission.getStudent().getStudentId());
        response.setStudentName(submission.getStudent().getStudentName());
        response.setResourceIds(submission.getResourceIds());
        response.setSubmittedOn(submission.getSubmittedOn());
        response.setFeedback(submission.getFeedback());
        response.setMarksObtained(submission.getMarksObtained());

        return response;
    }

    public AssignmentResourceResponseDto mapAssignmentResourceResponse(AssignmentResource resource) {

        AssignmentResourceResponseDto response = new AssignmentResourceResponseDto();

        response.setAssignmentResourceId(resource.getAssignmentResourceId());
        response.setName(resource.getName());
        response.setType(resource.getType());
        response.setFileName(resource.getFileName());
        response.setUploadedAt(resource.getUploadedAt());
        response.setUploaderId(resource.getUploaderId());

        return response;

    }

    public ResourceFolderResponse mapFolderResponse(ResourceFolder2 folder) {
        ResourceFolderResponse dto = new ResourceFolderResponse();
        dto.setFolderId(folder.getFolderId());
        dto.setName(folder.getName());
        dto.setTuitionId(folder.getTuitionId());
        dto.setCourseId(folder.getCourseId());
        dto.setSubjectId(folder.getSubjectId());
        dto.setPath(folder.getPath());

        if (folder.getParentFolder() != null) {
            dto.setParentFolderId(folder.getParentFolder().getFolderId());
        }

        return dto;
    }


    public ResourceResponse mapResourceResponse2(Resource2 resource) {
        ResourceResponse dto = new ResourceResponse();
        dto.setResourceId(resource.getResourceId());
        dto.setName(resource.getName());
        dto.setType(resource.getType());
        dto.setSize(resource.getSize());
        dto.setFileName(resource.getFileName());
        dto.setUploadedAt(resource.getUploadedAt());

        dto.setTuitionId(resource.getTuitionId());
        dto.setCourseId(resource.getCourseId());
        dto.setSubjectId(resource.getSubjectId());

        if (resource.getFolder() != null) {
            dto.setFolderId(resource.getFolder().getFolderId());
        }

        return dto;
    }

    public SessionResponse mapSessionResponse(Session s) {
        SessionResponse r = new SessionResponse();

        r.setId(s.getId());
        r.setDate(s.getDate());
        r.setStartTime(s.getStartTime());
        r.setEndTime(s.getEndTime());

        // ───── Tuition (ALWAYS) ─────
        r.setTuitionId(s.getTuitionClass().getTuition().getTuitionId());
        r.setTuitionName(s.getTuitionClass().getTuition().getTuitionName());

        // ───── Class (ALWAYS) ─────
        r.setTuitionClassId(s.getTuitionClass().getTuitionClassId());
        r.setTuitionClassName(
                s.getTuitionClass().getStandard().name()
                        + " - "
                        + s.getTuitionClass().getSection()
        );

        // ───── Subject / Course (MUTUALLY EXCLUSIVE) ─────
        if (s.getSubject() != null) {
            r.setSubjectId(s.getSubject().getSubjectId());
            r.setSubjectName(s.getSubject().getSubjectName());
        }

        if (s.getCourse() != null) {
            r.setCourseId(s.getCourse().getCourseId());
            r.setCourseName(s.getCourse().getCourseName());
        }

        // ───── Teacher ─────
        r.setTeacherId(s.getTeacher().getTeacherId());
        r.setTeacherName(s.getTeacher().getTeacherName());

        // ───── Status & Recurrence ─────
        r.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
        r.setRecurrenceGroupId(s.getRecurrenceGroupId());

        return r;
    }

    public SubscriptionPlanResponse mapSubscriptionResponse(SubscriptionPlan subscriptionPlan) {

        SubscriptionPlanResponse response = new SubscriptionPlanResponse();
        response.setSubscriptionId(subscriptionPlan.getId());
        response.setPlanName(subscriptionPlan.getName());
        response.setPrice(subscriptionPlan.getPricePerMonth());
        response.setMaxMembers(subscriptionPlan.getMaxMembers());
        response.setMaxStorageMb(subscriptionPlan.getMaxStorageMb());

        return response;
    }


//    public SessionResponse mapSessionResponse(Session s) {
//        SessionResponse r = new SessionResponse();
//        r.setId(s.getId());
//        r.setDate(s.getDate());
//        r.setStartTime(s.getStartTime());
//        r.setEndTime(s.getEndTime());
//         if (s.getSubject() != null) {
//            r.setSubjectId(s.getSubject().getSubjectId());
//            r.setSubjectName(s.getSubject().getSubjectName());
//            r.setTuitionClassId(s.getTuitionClass().getTuitionClassId());
//            r.setTuitionClassName(s.getTuitionClass().getStandard().name() + " - " + s.getTuitionClass().getSection());
//         } else if (s.getCourse() != null) {
//            r.setCourseId(s.getCourse().getCourseId());
//            r.setCourseName(s.getCourse().getCourseName());
//            r.setTuitionId(s.getCourse().getTuition().getTuitionId());
//            r.setTuitionName(s.getCourse().getTuition().getTuitionName());
//        }
//        r.setTeacherId(s.getTeacher().getTeacherId());
//        r.setTeacherName(s.getTeacher().getTeacherName());
//        r.setStatus(s.getStatus() != null ? s.getStatus().name() : null);
//        r.setRecurrenceGroupId(s.getRecurrenceGroupId());
//        return r;
//    }

}
