package in.ashar.mooble.dto;

import in.ashar.mooble.configuration.UppercaseLetter;
import in.ashar.mooble.utility.enums.Standard;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TuitionClassUpdateDto {

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Standard must not be null")
    private Standard standard;

    @UppercaseLetter
    @NotNull(message = "Section must not be null")
    private char section;

    private List<Integer> studentIds;
    private List<Integer> teacherIds;
    private List<Integer> subjectIds;
}
