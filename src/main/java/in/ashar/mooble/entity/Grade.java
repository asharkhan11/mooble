package in.ashar.mooble.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int gradeId;

    private Double score;
    private String remarks;
    private LocalDateTime gradedAt;

    @ManyToOne
    @JoinColumn(name = "submissionId")
    private Submission submission;

    @ManyToOne
    @JoinColumn(name = "gradedByTeacherId")
    private Teacher2 gradedBy;
}
