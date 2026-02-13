package in.ashar.mooble.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class StudentAttendanceRequest {

    private LocalDate startDate;
    private LocalDate endDate;

    // optional future filters
    private Integer subjectId;
    private Integer courseId;
}
