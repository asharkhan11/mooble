package in.ashar.mooble.repository;

import in.ashar.mooble.dto.BroadcastMessageDTO;
import in.ashar.mooble.entity.MessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRecipientRepository extends JpaRepository<MessageRecipient, Integer> {
    Optional<MessageRecipient> findByMessageIdAndRecipientId(int messageId, int userId);

    int countByRecipientIdAndIsRead(int userId, boolean b);

    List<BroadcastMessageDTO> findAllByRecipientId(int userId);

    List<MessageRecipient> findAllByMessageTuitionId(int tuitionId);

    void deleteByMessageTuitionId(int tuitionId);

    @Modifying
    @Query("DELETE FROM MessageRecipient mr WHERE mr.message.tuitionId = :id")
    void deleteAllByTuitionId(int id);

}
