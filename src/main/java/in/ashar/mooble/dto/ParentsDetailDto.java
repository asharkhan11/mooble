package in.ashar.mooble.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParentsDetailDto {

    @NotBlank(message = "Parent name must not be blank")
    private String name;

    @NotBlank(message = "Relation with student should be provided")
    private String relation;

    @NotBlank(message = "Parent phone number must be provided")
    @Length(min = 10, max = 10, message = "Student phone number must be 10 digits")
    private String phone;

    @NotBlank(message = "Occupation of a parent should not be blank")
    private String occupation;

    @NotBlank(message = "Parent Address must not be blank")
    private String address;

}
