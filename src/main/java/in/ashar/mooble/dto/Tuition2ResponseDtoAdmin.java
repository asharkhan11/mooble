package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tuition2ResponseDtoAdmin {

    private int tuitionId;
    private String tuitionName;
    private String branch;

}
