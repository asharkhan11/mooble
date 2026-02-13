package in.ashar.mooble.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class StudentAttendanceResponse {

    private StudentAttendanceSummaryResponse summary;
    private List<StudentAttendanceSessionResponse> sessions;
}
