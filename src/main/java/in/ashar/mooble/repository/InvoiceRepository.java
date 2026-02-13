package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Invoice;
import in.ashar.mooble.utility.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

    List<Invoice> findAllByStudentId(int studentId);

    List<Invoice> findAllByStatusAndTuitionIdIn(InvoiceStatus status, List<Integer> tuitionIds);

    List<Invoice> findAllByDueDateBetweenAndTuitionIdIn(LocalDate start, LocalDate end, List<Integer> tuitionIds);

    List<Invoice> findAllByStatusAndTuitionId(InvoiceStatus status, int tuitionId);

    List<Invoice> findAllByDueDateBetweenAndTuitionId(LocalDate start, LocalDate end, int tuitionId);
}
