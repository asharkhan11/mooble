package in.ashar.mooble.controller;

import in.ashar.mooble.dto.InvoiceRequestDTO;
import in.ashar.mooble.dto.TransactionRequestDTO;
import in.ashar.mooble.entity.Invoice;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.utility.enums.InvoiceStatus;
import in.ashar.mooble.entity.Transaction;
import in.ashar.mooble.utility.enums.PaymentStatus;
import in.ashar.mooble.service.InvoiceService;
import in.ashar.mooble.service.TransactionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/revenue")
@PreAuthorize("hasRole('ADMIN')")
public class RevenueController {

    private final InvoiceService invoiceService;
    private final TransactionService transactionService;

    public RevenueController(InvoiceService invoiceService, TransactionService transactionService) {
        this.invoiceService = invoiceService;
        this.transactionService = transactionService;
    }

    // -------------------- INVOICE APIS --------------------

    @PostMapping("/invoice")
    public Invoice createInvoice(@Valid @RequestBody InvoiceRequestDTO request) {
        return invoiceService.createInvoice(
                request.getTuitionId(),
                request.getStudentId(),
                request.getAmountDue(),
                request.getDueDate()
        );
    }

    @GetMapping("/invoice/student/{studentId}")
    public List<Invoice> getInvoicesByStudent(@PathVariable int studentId) {
        return invoiceService.getInvoicesByStudent(studentId);
    }

    @GetMapping("/invoice/status/{status}")
    public List<Invoice> getInvoicesByStatus(@PathVariable InvoiceStatus status) {
        return invoiceService.getInvoicesByStatus(status);
    }

    @GetMapping("/invoice/month")
    public List<Invoice> getInvoicesForMonth(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return invoiceService.getInvoicesForMonth(start, end);
    }

    @GetMapping("/tuition/{tuitionId}/invoice/status/{status}")
    public List<Invoice> getInvoicesByTuitionStatus(@PathVariable int tuitionId, @PathVariable InvoiceStatus status) {
        return invoiceService.getInvoicesByTuitionStatus(tuitionId, status);
    }

    @GetMapping("/tuition/{tuitionId}/invoice/month")
    public List<Invoice> getInvoicesTuitionForMonth(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                                    @PathVariable int tuitionId) {
        return invoiceService.getInvoicesTuitionForMonth(tuitionId,start, end);
    }

    // -------------------- TRANSACTION APIS --------------------

    @PostMapping("/transaction")
    public Transaction recordTransaction(@Valid @RequestBody TransactionRequestDTO request) {

        Invoice invoice = invoiceService.getInvoicesByStudent(request.getStudentId())
                .stream()
                .filter(inv -> inv.getId() == request.getInvoiceId())
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Invoice not found"));

        return transactionService.recordTransaction(
                invoice,
                request.getAmount(),
                request.getMethod(),
                request.getStatus(),
                request.getReferenceId()
        );
    }

    @GetMapping("/transaction/student/{studentId}")
    public List<Transaction> getTransactionsByStudent(@PathVariable int studentId) {
        return transactionService.getTransactionsByStudent(studentId);
    }

    @GetMapping("/transaction/tuition/{tuitionId}")
    public List<Transaction> getTransactionsByTuition(@PathVariable int tuitionId) {
        return transactionService.getTransactionsByTuition(tuitionId);
    }

    @GetMapping("/transaction/month")
    public List<Transaction> getTransactionsForMonth(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.atTime(23, 59, 59);
        return transactionService.getTransactionsBetween(startDateTime, endDateTime);
    }

    // -------------------- REVENUE REPORT APIS --------------------

    @GetMapping("/tuition/{tuitionId}")
    public Double getAllTimeRevenueOfTuition(@PathVariable int tuitionId) {

        return transactionService.getTotalRevenueOfTuition(tuitionId);
    }

    @GetMapping("/tuition")
    public Double getAllTimeRevenueOfAllTuition() {

        return transactionService.getTotalRevenueOfAllTuition();
    }

    @GetMapping("/tuition/{tuitionId}/monthly")
    public Double getMonthlyRevenueOfTuition(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
                                             @PathVariable @Positive int tuitionId) {

        List<Transaction> transactions = transactionService.getTransactionsBetweenAndTuition(
                start.atStartOfDay(), end.atTime(23, 59, 59), tuitionId
        );
        return transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @GetMapping("/tuition/monthly")
    public Double getMonthlyRevenueOfAllTuition(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        List<Transaction> transactions = transactionService.getTransactionsBetween(
                start.atStartOfDay(), end.atTime(23, 59, 59)
        );
        return transactions.stream()
                .filter(t -> t.getStatus() == PaymentStatus.SUCCESS)
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    @GetMapping("/tuition/{tuitionId}/pending")
    public List<Invoice> getPendingInvoicesOfTuition(@PathVariable @Positive int tuitionId) {

        return invoiceService.getInvoicesByTuitionStatus(tuitionId, InvoiceStatus.UNPAID);
    }

}
