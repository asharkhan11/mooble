package in.ashar.mooble.dto;

import in.ashar.mooble.entity.AttendanceEntry.AttendanceMark;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AttendanceEntryRequest {

    @NotNull
    private Integer studentId;

    @NotNull
    private AttendanceMark mark;
}
