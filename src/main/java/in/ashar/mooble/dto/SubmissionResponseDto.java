package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubmissionResponseDto {

    private int submissionId;

    private int studentId;
    private String studentName;

    private LocalDateTime submittedOn;
    private List<Integer> resourceIds;
    private int marksObtained;
    private String feedback;
    private String status;

}
