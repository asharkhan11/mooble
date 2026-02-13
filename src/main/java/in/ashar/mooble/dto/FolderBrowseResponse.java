package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FolderBrowseResponse {

    private List<ResourceFolderDto2> folders;
    private List<ResourceResponse> resources;
}
