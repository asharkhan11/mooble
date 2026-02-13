package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllCount {

    private int totalTuition;
    private int totalTeachers;
    private int totalStudents;
    private int totalTuitionClasses;

}
