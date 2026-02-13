package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Tuition2;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Tuition2Repository extends JpaRepository<Tuition2, Integer> {
    Optional<Tuition2> findByTuitionCode(int tuitionCode);

    List<Tuition2> findByTuitionAdminAdminId(int adminId);

    @EntityGraph(attributePaths = {
            "tuitionClasses.students.subjects",
            "tuitionClasses.teachers.subjects",
            "courses.students",
            "courses.teachers"
    })
    @Query("SELECT t FROM Tuition2 t WHERE t.tuitionId = :id")
    Optional<Tuition2> findFullGraphById(@Param("id") int id);

}
