package in.ashar.mooble.dto;

import lombok.Data;

@Data
public class GradeRequestDto {

    private int submissionId;
    private int score;
    private String feedback;
}
