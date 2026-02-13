//package in.ashar.mooble.service;
//
//import in.ashar.mooble.exception.NotFoundException;
//import in.ashar.mooble.exception.UnAuthorizedException;
//import in.ashar.mooble.security.GetCurrentUser;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class CourseStudentService {
//
//    private final GetCurrentUser getCurrentUser;
//    private final CourseStudentRepository courseStudentRepository;
//    private final CourseRepository courseRepository;
//    private final TuitionUserRepository tuitionUserRepository;
//
//    public CourseStudentService(GetCurrentUser getCurrentUser,
//                                CourseStudentRepository courseStudentRepository,
//                                CourseRepository courseRepository,
//                                TuitionUserRepository tuitionUserRepository) {
//        this.getCurrentUser = getCurrentUser;
//        this.courseStudentRepository = courseStudentRepository;
//        this.courseRepository = courseRepository;
//        this.tuitionUserRepository = tuitionUserRepository;
//    }
//
//    public List<CourseStudent> getAll(Long tuitionId, Long courseId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new NotFoundException("Course not found"));
//
//        // Verify tuition + ownership
//        if (!course.getTuition().getTuitionId().equals(tuitionId) ||
//                !course.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to view students of this course");
//        }
//
//        return courseStudentRepository.findAllByCourse(course);
//    }
//
//    public CourseStudent getById(Long tuitionId, Long courseId, Long courseStudentId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        CourseStudent courseStudent = courseStudentRepository.findById(courseStudentId)
//                .orElseThrow(() -> new NotFoundException("CourseStudent not found"));
//
//        // Verify tuition + ownership
//        if (!courseStudent.getCourse().getTuition().getTuitionId().equals(tuitionId) ||
//                !courseStudent.getCourse().getCourseId().equals(courseId) ||
//                !courseStudent.getCourse().getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to view this course-student relation");
//        }
//
//        return courseStudent;
//    }
//
//    public CourseStudent create(Long tuitionId, Long courseId, Long tuitionUserId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new NotFoundException("Course not found"));
//
//        // Verify tuition + ownership
//        if (!course.getTuition().getTuitionId().equals(tuitionId) ||
//                !course.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to add students to this course");
//        }
//
//        TuitionUser tuitionUser = tuitionUserRepository.findById(tuitionUserId)
//                .orElseThrow(() -> new NotFoundException("TuitionUser not found"));
//
//        // Ensure this TuitionUser actually belongs to the same tuition
//        if (!tuitionUser.getTuition().getTuitionId().equals(tuitionId)) {
//            throw new UnAuthorizedException("This user does not belong to your tuition");
//        }
//
//        // Ensure the role is STUDENT
//        if (tuitionUser.getUser().getRole() == null ||
//                !"STUDENT".equalsIgnoreCase(tuitionUser.getUser().getRole().name())) {
//            throw new UnAuthorizedException("Only users with STUDENT role can be enrolled in a course");
//        }
//
//        CourseStudent cs = CourseStudent.builder()
//                .course(course)
//                .student(tuitionUser)   // <-- Now using TuitionUser
//                .build();
//
//        return courseStudentRepository.save(cs);
//    }
//
//
//    public void delete(Long tuitionId, Long courseId, Long courseStudentId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        CourseStudent existing = courseStudentRepository.findById(courseStudentId)
//                .orElseThrow(() -> new NotFoundException("CourseStudent not found"));
//
//        // Verify tuition + ownership
//        if (!existing.getCourse().getTuition().getTuitionId().equals(tuitionId) ||
//                !existing.getCourse().getCourseId().equals(courseId) ||
//                !existing.getCourse().getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to delete this course-student relation");
//        }
//
//        courseStudentRepository.delete(existing);
//    }
//}