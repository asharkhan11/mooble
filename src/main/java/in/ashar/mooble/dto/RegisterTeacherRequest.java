package in.ashar.mooble.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterTeacherRequest {

    @NotBlank(message = "Teacher name must not be blank")
    private String teacherName;

    @Email(message = "Teacher email must be valid")
    @NotBlank(message = "Teacher email must not be blank")
    private String teacherEmail;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
    private String teacherPassword;

    @NotNull(message = "date of birth must be provided")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Teacher phone number must not be blank")
    @Length(min = 10, max = 10, message = "Teacher phone number must be 10 digits")
    private String teacherPhoneNumber;

    @NotBlank(message = "Teacher Address must not be blank")
    private String teacherAddress;

    @NotNull(message = "subjects must be provided")
    private List<String> knownSubjects;

    @NotBlank(message = "Experience must be provided")
    private String experience;

}
