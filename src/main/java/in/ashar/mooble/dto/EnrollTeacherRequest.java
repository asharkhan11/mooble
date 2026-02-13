package in.ashar.mooble.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EnrollTeacherRequest {

    @NotNull(message = "tuition id must be provided")
    @Positive(message = "tuition id must be greater than 0")
    private int tuitionId;
    @NotNull(message = "teacher id must be provided")
    @Positive(message = "teacher id must be greater than 0")
    private int teacherId;

    private List<Integer> subjectIds =new ArrayList<>();
    private List<Integer> courseIds = new ArrayList<>();

    @AssertTrue(message = "Either subjectIds or courseIds must be provided")
    public boolean isAtLeastOneProvided() {
        return (subjectIds != null && !subjectIds.isEmpty())
                || (courseIds  != null && !courseIds.isEmpty());
    }

}
