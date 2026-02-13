package in.ashar.mooble.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceCreateRequest {

    @NotNull
    private Integer sessionId;
}
