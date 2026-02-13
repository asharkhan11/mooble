package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BroadCastMessageResponse {

    private int id;
    private int tuitionId;
    private String tuitionName;
    private String title;
    private String message;
    private String audienceType;
    private boolean isUrgent;
    private LocalDateTime announceDate;

}
