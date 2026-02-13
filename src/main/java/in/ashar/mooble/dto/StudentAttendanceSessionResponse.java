package in.ashar.mooble.dto;

import in.ashar.mooble.entity.AttendanceEntry.AttendanceMark;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class StudentAttendanceSessionResponse {

    private int sessionId;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private String subjectName;
    private String courseName; // nullable

    private AttendanceMark mark;
}
