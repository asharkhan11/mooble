package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "resource_folder_2",
        indexes = {
                @Index(name = "idx_folder_tuition", columnList = "tuition_id"),
                @Index(name = "idx_folder_path", columnList = "path")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceFolder2 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "folder_id")
    private Integer folderId;

    @Column(nullable = false)
    private String name;

    @Column(name = "tuition_id", nullable = false)
    private int tuitionId;

    // exactly one of these may be non-null (or both null for tuition-level folders)
    @Column(name = "course_id")
    private Integer courseId;

    @Column(name = "subject_id")
    private Integer subjectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private ResourceFolder2 parentFolder;

    /**
     * Materialized path
     * Example: /1/4/9
     */
    @Column(length = 512)
    private String path;

    // Optional ordering support (for UI sorting)
    @Column(name = "position")
    private Integer position;
}
