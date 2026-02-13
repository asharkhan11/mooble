package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Course2;
import in.ashar.mooble.entity.Tuition2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Course2Repository extends JpaRepository<Course2, Integer> {

    List<Course2> findAllByTuition(Tuition2 tuition);

    List<Course2> findAllByTuitionTuitionId(int tuitionId);

    void deleteAllByTuitionTuitionId(int tuitionId);
}
