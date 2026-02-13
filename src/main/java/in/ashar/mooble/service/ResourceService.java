package in.ashar.mooble.service;

import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final FileStorageService fileStorageService;
    private final ResourceRepository resourceRepository;
    private final Course2Repository courseRepository;
    private final Subject2Repository subjectRepository;
    private final Admin2Repository adminRepository;
    private final Teacher2Repository teacherRepository;
    private final GetCurrentUser currentUser;
    private final Tuition2Repository tuitionRepository;
    private final ResourceFolderRepository folderRepository;
    private final AssignmentResourceRepository assignmentResourceRepository;


    public AssignmentResource uploadAssignmentResource(MultipartFile file) throws Exception {

        Object user = currentUser.getLoggedInUser();

        String fileName = fileStorageService.uploadFile(file);

        // ✅ Detect MIME type properly
        String mimeType = detectMimeType(file);

        AssignmentResource resource = new AssignmentResource();

        resource.setName(file.getOriginalFilename());
        resource.setFileName(fileName);
        resource.setType(mimeType);
        resource.setUploadedAt(LocalDateTime.now());

        if (user instanceof Student2 student) {

            resource.setUploaderId(student.getStudentId());

        } else if (user instanceof Teacher2 teacher) {

            resource.setUploaderId(teacher.getTeacherId());

        } else {
            throw new UnAuthorizedException("Access Deniend");
        }

        return assignmentResourceRepository.save(resource);

    }


    public Resource uploadResource(MultipartFile file, int tuitionId, Integer courseId, Integer subjectId, Integer folderId, boolean isAdmin) throws Exception {
        if ((courseId == null && subjectId == null) || (courseId != null && subjectId != null)) {
            throw new IllegalArgumentException("Provide either courseId or subjectId, not both");
        }

        String email = currentUser.getLoggedInUserEmail();
        int uploaderId = 0;
        Course2 course = null;
        Subject2 subject = null;

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("tuition not found"));

        if (isAdmin) {
            Admin2 admin = adminRepository.findByAdminEmail(email).orElseThrow(() -> new NotFoundException("Admin not found"));

            uploaderId = admin.getAdminId();

            if (tuition.getTuitionAdmin().getAdminId() != admin.getAdminId()) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            if (courseId != null) {
                course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid courseId"));

                if (!admin.getAdminTuition().contains(course.getTuition())) {
                    throw new UnAuthorizedException("Invalid course id");
                }


            } else {
                subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid subjectId"));

                if (!admin.getAdminTuition().stream().flatMap(t -> t.getTuitionClasses().stream()).collect(Collectors.toSet()).contains(subject.getTuitionClass())) {
                    throw new UnAuthorizedException("Invalid subject id");
                }
            }

        } else {
            Teacher2 teacher = teacherRepository.findByTeacherCredentialEmail(email).orElseThrow(() -> new NotFoundException("Teacher not found"));

            uploaderId = teacher.getTeacherId();

            if (!tuition.getTeacherIds().contains(teacher.getTeacherId())) {
                throw new UnAuthorizedException("Invalid teacher id");
            }

            if (courseId != null) {
                course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid courseId"));

                if (!teacher.getCourses().contains(course)) {
                    throw new UnAuthorizedException("Invalid course id");
                }


            } else {
                subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid subjectId"));

                if (!teacher.getSubjects().contains(subject)) {
                    throw new UnAuthorizedException("Invalid subject id");
                }
            }

        }

        ResourceFolder folder = null;
        if (folderId != null) {
            folder = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));
        }

        String fileName = fileStorageService.uploadFile(file);

        // ✅ Detect MIME type properly
        String mimeType = detectMimeType(file);

        Resource resource = Resource.builder()
                .name(file.getOriginalFilename())
                .type(mimeType)
                .fileName(fileName)
                .uploadedAt(LocalDateTime.now())
                .tuition(tuition)
                .course(course)
                .subject(subject)
                .uploaderId(uploaderId)
                .folder(folder)
                .build();

        return resourceRepository.save(resource);
    }


    public List<Resource> uploadMultipleResources(
            List<MultipartFile> files,
            int tuitionId,
            Integer courseId,
            Integer subjectId,
            Integer folderId,
            boolean isAdmin
    ) throws Exception {
        if ((courseId == null && subjectId == null) || (courseId != null && subjectId != null)) {
            throw new IllegalArgumentException("Provide either courseId or subjectId, not both");
        }

        String email = currentUser.getLoggedInUserEmail();
        int uploaderId;
        Course2 course = null;
        Subject2 subject = null;

        Tuition2 tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new NotFoundException("Tuition not found"));

        if (isAdmin) {
            Admin2 admin = adminRepository.findByAdminEmail(email)
                    .orElseThrow(() -> new NotFoundException("Admin not found"));
            uploaderId = admin.getAdminId();

            if (tuition.getTuitionAdmin().getAdminId() != admin.getAdminId()) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            if (courseId != null) {
                course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid courseId"));

                if (!admin.getAdminTuition().contains(course.getTuition())) {
                    throw new UnAuthorizedException("Invalid course id");
                }
            } else {
                subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid subjectId"));

                boolean valid = admin.getAdminTuition().stream()
                        .flatMap(t -> t.getTuitionClasses().stream())
                        .collect(Collectors.toSet())
                        .contains(subject.getTuitionClass());

                if (!valid) {
                    throw new UnAuthorizedException("Invalid subject id");
                }
            }

        } else {
            Teacher2 teacher = teacherRepository.findByTeacherCredentialEmail(email)
                    .orElseThrow(() -> new NotFoundException("Teacher not found"));
            uploaderId = teacher.getTeacherId();

            if (!tuition.getTeacherIds().contains(teacher.getTeacherId())) {
                throw new UnAuthorizedException("Invalid teacher id");
            }

            if (courseId != null) {
                course = courseRepository.findById(courseId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid courseId"));

                if (!teacher.getCourses().contains(course)) {
                    throw new UnAuthorizedException("Invalid course id");
                }
            } else {
                subject = subjectRepository.findById(subjectId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid subjectId"));

                if (!teacher.getSubjects().contains(subject)) {
                    throw new UnAuthorizedException("Invalid subject id");
                }
            }
        }

        List<Resource> uploadedResources = new ArrayList<>();
        ResourceFolder folder = folderRepository.findById(folderId).orElseThrow(() -> new NotFoundException("Folder not found"));

        for (MultipartFile file : files) {
            String fileName = fileStorageService.uploadFile(file);

            // ✅ Detect MIME type properly
            String mimeType = detectMimeType(file);

            Resource resource = Resource.builder()
                    .name(file.getOriginalFilename())
                    .type(mimeType)
                    .fileName(fileName)
                    .uploadedAt(LocalDateTime.now())
                    .tuition(tuition)
                    .course(course)
                    .subject(subject)
                    .uploaderId(uploaderId)
                    .folder(folder)
                    .build();

            uploadedResources.add(resourceRepository.save(resource));
        }

        return uploadedResources;
    }


    public List<Resource> getAllResources(int tuitionId) {

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found"));

        Object user = currentUser.getLoggedInUser();
        if (user instanceof Student2 student) {

            if (!tuition.getStudentIds().contains(student.getStudentId())) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            List<Subject2> subjects = student.getSubjects();
            List<Course2> courses = student.getCourses();

            return resourceRepository.findAllByCourseInOrSubjectIn(courses, subjects);

        } else if (user instanceof Teacher2 teacher) {

            if (!tuition.getTeacherIds().contains(teacher.getTeacherId())) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            List<Subject2> subjects = teacher.getSubjects();
            List<Course2> courses = teacher.getCourses();


            return resourceRepository.findAllByCourseInOrSubjectIn(courses, subjects);

        } else if (user instanceof Admin2 admin) {

            if (!admin.getAdminTuition().contains(tuition)) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            return resourceRepository.findAllByTuition(tuition);

        } else {
            throw new UnAuthorizedException("Invalid User");
        }
    }

    public Resource getResourceById(int id) {

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Student2 student) {

            boolean authorized = (resource.getSubject() != null)
                    ? student.getSubjects().stream()
                    .anyMatch(s -> s.getSubjectId() == resource.getSubject().getSubjectId())
                    : student.getCourses().stream()
                    .anyMatch(c -> c.getCourseId() == resource.getCourse().getCourseId());

            if (!authorized) throw new UnAuthorizedException("Invalid resource id");

        } else if (user instanceof Teacher2 teacher) {

            boolean authorized = (resource.getSubject() != null)
                    ? teacher.getSubjects().stream()
                    .anyMatch(s -> s.getSubjectId() == resource.getSubject().getSubjectId())
                    : teacher.getCourses().stream()
                    .anyMatch(c -> c.getCourseId() == resource.getCourse().getCourseId());

            if (!authorized) throw new UnAuthorizedException("Invalid resource id");

        } else if (user instanceof Admin2 admin) {

            boolean authorized = admin.getAdminTuition().stream()
                    .anyMatch(t -> t.getTuitionId() == resource.getTuition().getTuitionId());

            if (!authorized) throw new UnAuthorizedException("Invalid resource id");

        } else {
            throw new UnAuthorizedException("Invalid User");
        }

        return resource;
    }


    public AssignmentResource getAssignmentResourceById(int id) {

        AssignmentResource resource = assignmentResourceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        currentUser.getLoggedInUser();

        return resource;
    }

    public List<AssignmentResource> getAssignmentResourceByIds(List<Integer> ids) {

        List<AssignmentResource> resources = assignmentResourceRepository.findAllById(ids);

        currentUser.getLoggedInUser();

        return resources;
    }


    public InputStream downloadResource(String fileName) throws Exception {
//        try (InputStream is = fileStorageService.downloadFile(fileName)) {
//            return is.readAllBytes();
//        }
        return fileStorageService.downloadFile(fileName);
    }

    public void deleteResource(int id) throws Exception {

        Resource resource = resourceRepository.findById(id).orElseThrow(() -> new NotFoundException("resource not found"));

        Object user = currentUser.getLoggedInUser();


        if (user instanceof Admin2 admin) {

            if (!admin.getAdminTuition().contains(resource.getTuition())) {
                throw new UnAuthorizedException("Invalid resource id");
            }

        } else if (user instanceof Teacher2 teacher) {

            if (resource.getUploaderId() != teacher.getTeacherId()) {
                throw new UnAuthorizedException("Access denied");
            }

        } else if (user instanceof Student2 student) {
            if (resource.getUploaderId() != student.getStudentId()) {
                throw new UnAuthorizedException("Access denied");
            }
        } else {
            throw new UnAuthorizedException("Invalid User");
        }

        fileStorageService.deleteFile(resource.getFileName());
        resourceRepository.delete(resource);
    }


    public void deleteSubmissionResources(List<Integer> resourceIds) {

        List<Resource> resources = resourceRepository.findAllById(resourceIds);

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {

            fileStorageService.deleteAllFiles(resources.stream().map(Resource::getFileName).toList());
            resourceRepository.deleteAllById(resourceIds);

        } else {
            throw new UnAuthorizedException("Invalid User");
        }

    }


    /// ////////////////////////////////////


    // ✅ Detect MIME type from filename
    private String detectMimeType(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return "application/octet-stream";
        }

        String extension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot + 1).toLowerCase();
        }

        // Map extensions to MIME types
        return switch (extension) {
            // Images
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "bmp" -> "image/bmp";

            // Videos
            case "mp4" -> "video/mp4";
            case "webm" -> "video/webm";
            case "mov" -> "video/quicktime";
            case "avi" -> "video/x-msvideo";

            // Audio
            case "mp3" -> "audio/mpeg";
            case "wav" -> "audio/wav";
            case "ogg" -> "audio/ogg";

            // Documents
            case "pdf" -> "application/pdf";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "txt" -> "text/plain";

            // Default
            default -> {
                String contentType = file.getContentType();
                yield (contentType != null && !contentType.equals("application/octet-stream"))
                        ? contentType
                        : "application/octet-stream";
            }
        };
    }

    public void deleteAssignmentResource(int id) throws Exception {
        AssignmentResource resource = assignmentResourceRepository.findById(id).orElseThrow(() -> new NotFoundException("Assignment Resourcce not found"));

        Object user = currentUser.getLoggedInUser();

        int uploaderId;

        if (user instanceof Student2 student) {
            uploaderId = student.getStudentId();
        } else if (user instanceof Teacher2 teacher) {
            uploaderId = teacher.getTeacherId();
        } else if (user instanceof Admin2 admin) {
            uploaderId = admin.getAdminId();
        } else {
            throw new UnAuthorizedException("Invalid user");
        }

        if (resource.getUploaderId() != uploaderId) {
            throw new UnAuthorizedException("Access denied");
        }

        fileStorageService.deleteFile(resource.getFileName());
        assignmentResourceRepository.deleteById(resource.getAssignmentResourceId());
    }

    public void deleteAssignmentResources(List<Integer> ids) {

        List<AssignmentResource> resources = assignmentResourceRepository.findAllById(ids);
        Object user = currentUser.getLoggedInUser();

        int uploaderId;

        if (user instanceof Student2 student) {
            uploaderId = student.getStudentId();
        } else if (user instanceof Teacher2 teacher) {
            uploaderId = teacher.getTeacherId();
        } else if (user instanceof Admin2 admin) {
            uploaderId = admin.getAdminId();
        } else {
            throw new UnAuthorizedException("Invalid user");
        }

        for (AssignmentResource resource : resources) {
            if (resource.getUploaderId() != uploaderId) {
                throw new UnAuthorizedException("Access denied");
            }
        }


        fileStorageService.deleteAllFiles(resources.stream().map(AssignmentResource::getFileName).toList());
        assignmentResourceRepository.deleteAllById(resources.stream().map(AssignmentResource::getAssignmentResourceId).toList());

    }


}
