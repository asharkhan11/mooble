package in.ashar.mooble.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentAttendanceSummaryResponse {

    private int totalSessions;
    private int presentCount;
    private int absentCount;
    private int lateCount;
    private int excusedCount;

    private double attendancePercentage;
}
