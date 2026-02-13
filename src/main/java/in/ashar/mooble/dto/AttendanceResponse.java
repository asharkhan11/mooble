package in.ashar.mooble.dto;

import in.ashar.mooble.dto.AttendanceEntryResponse;
import in.ashar.mooble.entity.Attendance.AttendanceStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AttendanceResponse {

    private int attendanceId;
    private int sessionId;

    private AttendanceStatus status;
    private LocalDateTime markedAt;

    private List<AttendanceEntryResponse> entries;
}
