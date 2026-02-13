package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherResponseDto {

    private int teacherId;
    private String teacherName;
    private String teacherEmail;
    private String teacherPhoneNumber;
    private String teacherAddress;
    private LocalDate dateOfBirth;
    private String experience;
    private List<String> knownSubjects;
    private List<Map<Integer,Integer>> classIdsAndSubjectIds;
    private List<Map<Integer,Integer>> tuitionIdsAndCourseIds;
    private LocalDateTime joined; //yyyy-MM-dd
    private List<Map<Integer, LocalDateTime>> tuitionIdsAndJoinedDate;

}
