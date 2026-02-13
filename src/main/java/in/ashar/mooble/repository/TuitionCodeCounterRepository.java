package in.ashar.mooble.repository;

import in.ashar.mooble.entity.TuitionCodeCounter;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TuitionCodeCounterRepository extends JpaRepository<TuitionCodeCounter, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM TuitionCodeCounter c WHERE c.id = 1")
    TuitionCodeCounter lockAndGet();
}
