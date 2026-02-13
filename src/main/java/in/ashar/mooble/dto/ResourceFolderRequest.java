package in.ashar.mooble.dto;

import lombok.Data;

@Data
public class ResourceFolderRequest {
    private String name;
    private Integer courseId;
    private Integer subjectId;
    private Integer tuitionId;
    private Integer parentFolderId;
}