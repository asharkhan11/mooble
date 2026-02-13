package in.ashar.mooble.entity;

import in.ashar.mooble.utility.enums.AudienceType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "broadcast_messages")
@Data
public class BroadcastMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    
    @Column(name = "tuition_id", nullable = false)
    private int tuitionId;
    
    @Column(name = "sender_id", nullable = false)
    private int adminId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "audience_type", nullable = false)
    private AudienceType audienceType;
    
    @Column(name = "is_urgent")
    private Boolean isUrgent = false;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

}

