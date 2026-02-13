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
public class Student2ResponseDto {

    private int studentId;
    private String studentName;
    private String studentEmail;
    private String studentPhoneNumber;
    private String studentAddress;
    private LocalDate dateOfBirth;
    private ParentsDetailDto parentsDetail;
    private List<Map<Integer,Integer>> classIdsAndSubjectIds;
    private List<Map<Integer,Integer>> tuitionIdsAndCourseIds;
    private LocalDateTime joined; //yyyy-MM-dd
    private List<Map<Integer, LocalDateTime>> tuitionIdsAndJoinedDate;

}
