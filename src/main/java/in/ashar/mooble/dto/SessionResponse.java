package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SessionResponse {
    private Integer id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;

    private Integer tuitionClassId;
    private String tuitionClassName;
    private Integer subjectId;
    private String subjectName;

    private Integer tuitionId;
    private String tuitionName;
    private Integer courseId;
    private String courseName;

    private Integer teacherId;
    private String teacherName;

    private String status;
    private String recurrenceGroupId;
}
