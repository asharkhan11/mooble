package in.ashar.mooble.dto;

import in.ashar.mooble.entity.Admin2;
import in.ashar.mooble.entity.TuitionClass;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
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
public class Tuition2Dto {

    @NotBlank(message = "tuition name must not be blank")
    private String tuitionName;

    @Email(message = "Email must be valid")
    private String tuitionEmail;

    @Length(min = 10, max = 10, message = "number must be 10 digits")
    private String tuitionPhoneNumber;

    @NotBlank(message = "Address must not be blank")
    private String tuitionAddress;

    private String branch;

    @Positive(message = "Admin Id must be valid")
    private int adminId;

}
