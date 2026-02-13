package in.ashar.mooble.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subject2ResponseDto {

    private int subjectId;
    private String subjectName;

    private String standard;
    private char section;
    private List<Integer> studentIds;
    private List<Integer> teacherIds;
    private List<Integer> resourceIds;
    private int tuitionClassId;

}
