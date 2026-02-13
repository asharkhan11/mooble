package in.ashar.mooble.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "resource")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int resourceId;

    private String name;
    private String fileName;
    private String type;
//    private String url;
    private LocalDateTime uploadedAt;

    @ManyToOne
    @JoinColumn(name = "subjectId")
    private Subject2 subject;

    @ManyToOne
    @JoinColumn(name = "courseId")
    @JsonIgnore
    private Course2 course;

    @ManyToOne
    @JoinColumn(name = "tuitionId")
    private Tuition2 tuition;

    private int uploaderId;

    @ManyToOne
    @JoinColumn(name = "folderId")
    @JsonIgnore
    private ResourceFolder folder;

}

