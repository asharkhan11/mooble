package in.ashar.mooble.service;

import in.ashar.mooble.dto.GradeRequestDto;
import in.ashar.mooble.entity.Assignment;
import in.ashar.mooble.entity.Student2;
import in.ashar.mooble.entity.Submission;
import in.ashar.mooble.entity.Teacher2;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.AssignmentRepository;
import in.ashar.mooble.repository.SubmissionRepository;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.helpers.SubmissionHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final GetCurrentUser currentUser;
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionHelper helper;
    private final ResourceService resourceService;

    public List<Submission> getAllSubmission() {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            return submissionRepository.findByStudentStudentId(student.getStudentId());


        } else if (user instanceof Teacher2 teacher) {

            List<Assignment> assignments = assignmentRepository.findByTeacherTeacherId(teacher.getTeacherId());

            return submissionRepository.findByAssignmentIdIn(assignments.stream().map(Assignment::getId).toList());

        } else {

            return null;

        }

    }

    public List<Submission> getSubmissionByAssignmentId(int assignmentId) {

        Object user = currentUser.getLoggedInUser();

        if (!helper.isMyAssignment(assignmentId)) {
            throw new UnAuthorizedException("Invalid Assignment id");
        }

        if (user instanceof Student2 student) {

            Submission submission = submissionRepository.findByStudentStudentIdAndAssignmentId(student.getStudentId(), assignmentId).orElseThrow(() -> new NotFoundException("Submission not found"));
            return List.of(submission);


        } else if (user instanceof Teacher2) {

            return submissionRepository.findByAssignmentId(assignmentId);

        } else {

            return null;

        }

    }

    public Submission gradeSubmission(GradeRequestDto requestDto) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {

            Submission submission = submissionRepository.findById(requestDto.getSubmissionId()).orElseThrow(() -> new NotFoundException("Submission not found"));

            if (submission.getAssignment().getTeacher().getTeacherId() != teacher.getTeacherId()) {
                throw new UnAuthorizedException("Invalid Submission id");
            }

            submission.setMarksObtained(requestDto.getScore());
            submission.setFeedback(requestDto.getFeedback());
            submission.setGradedOn(LocalDateTime.now());
            submission.setStatus(Submission.SubmissionStatus.GRADED);

            resourceService.deleteSubmissionResources(submission.getResourceIds());
            submission.getResourceIds().clear();

            return submissionRepository.save(submission);

        } else {
            throw new UnAuthorizedException("Access denied to grade submission");
        }

    }

    public void deleteSubmissionByAssignmentId(int assignmentId) {


        Object user = currentUser.getLoggedInUser();

        if (!helper.isMyAssignment(assignmentId)) {
            throw new UnAuthorizedException("Invalid Assignment id");
        }

        if (user instanceof Teacher2) {

            List<Submission> submissions = submissionRepository.findByAssignmentId(assignmentId);
            List<Integer> resourceIds = submissions.stream().flatMap(s -> s.getResourceIds().stream()).toList();
            resourceService.deleteSubmissionResources(resourceIds);

        } else {

            throw new UnAuthorizedException("Access denied");

        }


    }

}
