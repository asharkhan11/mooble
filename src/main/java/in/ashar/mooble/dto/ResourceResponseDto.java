package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResourceResponseDto {
    private int resourceId;
    private String name;
    private String type;
    private String url;
    private String fileName;
    private LocalDateTime uploadedAt;
    private int subjectId;
    private int courseId;
    private int tuitionId;
    private int uploaderId;
    private int folderId;
}
