package in.ashar.mooble.dto;

import in.ashar.mooble.utility.enums.PaymentMethod;
import in.ashar.mooble.utility.enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequestDTO {

    @NotNull(message = "Invoice ID is required")
    private int invoiceId;

    @NotNull(message = "Student ID is required")
    private int studentId;

    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least 1")
    private Double amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    @Size(max = 100, message = "Reference ID can be at most 100 characters")
    private String referenceId;
}
