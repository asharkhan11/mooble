package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceFolderResponse {
    private Integer folderId;
    private String name;
    private Integer tuitionId;
    private Integer courseId;
    private Integer subjectId;
    private Integer parentFolderId;
    private String path;
}
