package in.ashar.mooble.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterAdminRequest {

    @NotBlank(message = "name must not be blank")
    private String adminName;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be blank")
    private String adminEmail;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
    private String adminPassword;

    @NotBlank(message = "phone number must be provided")
    @Length(min = 10, max = 10, message = "number must be 10 digits")
    private String adminPhoneNumber;

    @NotBlank(message = "Address must not be blank")
    private String adminAddress;

}
