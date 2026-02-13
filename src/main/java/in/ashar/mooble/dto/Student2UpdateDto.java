package in.ashar.mooble.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Student2UpdateDto {

    @NotBlank(message = "Student name must not be blank")
    private String studentName;

    @Email(message = "Student email must be valid")
    @NotBlank(message = "Student email must not be blank")
    private String studentEmail;

    @NotNull(message = "date of birth must be provided")
    private LocalDate dateOfBirth;

    @Length(min = 10, max = 10, message = "Student phone number must be 10 digits")
    private String studentPhoneNumber;

    @NotBlank(message = "Student Address must not be blank")
    private String studentAddress;

    @NotNull(message = "subjectIds field cannot be null")
    private List<Integer> subjectIds;

    @NotNull(message = "courseIds field cannot be null")
    private List<Integer> courseIds;

    @NotNull(message = "parents detail should be provided")
    private ParentsDetailDto parentsDetail;

}
