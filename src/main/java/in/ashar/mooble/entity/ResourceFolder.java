package in.ashar.mooble.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResourceFolder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int folderId;

    private String name;

    private int courseId;

    private int subjectId;

    private int tuitionId;

    @ManyToOne
    @JoinColumn(name = "parentFolderId")
    private ResourceFolder parentFolder; // Optional for nested folders

    @OneToMany(mappedBy = "folder")
    private List<Resource> resources;
}