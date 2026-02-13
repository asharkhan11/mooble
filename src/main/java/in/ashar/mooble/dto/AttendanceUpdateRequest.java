package in.ashar.mooble.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class AttendanceUpdateRequest {

    @NotEmpty
    private List<AttendanceEntryRequest> entries;
}
