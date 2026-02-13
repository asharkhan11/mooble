package in.ashar.mooble.repository;

import in.ashar.mooble.entity.BroadcastMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BroadcastMessageRepository extends JpaRepository<BroadcastMessage, Integer> {
    List<BroadcastMessage> findByTuitionId(int tuitionId);

    List<BroadcastMessage> findByTuitionIdAndAudienceTypeIn(Integer tuitionId, List<String> teachers);

    @Modifying
    @Query("DELETE FROM BroadcastMessage bm WHERE bm.tuitionId = :id")
    void deleteAllByTuitionId(@Param("id") int id);
}
