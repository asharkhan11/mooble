package in.ashar.mooble.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Due date is required")
    @FutureOrPresent(message = "Due date cannot be in the past")
    private LocalDate dueDate;

    @Min(value = 0, message = "Maximum marks must be zero or positive")
    private int maxMarks;

    private List<Integer> resourceIds;

    @PositiveOrZero(message = "Subject ID must be a positive integer")
    private int subjectId;

    @PositiveOrZero(message = "Course ID must be a positive integer")
    private int courseId;

}
