package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TuitionDetails {

    private int tuitionCode;
    private String tuitionName;
    private String branch;
    private String address;
    private String email;
    private String tuitionPhone;
    private String adminName;
    private String adminPhone;
    private int totalStudents;
    private int totalTeacher;

}
