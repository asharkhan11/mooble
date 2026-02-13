package in.ashar.mooble.dto;

import lombok.Data;

import java.util.List;

@Data
public class ResourceFolderDto {
    private int folderId;
    private String name;
    private int parentFolderId;
    private List<ResourceResponseDto> resources;
}