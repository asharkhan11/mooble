package in.ashar.mooble.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcement_reads")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRead {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "teacher_id", nullable = false)
    private Integer teacherId;
    
    @Column(name = "announcement_id", nullable = false)
    private Integer announcementId;
    
    @Column(name = "tuition_id", nullable = false)
    private Integer tuitionId;
    
    @Column(name = "read_at", nullable = false)
    private LocalDateTime readAt = LocalDateTime.now();

}
