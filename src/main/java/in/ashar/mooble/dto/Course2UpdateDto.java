package in.ashar.mooble.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course2UpdateDto {

    @NotBlank(message = "course name must be provided")
    private String courseName;

    @NotBlank(message = "course duration must be provided")
    private String courseDuration;

    @NotNull(message = "subject ids can not be null")
    private List<Integer> subjectIds;

}
