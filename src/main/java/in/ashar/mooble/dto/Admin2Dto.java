package in.ashar.mooble.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Admin2Dto {

    @NotBlank(message = "name must not be blank")
    private String adminName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be blank")
    private String adminEmail;

    @NotBlank(message = "phone number must be provided")
    @Length(min = 10, max = 10, message = "number must be 10 digits")
    private String adminPhoneNumber;

    @NotBlank(message = "Address must not be blank")
    private String adminAddress;

}
