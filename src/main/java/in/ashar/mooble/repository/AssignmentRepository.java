package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Assignment;
import in.ashar.mooble.entity.Teacher2;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Integer> {
    List<Assignment> findByTeacher(Teacher2 teacher);

    List<Assignment> findBySubjectSubjectId(int subjectId);

    List<Assignment> findByCourseCourseId(int courseId);

    List<Assignment> findBySubjectSubjectIdAndTeacher(int subjectId, Teacher2 teacher);
    List<Assignment> findByCourseCourseIdAndTeacher(int courseId, Teacher2 teacher);

    List<Assignment> findByCourseCourseIdIn(List<Integer> list);

    List<Assignment> findBySubjectSubjectIdIn(List<Integer> list);

    List<Assignment> findByTeacherTeacherId(int teacherId);
}
