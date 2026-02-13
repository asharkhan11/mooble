package in.ashar.mooble.service;

import in.ashar.mooble.entity.Invoice;
import in.ashar.mooble.entity.Transaction;
import in.ashar.mooble.entity.Tuition2;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.InvoiceRepository;
import in.ashar.mooble.repository.TransactionRepository;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.enums.InvoiceStatus;
import in.ashar.mooble.utility.enums.PaymentMethod;
import in.ashar.mooble.utility.enums.PaymentStatus;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final GetCurrentUser getCurrentUser;


    // Record a payment and update invoice
    public Transaction recordTransaction(Invoice invoice, Double amount, PaymentMethod method, PaymentStatus status, String referenceId) {
        Transaction transaction = new Transaction();
        transaction.setInvoice(invoice);
        transaction.setTuitionId(invoice.getTuitionId());
        transaction.setStudentId(invoice.getStudentId());
        transaction.setAmount(amount);
        transaction.setMethod(method);
        transaction.setStatus(status);
        transaction.setPaymentDate(LocalDateTime.now());
        transaction.setReferenceId(referenceId);

        // Update invoice
        invoice.setAmountPaid(invoice.getAmountPaid() + amount);
        if (invoice.getAmountPaid() >= invoice.getAmountDue()) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else if (invoice.getAmountPaid() > 0) {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        invoiceRepository.save(invoice);

        return transactionRepository.save(transaction);
    }

    // Get transactions by student
    public List<Transaction> getTransactionsByStudent(int studentId) {

        boolean myStudent = getCurrentUser.isMyStudent(studentId);

        if(!myStudent){
            throw new UnAuthorizedException("Invalid student id : "+studentId);
        }

        return transactionRepository.findAllByStudentId(studentId);
    }

    // Get transactions by tuition
    public List<Transaction> getTransactionsByTuition(int tuitionId) {

        boolean myTuition = getCurrentUser.isMyTuition(tuitionId);

        if(!myTuition){
            throw new UnAuthorizedException("Invalid tuition id : "+tuitionId);
        }

        return transactionRepository.findAllByTuitionId(tuitionId);
    }

    // Get transactions between dates (monthly report)
    public List<Transaction> getTransactionsBetween(LocalDateTime start, LocalDateTime end) {

        List<Integer> tuitionIds = getCurrentUser.getCurrentAdminTuition().stream().map(Tuition2::getTuitionId).toList();

        return transactionRepository.findAllByPaymentDateBetweenAndTuitionIdIn(start, end, tuitionIds);
    }

    public List<Transaction> getTransactionsBetweenAndTuition(LocalDateTime start, LocalDateTime end, int tuitionId) {

        boolean myTuition = getCurrentUser.isMyTuition(tuitionId);

        if(!myTuition){
            throw new UnAuthorizedException("Invalid tuition id : "+tuitionId);
        }

        return transactionRepository.findAllByPaymentDateBetweenAndTuitionId(start, end, tuitionId);
    }

    // Calculate total revenue (all-time)
    public Double getTotalRevenueOfTuition(int tuitionId) {

        boolean myTuition = getCurrentUser.isMyTuition(tuitionId);

        if(!myTuition){
            throw new UnAuthorizedException("Invalid tuition id : "+tuitionId);
        }

        return transactionRepository.findAllByTuitionId(tuitionId).stream()
                .filter(t -> t.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public Double getTotalRevenueOfAllTuition() {

        List<Integer> tuitionIds = getCurrentUser.getCurrentAdminTuition().stream().map(Tuition2::getTuitionId).toList();

        return transactionRepository.findAllByTuitionIdIn(tuitionIds).stream()
                .filter(t -> t.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

}
