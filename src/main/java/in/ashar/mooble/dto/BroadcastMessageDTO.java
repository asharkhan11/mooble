package in.ashar.mooble.dto;

import in.ashar.mooble.utility.enums.AudienceType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BroadcastMessageDTO {
    private int id;
    private String title;
    private String message;
    private String senderName;
    private AudienceType audienceType;
    private Boolean isUrgent;
    private Boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}