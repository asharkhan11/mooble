package in.ashar.mooble.repository;

import in.ashar.mooble.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Student2Repository extends JpaRepository<Student2, Integer> {
    Optional<Student2> findByStudentCredential(Credentials2 credential);

    Optional<Student2> findByStudentCredentialEmail(String email);


    List<Student2> findAllBySubjectsIn(List<Subject2> subjects);

    List<Student2> findAllByTuitionClassesIn(List<TuitionClass> tc);

    List<Student2> findAllBySubjectsContaining(Subject2 subject);

    List<Student2> findAllByCoursesContaining(Course2 course);

    @Query("""
        SELECT DISTINCT s
        FROM Student2 s
        JOIN s.subjects subj
        WHERE subj.subjectId = :subjectId
    """)
    List<Student2> findBySubjectId(@Param("subjectId") int subjectId);

    @Query("""
        SELECT DISTINCT s
        FROM Student2 s
        JOIN s.courses c
        WHERE c.courseId = :courseId
    """)
    List<Student2> findByCourseId(@Param("courseId") int courseId);
}
