package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Tuition2;
import in.ashar.mooble.entity.TuitionClass;
import in.ashar.mooble.utility.enums.Standard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TuitionClassRepository extends JpaRepository<TuitionClass, Integer> {

    Optional<TuitionClass> findByTuitionAndStandard(Tuition2 tuition, Standard standard);

    List<TuitionClass> findByTuitionAndStandardIn(Tuition2 tuition, List<Standard> standards);

    Optional<TuitionClass> findByTuitionAndStandardAndSection(Tuition2 tuition, Standard standard, char section);
}
