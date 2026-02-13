package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceRequestDTO {

    @NotNull(message = "Tuition ID is required")
    private int tuitionId;

    @NotNull(message = "Student ID is required")
    private int studentId;

    @NotNull(message = "Amount due is required")
    @Min(value = 1, message = "Amount due must be at least 1")
    private Double amountDue;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;
}