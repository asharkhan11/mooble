package in.ashar.mooble.utility.helpers;

import in.ashar.mooble.entity.Assignment;
import in.ashar.mooble.entity.Student2;
import in.ashar.mooble.entity.Teacher2;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.repository.AssignmentRepository;
import in.ashar.mooble.repository.SubmissionRepository;
import in.ashar.mooble.security.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubmissionHelper {


    private final GetCurrentUser currentUser;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;

    public boolean isMyAssignment(int assignmentId) {

        Object user = currentUser.getLoggedInUser();

        Optional<Assignment> optAssignment = assignmentRepository.findById(assignmentId);

        if (optAssignment.isEmpty()) return false;

        Assignment assignment = optAssignment.get();

        if (user instanceof Student2 student) {

            boolean allowed = false;

            if (assignment.getSubject() != null && student.getSubjects() != null) {
                allowed = student.getSubjects().stream().anyMatch(sub -> sub.getSubjectId() == assignment.getSubject().getSubjectId());
            }
            if (!allowed && assignment.getCourse() != null && student.getCourses() != null) {
                allowed = student.getCourses().stream().anyMatch(c -> c.getCourseId() == assignment.getCourse().getCourseId());
            }

            return allowed;


        } else if (user instanceof Teacher2 teacher) {

            return assignment.getTeacher().getTeacherId() == teacher.getTeacherId();

        } else {

            return false;

        }

    }

}
