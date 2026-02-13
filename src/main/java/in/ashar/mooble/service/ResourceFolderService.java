package in.ashar.mooble.service;

import in.ashar.mooble.dto.ResourceFolderDto;
import in.ashar.mooble.dto.ResourceFolderRequest;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.InvalidOptionException;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.message.MapObjects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceFolderService {

    private final ResourceFolderRepository folderRepository;
    private final ResourceFolderRepository2 folderRepository2;
    private final ResourceRepository resourceRepo;
    private final MapObjects mapObject;
    private final Tuition2Repository tuitionRepository;
    private final GetCurrentUser currentUser;
    private final Course2Repository courseRepository;
    private final Subject2Repository subjectRepository;

    public List<ResourceFolderDto> getFoldersWithResources(int id, boolean isCourseId) {

        List<ResourceFolder> folders;

        if(isCourseId) {
            folders = folderRepository.findAllByCourseId(id);
        }
        else{
            folders = folderRepository.findAllBySubjectId(id);
        }

        return folders.stream().map(folder -> {
            ResourceFolderDto dto = new ResourceFolderDto();
            dto.setFolderId(folder.getFolderId());
            dto.setName(folder.getName());
            dto.setParentFolderId(folder.getParentFolder() != null ? folder.getParentFolder().getFolderId() : 0);
            dto.setResources(folder.getResources().stream()
                    .map(mapObject::mapResourceResponse)
                    .collect(Collectors.toList()));
            return dto;
        }).collect(Collectors.toList());
    }

    public ResourceFolderDto createFolder(ResourceFolderRequest req) {
        ResourceFolder folder = new ResourceFolder();
        folder.setName(req.getName());
        if(req.getCourseId()!=0){
            folder.setCourseId(req.getCourseId());
        }
        if(req.getSubjectId()!=0){
            folder.setSubjectId(req.getSubjectId());
        }
        folder.setTuitionId(req.getTuitionId());
        if (req.getParentFolderId() != 0) {
            folder.setParentFolder(folderRepository.findById(req.getParentFolderId()).orElse(null));
        }
        folder = folderRepository.save(folder);
        ResourceFolderDto dto = new ResourceFolderDto();
        dto.setFolderId(folder.getFolderId());
        dto.setName(folder.getName());
        dto.setParentFolderId(req.getParentFolderId());
        dto.setResources(Collections.emptyList());
        return dto;
    }




    //////////////////// NEW API ////////////////////
}
