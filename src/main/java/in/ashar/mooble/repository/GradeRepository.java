package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade,Integer> {
    List<Grade> findByGradedByTeacherId(int teacherId);

    List<Grade> findBySubmissionStudentStudentId(int studentId);

    Optional<Grade> findBySubmissionSubmissionId(long submissionId);
}
