package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Subject2;
import in.ashar.mooble.entity.TuitionClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Subject2Repository extends JpaRepository<Subject2, Integer> {

    List<Subject2> findAllByTuitionClassIn(List<TuitionClass> tuitionClasses);

}
