package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends JpaRepository<Submission, Integer> {

    List<Submission> findByAssignmentId(int assignmentId);

    List<Submission> findByStudentStudentId(int studentId);

    Optional<Submission> findByStudentStudentIdAndAssignmentId(int studentId, int assignmentId);

    List<Submission> findByAssignmentIdIn(List<Integer> assignmentIds);
}
