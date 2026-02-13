package in.ashar.mooble.utility.helpers;

import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TeacherHelper {

    private final TuitionClassRepository tuitionClassRepository;
    private final GetCurrentUser currentUser;
    private final Teacher2Repository teacherRepository;
    private final Subject2Repository subjectRepository;
    private final MinioClient minioClient;
    private final ResourceRepository resourceRepository;
    private final Course2Repository courseRepository;

    @Value("${minio.bucket}")
    private String BUCKET_NAME;

    public TuitionClass getOwnTuitionClass(int tuitionClassId) {

        String email = currentUser.getLoggedInUserEmail();

        Teacher2 teacher = teacherRepository.findByTeacherCredentialEmail(email).orElseThrow(()-> new NotFoundException("Teacher not found"));

        Optional<TuitionClass> any = teacher.getTuitionClasses().stream().filter(tc -> tc.getTuitionClassId() == tuitionClassId).findAny();

        if(any.isPresent()){
            return any.get();
        }

        throw new UnAuthorizedException("tuition class not found with id : "+tuitionClassId);

    }

//    public Subject2 getOwnSubjectById(int subjectId){
//
//        Subject2 subject = subjectRepository.findById(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));
//
//        String email = currentUser.getLoggedInUserEmail();
//
//        Teacher2 teacher = teacherRepository.findByTeacherCredentialEmail(email).orElseThrow(()-> new NotFoundException("Teacher not found"));
//
//        if(!teacher.getSubjects().contains(subject)){
//            throw new UnAuthorizedException("Invalid subject id : "+ subjectId);
//        }
//
//        return subject;
//    }


//    public Course2 getOwnCourseById(int courseId){
//
//        Course2 course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found"));
//
//        String email = currentUser.getLoggedInUserEmail();
//
//        Teacher2 teacher = teacherRepository.findByTeacherCredentialEmail(email).orElseThrow(()-> new NotFoundException("Teacher not found"));
//
//        if(!teacher.getSubjects().contains(subject)){
//            throw new UnAuthorizedException("Invalid subject id : "+ subjectId);
//        }
//
//        return subject;
//    }


//
//    public Resource addResourceToSubject(Subject2 subject,Teacher2 uploadedBy, MultipartFile file) {
//
//        try {
//
//            // ---- Ensure bucket exists ----
//            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
//                    .bucket(BUCKET_NAME)
//                    .build());
//
//            if (!found) {
//                minioClient.makeBucket(MakeBucketArgs.builder()
//                        .bucket(BUCKET_NAME)
//                        .build());
//            }
//
//            // ---- Upload to MinIO ----
//            String fileName = UUID.randomUUID() + "__" + file.getOriginalFilename();
//            minioClient.putObject(
//                    PutObjectArgs.builder()
//                            .bucket(BUCKET_NAME)
//                            .object(fileName)
//                            .stream(file.getInputStream(), file.getSize(), -1)
//                            .contentType(file.getContentType())
//                            .build()
//            );
//
//            String fileUrl = "/minio/" + BUCKET_NAME + "/" + fileName; // You may construct public URL if needed
//
//            // ---- Save metadata in DB ----
//            Resource resource = Resource.builder()
//                    .subject(subject)
//                    .uploadedBy(uploadedBy)
//                    .name(file.getOriginalFilename())
//                    .type(file.getContentType())
//                    .url(fileUrl)
//                    .uploadedAt(LocalDateTime.now())
//                    .build();
//
//            return resource;
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to upload file to MinIO", e);
//        }
//    }


}
