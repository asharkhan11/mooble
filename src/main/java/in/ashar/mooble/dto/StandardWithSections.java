package in.ashar.mooble.dto;

import in.ashar.mooble.configuration.UppercaseLetter;
import in.ashar.mooble.utility.enums.Standard;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StandardWithSections {

    @NotNull(message = "student Class/Standard must not be Null")
    private Standard teacherStandard;

    @UppercaseLetter
    @NotNull(message = "Section must not be null")
    private List<Character> sections;

}
