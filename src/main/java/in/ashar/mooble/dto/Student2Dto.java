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
public class Student2Dto {

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


    @NotNull(message = "tuition class must not be Null")
    @Positive(message = "tuition class id must be positive")
    private int tuitionClassId;

    @NotNull(message = "parents detail should be provided")
    @Valid
    private ParentsDetailDto parentsDetail;

    private List<Integer> subjectIds = new ArrayList<>();

    private List<Integer> courseIds = new ArrayList<>();

}
