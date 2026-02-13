package in.ashar.mooble.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SessionUpdate {

    @NotNull
    private LocalTime startTime;

    @NotNull
    private LocalTime endTime;

    private Integer subjectId;
    private Integer courseId;

    @NotNull
    private Integer teacherId;

    @NotNull
    private boolean updateAll;

}
