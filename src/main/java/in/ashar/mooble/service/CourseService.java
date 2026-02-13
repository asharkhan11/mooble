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
//public class CourseService {
//
//    private final GetCurrentUser getCurrentUser;
//    private final CourseRepository courseRepository;
//    private final TuitionRepository tuitionRepository;
//
//    public CourseService(GetCurrentUser getCurrentUser,
//                         CourseRepository courseRepository,
//                         TuitionRepository tuitionRepository) {
//        this.getCurrentUser = getCurrentUser;
//        this.courseRepository = courseRepository;
//        this.tuitionRepository = tuitionRepository;
//    }
//
//    public List<Course> getAll(Long tuitionId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Tuition tuition = tuitionRepository.findById(tuitionId)
//                .orElseThrow(() -> new NotFoundException("Tuition not found"));
//
//        // Check ownership
//        if (!tuition.getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to view courses of this tuition");
//        }
//
//        return courseRepository.findAllByTuition(tuition);
//    }
//
//    public Course getById(Long tuitionId, Long courseId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Course course = courseRepository.findById(courseId)
//                .orElseThrow(() -> new NotFoundException("Course not found"));
//
//        // Check ownership via tuition
//        if (!course.getTuition().getTuitionId().equals(tuitionId) ||
//                !course.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to view this course");
//        }
//
//        return course;
//    }
//
//    public Course create(Long tuitionId, Course course) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Tuition tuition = tuitionRepository.findById(tuitionId)
//                .orElseThrow(() -> new NotFoundException("Tuition not found"));
//
//        // Check ownership
//        if (!tuition.getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to add courses to this tuition");
//        }
//
//        course.setTuition(tuition);
//
//        return courseRepository.save(course);
//    }
//
//    public Course update(Long tuitionId, Long courseId, Course course) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Course existing = courseRepository.findById(courseId)
//                .orElseThrow(() -> new NotFoundException("Course not found"));
//
//        // Check ownership
//        if (!existing.getTuition().getTuitionId().equals(tuitionId) ||
//                !existing.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to update this course");
//        }
//
//        // Update only allowed fields
//        existing.setCourseName(course.getCourseName());
//        existing.setStandard(course.getStandard());
//        existing.setCourseDuration(course.getCourseDuration());
//
//        return courseRepository.save(existing);
//    }
//
//    public void delete(Long tuitionId, Long courseId) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Course existing = courseRepository.findById(courseId)
//                .orElseThrow(() -> new NotFoundException("Course not found"));
//
//        // Check ownership
//        if (!existing.getTuition().getTuitionId().equals(tuitionId) ||
//                !existing.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to delete this course");
//        }
//
//        courseRepository.delete(existing);
//    }
//}