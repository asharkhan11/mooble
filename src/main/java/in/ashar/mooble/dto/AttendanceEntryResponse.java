package in.ashar.mooble.dto;

import in.ashar.mooble.entity.AttendanceEntry.AttendanceMark;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttendanceEntryResponse {

    private int studentId;
    private String studentName;
    private AttendanceMark mark;
}
