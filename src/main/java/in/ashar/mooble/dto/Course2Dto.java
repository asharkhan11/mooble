package in.ashar.mooble.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Course2Dto {

    @NotBlank(message = "course name must be provided")
    private String courseName;

    @NotBlank(message = "course duration must be provided")
    private String courseDuration;

    @Positive(message = "tuition id must be valid number")
    @NotNull(message = "tuition id must be provided")
    private int tuitionId;

    private List<MultipartFile> resources;

    private List<Integer> subjectIds;


}
