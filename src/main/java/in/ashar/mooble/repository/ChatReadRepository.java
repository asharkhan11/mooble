package in.ashar.mooble.repository;


import in.ashar.mooble.entity.ChatRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatReadRepository extends JpaRepository<ChatRead, Integer> {
    
    Optional<ChatRead> findByTeacherIdAndTuitionId(Integer teacherId, Integer tuitionId);


    @Modifying
    @Query("DELETE FROM ChatRead cr WHERE cr.tuitionId = :id")
    void deleteAllByTuitionId(@Param("id") int id);

}
