package in.ashar.mooble.repository;

import in.ashar.mooble.entity.JoinRequestTuition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinRequestTuitionRepository extends JpaRepository<JoinRequestTuition, Integer> {
    List<JoinRequestTuition> findByTuitionCode(int tuitionCode);

    Optional<JoinRequestTuition> findByTuitionCodeAndUserIdAndIsTeacher(int tuitionCode, int teacherId, boolean isTeacher);

    List<JoinRequestTuition> findByTuitionCodeAndStatus(int tuitionCode, JoinRequestTuition.JoinStatus joinStatus);

    List<JoinRequestTuition> findByTuitionCodeAndStatusAndIsTeacher(int tuitionCode, JoinRequestTuition.JoinStatus joinStatus, boolean b);

    void deleteByTuitionCodeAndStatusAndIsTeacherAndUserId(int tuitionCode, JoinRequestTuition.JoinStatus joinStatus, boolean b, int userId);
}
