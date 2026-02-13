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
public class Admin2ResponseDto {

    private int adminId;
    private String adminName;
    private String adminEmail;
    private String adminPhoneNumber;
    private String adminAddress;
    private List<Tuition2ResponseDtoAdmin> adminTuition;

}
