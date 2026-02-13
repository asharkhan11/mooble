package in.ashar.mooble.dto;


import in.ashar.mooble.configuration.UppercaseLetter;
import in.ashar.mooble.utility.enums.Standard;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TuitionClassDto {

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Standard must not be null")
    private Standard standard;

    @UppercaseLetter
    @NotNull(message = "Section must not be null")
    private char section;

    @Positive(message = "Tuition Id must be valid")
    @NotNull(message = "tuition id must be provided")
    private int tuitionId;

}
