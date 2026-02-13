package in.ashar.mooble.service;

import in.ashar.mooble.entity.Invoice;
import in.ashar.mooble.entity.Tuition2;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.InvoiceRepository;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.enums.InvoiceStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final GetCurrentUser getCurrentUser;

    // Create a new invoice
    public Invoice createInvoice(int tuitionId, int studentId, Double amountDue, LocalDate dueDate) {

        boolean isValid = getCurrentUser.isMyTuitionAndStudent(tuitionId, studentId);

        if(!isValid){
            throw new UnAuthorizedException("Invalid tuition or student");
        }

        Invoice invoice = new Invoice();
        invoice.setTuitionId(tuitionId);
        invoice.setStudentId(studentId);
        invoice.setAmountDue(amountDue);
        invoice.setDueDate(dueDate);
        invoice.setStatus(InvoiceStatus.UNPAID);
        return invoiceRepository.save(invoice);
    }

    // Get invoices by student
    public List<Invoice> getInvoicesByStudent(int studentId) {

        boolean myStudent = getCurrentUser.isMyStudent(studentId);

        if(!myStudent){
            throw new UnAuthorizedException("Invalid student id : "+studentId);
        }

        return invoiceRepository.findAllByStudentId(studentId);
    }

    // Get invoices by status
    public List<Invoice> getInvoicesByStatus(InvoiceStatus status) {

        List<Integer> tuitionIds = getCurrentUser.getCurrentAdminTuition().stream().map(Tuition2::getTuitionId).toList();

        return invoiceRepository.findAllByStatusAndTuitionIdIn(status,tuitionIds);
    }

    // Get invoices for a date range (monthly report)
    public List<Invoice> getInvoicesForMonth(LocalDate start, LocalDate end) {

        List<Integer> tuitionIds = getCurrentUser.getCurrentAdminTuition().stream().map(Tuition2::getTuitionId).toList();

        return invoiceRepository.findAllByDueDateBetweenAndTuitionIdIn(start, end, tuitionIds);
    }

    public List<Invoice> getInvoicesByTuitionStatus(int tuitionId, InvoiceStatus status) {

        boolean myTuition = getCurrentUser.isMyTuition(tuitionId);

        if(!myTuition){
            throw new UnAuthorizedException("Invalid tuition id : "+tuitionId);
        }

        return invoiceRepository.findAllByStatusAndTuitionId(status, tuitionId);

    }

    public List<Invoice> getInvoicesTuitionForMonth(int tuitionId, LocalDate start, LocalDate end) {

        boolean myTuition = getCurrentUser.isMyTuition(tuitionId);

        if(!myTuition){
            throw new UnAuthorizedException("Invalid tuition id : "+tuitionId);
        }

        return invoiceRepository.findAllByDueDateBetweenAndTuitionId(start, end, tuitionId);

    }
}
