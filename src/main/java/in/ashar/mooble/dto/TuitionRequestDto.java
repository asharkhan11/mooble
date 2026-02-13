package in.ashar.mooble.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TuitionRequestDto {

    @NotBlank(message = "tuition name must not be blank")
    private String tuitionName;

    @Email(message = "Email must be valid")
    private String tuitionEmail;

    @Length(min = 10, max = 10, message = "number must be 10 digits")
    private String tuitionPhoneNumber;

    @NotBlank(message = "Address must not be blank")
    private String tuitionAddress;

    private String branch;

}
