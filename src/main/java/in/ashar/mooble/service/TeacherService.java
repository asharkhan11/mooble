//package in.ashar.mooble.service;
//
//import in.ashar.mooble.exception.NotFoundException;
//import in.ashar.mooble.security.GetCurrentUser;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class TeacherService {
//
//    private final GetCurrentUser currentUser;
//    private final CourseTeacherRepository courseTeacherRepository;
//    private final CourseStudentRepository courseStudentRepository;
//    private final TuitionUserRepository tuitionUserRepository;
//
//    public TeacherService(GetCurrentUser currentUser, CourseTeacherRepository courseTeacherRepository, CourseStudentRepository courseStudentRepository, TuitionUserRepository tuitionUserRepository) {
//        this.currentUser = currentUser;
//        this.courseTeacherRepository = courseTeacherRepository;
//        this.courseStudentRepository = courseStudentRepository;
//        this.tuitionUserRepository = tuitionUserRepository;
//    }
//
//    public ResponseEntity<List<TuitionUser>> getAllUsersInCourse(Long courseId, String who) {
//
//        String teacherEmail = currentUser.getLoggedInUserEmail();
//
//        Course course = courseTeacherRepository.findCourseByTeacherEmailAndCourseId(teacherEmail, courseId)
//                .orElseThrow(()-> new NotFoundException("Course Not Found"));
//
//        if(who.equals("student")) return ResponseEntity.ok(courseStudentRepository.findAllStudentByCourse(course));
//
//        else return ResponseEntity.ok(courseTeacherRepository.findAllTeacherByCourse(course));
//
//    }
//
//    public ResponseEntity<TuitionUser> getStudentInCourse(Long courseId, Long studentId) {
//        String teacherEmail = currentUser.getLoggedInUserEmail();
//
//        Course course = courseTeacherRepository.findCourseByTeacherEmailAndCourseId(teacherEmail, courseId)
//                .orElseThrow(()-> new NotFoundException("Course Not Found"));
//
//        return ResponseEntity.ok(courseStudentRepository.findStudentById(course,studentId)
//                .orElseThrow(()-> new NotFoundException("Student Not found")));
//    }
//
//
//    public ResponseEntity<List<Tuition>> getAllTuition() {
//        String teacherEmail = currentUser.getLoggedInUserEmail();
//        return ResponseEntity.ok(tuitionUserRepository.findAllTuitionByUserEmail(teacherEmail));
//    }
//
//    public ResponseEntity<Tuition> getTuitionById(Long tuitionId) {
//        String teacherEmail = currentUser.getLoggedInUserEmail();
//        Tuition tuition = tuitionUserRepository.findTuitionById(teacherEmail,tuitionId).orElseThrow(()-> new NotFoundException("Tuition Not Found"));
//        return ResponseEntity.ok(tuition);
//    }
//}
