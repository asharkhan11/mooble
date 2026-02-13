package in.ashar.mooble.dto;

import in.ashar.mooble.utility.enums.AudienceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BroadcastMessageRequest {
    @NotNull
    private int tuitionId;
    
    @NotBlank
    @Size(max = 255)
    private String title;
    
    @NotBlank
    private String message;
    
    @NotNull
    private AudienceType audienceType;
    
    private Boolean isUrgent = false;
}

