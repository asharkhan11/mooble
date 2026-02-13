package in.ashar.mooble.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterStudentRequest {

    @NotBlank(message = "Student name must not be blank")
    private String studentName;

    @Email(message = "Student email must be valid")
    @NotBlank(message = "Student email must not be blank")
    private String studentEmail;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 64, message = "Password must be at least 8 characters")
    private String studentPassword;

    @NotNull(message = "date of birth must be provided")
    private LocalDate dateOfBirth;

    @Length(min = 10, max = 10, message = "Student phone number must be 10 digits")
    private String studentPhoneNumber;

    @NotBlank(message = "Student Address must not be blank")
    private String studentAddress;

    @NotNull(message = "parents detail should be provided")
    @Valid
    private ParentsDetailDto parentsDetail;


}
