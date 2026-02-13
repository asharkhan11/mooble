package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "resource_2",
        indexes = {
                @Index(name = "idx_resource_tuition", columnList = "tuition_id"),
                @Index(name = "idx_resource_folder", columnList = "folder_id"),
                @Index(name = "idx_resource_file", columnList = "file_name")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Resource2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private int resourceId;

    // Original file name (shown in UI)
    @Column(nullable = false)
    private String name;

    // MIME type
    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private long size;

    /**
     * Full MinIO object key
     * Example: tuition-12/uuid-filename.pdf
     */
    @Column(name = "file_name", nullable = false, unique = true, length = 512)
    private String fileName;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "tuition_id", nullable = false)
    private int tuitionId;

    // exactly one of these must be non-null
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "subject_id")
    private Integer subjectId;

    // adminId / teacherId (role-agnostic)
    @Column(name = "credential_id", nullable = false)
    private int credentialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id")
    private ResourceFolder2 folder;
}

