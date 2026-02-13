//package in.ashar.mooble.service;
//
//import in.ashar.mooble.exception.NotFoundException;
//import in.ashar.mooble.exception.UnAuthorizedException;
//import in.ashar.mooble.security.GetCurrentUser;
//import in.ashar.mooble.utility.enums.Subject;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//public class CourseTeacherService {
//
//    private final GetCurrentUser getCurrentUser;
//    private final CourseTeacherRepository courseTeacherRepository;
//    private final CourseRepository courseRepository;
//    private final TuitionUserRepository tuitionUserRepository;
//
//    public CourseTeacherService(GetCurrentUser getCurrentUser,
//                                CourseTeacherRepository courseTeacherRepository,
//                                CourseRepository courseRepository,
//                                TuitionUserRepository tuitionUserRepository) {
//        this.getCurrentUser = getCurrentUser;
//        this.courseTeacherRepository = courseTeacherRepository;
//        this.courseRepository = courseRepository;
//        this.tuitionUserRepository = tuitionUserRepository;
//    }
//
//    public List<CourseTeacher> getAll(Long tuitionId, Long courseId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new NotFoundException("Course not found"));
//
//        // Verify tuition + ownership
//        if (!course.getTuition().getTuitionId().equals(tuitionId) ||
//                !course.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to view teachers of this course");
//        }
//
//        return courseTeacherRepository.findAllByCourse(course);
//    }
//
//    public CourseTeacher getById(Long tuitionId, Long courseId, Long courseTeacherId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        CourseTeacher courseTeacher = courseTeacherRepository.findById(courseTeacherId)
//                .orElseThrow(() -> new NotFoundException("CourseTeacher not found"));
//
//        // Verify tuition + ownership
//        if (!courseTeacher.getCourse().getTuition().getTuitionId().equals(tuitionId) ||
//                !courseTeacher.getCourse().getCourseId().equals(courseId) ||
//                !courseTeacher.getCourse().getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to view this course-teacher relation");
//        }
//
//        return courseTeacher;
//    }
//
//    public CourseTeacher create(Long tuitionId, Long courseId, Long tuitionUserId, Subject subject) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new NotFoundException("Course not found"));
//
//        // Verify tuition + ownership
//        if (!course.getTuition().getTuitionId().equals(tuitionId) ||
//                !course.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to add teachers to this course");
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
//        // Ensure the role is TEACHER
//        if (tuitionUser.getUser().getRole() == null ||
//                !"TEACHER".equalsIgnoreCase(tuitionUser.getUser().getRole().name())) {
//            throw new UnAuthorizedException("Only users with TEACHER role can be assigned to a course");
//        }
//
//        CourseTeacher ct = CourseTeacher.builder()
//                .course(course)
//                .subject(subject)
//                .teacher(tuitionUser)
//                .build();
//
//        return courseTeacherRepository.save(ct);
//    }
//
//    public void delete(Long tuitionId, Long courseId, Long courseTeacherId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        CourseTeacher existing = courseTeacherRepository.findById(courseTeacherId)
//                .orElseThrow(() -> new NotFoundException("CourseTeacher not found"));
//
//        // Verify tuition + ownership
//        if (!existing.getCourse().getTuition().getTuitionId().equals(tuitionId) ||
//                !existing.getCourse().getCourseId().equals(courseId) ||
//                !existing.getCourse().getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to delete this course-teacher relation");
//        }
//
//        courseTeacherRepository.delete(existing);
//    }
//}