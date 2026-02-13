package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_reads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;
    
    @Column(name = "tuition_id", nullable = false)
    private Integer tuitionId;
    
    @Column(name = "last_read_at", nullable = false)
    private LocalDateTime lastReadAt;
    
    @PrePersist
    protected void onCreate() {
        lastReadAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastReadAt = LocalDateTime.now();
    }
}
