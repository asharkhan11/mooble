package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Submission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int submissionId;

    @ManyToOne
    @JoinColumn(name = "studentId")
    private Student2 student;

    @ManyToOne
    @JoinColumn(name = "assignmentId")
    private Assignment assignment;


    private LocalDateTime submittedOn;
    private LocalDateTime gradedOn;

    @ElementCollection
    private List<Integer> resourceIds;
    private int marksObtained;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Submission.SubmissionStatus status = Submission.SubmissionStatus.NOT_SUBMITTED;


    public enum SubmissionStatus{
        NOT_SUBMITTED ,SUBMITTED, GRADED, LATE
    }
}

