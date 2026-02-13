package in.ashar.mooble.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tuition2ResponseDto {

    private int tuitionId;
    private int tuitionCode;
    private String tuitionName;
    private String tuitionEmail;
    private String tuitionPhoneNumber;
    private String tuitionAddress;
    private String branch;

    private int adminId;
    private String adminName;
    private String adminEmail;

    private List<Integer> tuitionClassIds;
    private List<Integer> studentIds;
    private List<Integer> teacherIds;

}
