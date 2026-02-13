package in.ashar.mooble.repository;

import in.ashar.mooble.entity.ClassJoined;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassJoinedRepository extends JpaRepository<ClassJoined, Integer> {



    @Modifying
    @Query("DELETE FROM ClassJoined c WHERE c.tuitionId = :id")
    void deleteAllByTuitionId(@Param("id") int id);

}
