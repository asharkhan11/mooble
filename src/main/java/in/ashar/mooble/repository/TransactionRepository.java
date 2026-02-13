package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findAllByStudentId(int studentId);

    List<Transaction> findAllByTuitionId(int tuitionId);

//    List<Transaction> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    List<Transaction> findAllByPaymentDateBetweenAndTuitionIdIn(LocalDateTime start, LocalDateTime end, List<Integer> tuitionIds);

    List<Transaction> findAllByTuitionIdIn(List<Integer> tuitionIds);

    List<Transaction> findAllByPaymentDateBetweenAndTuitionId(LocalDateTime start, LocalDateTime end, int tuitionId);
}
