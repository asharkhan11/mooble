package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignmentResponseDto {

    private int id;
    private String title;
    private String description;
    private LocalDate assignedDate;
    private LocalDate dueDate;
    private int maxMarks;
    private List<Integer> resourceIds;
    private int teacherId;
    private String teacherName;
    private int subjectId;
    private String subjectName;
    private int courseId;
    private String courseName;

}
