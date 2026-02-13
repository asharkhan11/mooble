package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResourceResponseDto {

    private int assignmentResourceId;
    private String name;
    private String type;
    private String url;
    private String fileName;
    private LocalDateTime uploadedAt;
    private int uploaderId;

}
