package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceResponse {

    private int resourceId;
    private String name;
    private String type;
    private long size;
    private String fileName;
    private LocalDateTime uploadedAt;

    private int tuitionId;
    private Integer courseId;
    private Integer subjectId;

    private Integer folderId;
}
