package in.ashar.mooble.controller;

import in.ashar.mooble.dto.AssignmentResourceResponseDto;
import in.ashar.mooble.dto.ResourceFolderDto;
import in.ashar.mooble.dto.ResourceFolderRequest;
import in.ashar.mooble.dto.ResourceResponseDto;
import in.ashar.mooble.entity.AssignmentResource;
import in.ashar.mooble.entity.Resource;
import in.ashar.mooble.service.ResourceFolderService;
import in.ashar.mooble.service.ResourceService;
import in.ashar.mooble.service.FileStorageService;
import in.ashar.mooble.utility.message.MapObjects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "*",
        allowedHeaders = "*",
        exposedHeaders = {"Content-Type", "Content-Disposition", "Content-Length"},
        maxAge = 3600
)
public class ResourceController {

    private final ResourceService resourceService;
    private final ResourceFolderService folderService;
    private final FileStorageService fileStorageService;
    private final MapObjects mapObject;

    // ============ Upload Endpoints ============


    @PostMapping("/upload")
    public ResponseEntity<ResourceResponseDto> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "tuitionId") int tuitionId,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "folderId", required = false) Integer folderId,
            @RequestParam("isAdmin") boolean isAdmin) throws Exception {

        log.info("Uploading resource: {} to tuition: {}", file.getOriginalFilename(), tuitionId);

        Resource resource = resourceService.uploadResource(
                file, tuitionId, courseId, subjectId, folderId, isAdmin
        );
        ResourceResponseDto responseDto = mapObject.mapResourceResponse(resource);

        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/upload/multiple")
    public ResponseEntity<List<ResourceResponseDto>> uploadMultipleResources(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("tuitionId") int tuitionId,
            @RequestParam(value = "courseId", required = false) Integer courseId,
            @RequestParam(value = "subjectId", required = false) Integer subjectId,
            @RequestParam(value = "folderId", required = false) Integer folderId,
            @RequestParam("isAdmin") boolean isAdmin) throws Exception {

        log.info("Uploading {} resources to tuition: {}", files.size(), tuitionId);

        List<Resource> uploadedResources = resourceService.uploadMultipleResources(
                files, tuitionId, courseId, subjectId, folderId, isAdmin
        );
        List<ResourceResponseDto> list = uploadedResources.stream()
                .map(mapObject::mapResourceResponse)
                .toList();

        return ResponseEntity.ok(list);
    }

    // ============ Download/View Endpoints ============

    @GetMapping("/{id}/view")
    public ResponseEntity<ByteArrayResource> viewResourceByte(
            @PathVariable int id) throws Exception {

        log.debug("Viewing resource: {}", id);

        Resource resource = resourceService.getResourceById(id);
        byte[] fileBytes = loadFileAsBytes(resource.getFileName());

        MediaType mediaType = resolveMediaType(resource.getType());
        HttpHeaders headers = buildHeaders(mediaType, fileBytes, resource, false);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ByteArrayResource(fileBytes));
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> downloadResource(
            @PathVariable int id) throws Exception {

        log.debug("Downloading resource: {}", id);

        Resource resource = resourceService.getResourceById(id);
        byte[] fileBytes = loadFileAsBytes(resource.getFileName());

        MediaType mediaType = resolveMediaType(resource.getType());
        HttpHeaders headers = buildHeaders(mediaType, fileBytes, resource, true);

        return ResponseEntity.ok()
                .headers(headers)
                .body(new ByteArrayResource(fileBytes));
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<Map<String, String>> getPresignedUrl(@PathVariable int id) throws Exception {

        log.debug("Generating presigned URL for resource: {}", id);

        Resource resource = resourceService.getResourceById(id);
        String presignedUrl = fileStorageService.getPresignedUrl(resource.getFileName(), 30);

        return ResponseEntity.ok(Map.of(
                "url", presignedUrl,
                "name", resource.getName(),
                "type", resource.getType()
        ));
    }


    @GetMapping("/{id}/url/assignment")
    public ResponseEntity<Map<String, String>> getPresignedUrlOfAssignmentResource(@PathVariable int id) throws Exception {

        log.debug("Generating presigned URL for assignment resource: {}", id);

        AssignmentResource resource = resourceService.getAssignmentResourceById(id);
        String presignedUrl = fileStorageService.getPresignedUrl(resource.getFileName(), 30);

        return ResponseEntity.ok(Map.of(
                "url", presignedUrl,
                "name", resource.getName(),
                "type", resource.getType()
        ));
    }


    // ============ CRUD Endpoints ============

    @GetMapping
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','STUDENT')")
    public ResponseEntity<List<ResourceResponseDto>> getAllResources(
            @RequestParam("tuitionId") int tuitionId) {

        log.debug("Fetching all resources for tuition: {}", tuitionId);

        List<Resource> allResources = resourceService.getAllResources(tuitionId);
        List<ResourceResponseDto> list = allResources.stream()
                .map(mapObject::mapResourceResponse)
                .toList();

        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponseDto> getResourceById(
            @PathVariable int id) throws Exception {

        log.debug("Fetching resource: {}", id);

        Resource resource = resourceService.getResourceById(id);

        return ResponseEntity.ok(mapObject.mapResourceResponse(resource));
    }



    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable int id) throws Exception {

        log.info("Deleting resource: {}", id);

        resourceService.deleteResource(id);

        return ResponseEntity.noContent().build();
    }


    /// Assignment Resource

    @PostMapping("/upload/assignment")
    public ResponseEntity<AssignmentResourceResponseDto> uploadAssignmentResource(
            @RequestParam("file") MultipartFile file) throws Exception {

        log.info("Uploading assignment resource: {} ", file.getOriginalFilename());

        AssignmentResource resource = resourceService.uploadAssignmentResource(file);
        AssignmentResourceResponseDto responseDto = mapObject.mapAssignmentResourceResponse(resource);

        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{id}/assignment")
    public ResponseEntity<AssignmentResourceResponseDto> getAssignmentResourceById(
            @PathVariable int id) {

        log.debug("Fetching assignment resource: {}", id);

        AssignmentResource resource = resourceService.getAssignmentResourceById(id);

        return ResponseEntity.ok(mapObject.mapAssignmentResourceResponse(resource));
    }

    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResourceResponseDto>> getAssignmentResourceByIds(
            @RequestParam List<Integer> ids) {

        log.debug("Fetching assignment resources: {}", ids);

        List<AssignmentResource> resources = resourceService.getAssignmentResourceByIds(ids);

        return ResponseEntity.ok(resources.stream().map(mapObject::mapAssignmentResourceResponse).toList());
    }

    @DeleteMapping("/{id}/assignment")
    public ResponseEntity<Void> deleteAssignmentResource(@PathVariable int id) throws Exception {

        log.info("Deleting Assignment resource: {}", id);

        resourceService.deleteAssignmentResource(id);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/assignments")
    public ResponseEntity<Void> deleteAssignmentResources(@RequestParam List<Integer> ids) throws Exception {

        log.info("Deleting Assignment resources : {}", ids);

        resourceService.deleteAssignmentResources(ids);

        return ResponseEntity.noContent().build();
    }

    // ============ Folder Endpoints ============

    @GetMapping("/course/{courseId}/folders")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','STUDENT')")
    public ResponseEntity<List<ResourceFolderDto>> getCourseFolders(
            @PathVariable int courseId) {

        log.debug("Fetching folders for course: {}", courseId);

        return ResponseEntity.ok(folderService.getFoldersWithResources(courseId, true));
    }

    @GetMapping("/subject/{subjectId}/folders")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN','STUDENT')")
    public ResponseEntity<List<ResourceFolderDto>> getSubjectFolders(
            @PathVariable int subjectId) {

        log.debug("Fetching folders for subject: {}", subjectId);

        return ResponseEntity.ok(folderService.getFoldersWithResources(subjectId, false));
    }

    @PostMapping("/folder")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    public ResponseEntity<ResourceFolderDto> createFolder(
            @RequestBody ResourceFolderRequest req) {

        log.info("Creating folder: {}", req.getName());

        return ResponseEntity.ok(folderService.createFolder(req));
    }

    // ============ Helper Methods ============

    /**
     * Load file from storage as byte array
     */
    private byte[] loadFileAsBytes(String fileName) throws Exception {
        try (InputStream in = fileStorageService.downloadFile(fileName);
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {

            byte[] data = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, bytesRead);
            }

            return buffer.toByteArray();
        } catch (Exception e) {
            log.error("Error loading file: {}", fileName, e);
            throw e;
        }
    }

    /**
     * Resolve media type from extension or type string
     */
    private MediaType resolveMediaType(String type) {
        if (type != null && !type.isEmpty()) {
            try {
                return MediaType.parseMediaType(type);
            } catch (Exception e) {
                log.warn("Failed to parse media type: {}", type);
            }
        }
        return MediaType.APPLICATION_OCTET_STREAM;
    }

    /**
     * Build HTTP headers for file response
     */
    @NotNull
    private HttpHeaders buildHeaders(
            MediaType mediaType,
            byte[] fileBytes,
            Resource resource,
            boolean isDownload) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(mediaType);
        headers.setContentLength(fileBytes.length);

        String disposition = isDownload
                ? String.format("attachment; filename=\"%s\"", resource.getName())
                : String.format("inline; filename=\"%s\"", resource.getName());
        headers.set(HttpHeaders.CONTENT_DISPOSITION, disposition);

        headers.set(HttpHeaders.CACHE_CONTROL, "public, max-age=3600");

        // CORS headers
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST, PUT, DELETE, OPTIONS");
        headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "*");
        headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
                "Content-Type, Content-Disposition, Content-Length");

        return headers;
    }
}