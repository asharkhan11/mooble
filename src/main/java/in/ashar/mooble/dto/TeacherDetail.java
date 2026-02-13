package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherDetail {

    private int teacherId;
    private String teacherName;
    private String phoneNumber;
    private String experience;
    private List<String> subjects;

}
