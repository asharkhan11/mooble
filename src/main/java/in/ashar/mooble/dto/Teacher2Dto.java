package in.ashar.mooble.dto;

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
public class Teacher2Dto {

    @NotBlank(message = "Teacher name must not be blank")
    private String teacherName;

    @Email(message = "Teacher email must be valid")
    @NotBlank(message = "Teacher email must not be blank")
    private String teacherEmail;

    @NotNull(message = "date of birth must be provided")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Teacher phone number must not be blank")
    @Length(min = 10, max = 10, message = "Teacher phone number must be 10 digits")
    private String teacherPhoneNumber;

    @NotBlank(message = "Teacher Address must not be blank")
    private String teacherAddress;

    @NotBlank(message = "Experience must be provided")
    private String experience;


    private List<Integer> subjectIds = new ArrayList<>();


    private List<Integer> courseIds = new ArrayList<>();

}
