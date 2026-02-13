package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Credentials2;
import in.ashar.mooble.entity.Subject2;
import in.ashar.mooble.entity.Teacher2;
import in.ashar.mooble.entity.TuitionClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Teacher2Repository extends JpaRepository<Teacher2, Integer> {

    Optional<Teacher2> findByTeacherCredential(Credentials2 credential);

    Optional<Teacher2> findByTeacherCredentialEmail(String email);

    List<Teacher2> findAllBySubjectsIn(List<Subject2> subjects);

    List<Teacher2> findAllByTuitionClassesIn(List<TuitionClass> tc);

    List<Teacher2> findAllBySubjectsContaining(Subject2 subject);
}
