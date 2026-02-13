package in.ashar.mooble.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class JoinRequestTuition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int requestId;

    private int tuitionCode;
    @Column(nullable = false, updatable = false)
    private LocalDateTime requestedOn;

    @JsonProperty("isTeacher")
    private boolean isTeacher;
    private int userId; // isTeacher ? teacherId : studentId;

    @Enumerated(EnumType.STRING)
    private JoinStatus status = JoinStatus.PENDING;



    @PrePersist
    void onCreate() {
        this.requestedOn = LocalDateTime.now();
    }

    public enum JoinStatus{ PENDING, APPROVED, REGRET }
}
