package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentResource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int assignmentResourceId;

    private String name;
    private String fileName;
    private String type;
    private LocalDateTime uploadedAt;
    private int uploaderId;

}
