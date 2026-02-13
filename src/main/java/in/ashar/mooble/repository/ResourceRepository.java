package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Course2;
import in.ashar.mooble.entity.Resource;
import in.ashar.mooble.entity.Subject2;
import in.ashar.mooble.entity.Tuition2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer> {

    List<Resource> findAllByCourseIn(List<Course2> courses);

    List<Resource> findAllBySubjectIn(List<Subject2> subjects);

    List<Resource> findAllBySubject(Subject2 subject);

    List<Resource> findAllByCourse(Course2 course);

    List<Resource> findAllByTuition(Tuition2 tuition);

    List<Resource> findAllByCourseInOrSubjectIn(List<Course2> courses, List<Subject2> subjects);
}
