package in.ashar.mooble.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Subject2Dto {

    @NotBlank(message = "Subject name must not be blank")
    private String subjectName;

    @Positive(message = "Tuition class id must be valid")
    @NotNull(message = "tuition class id must be provided")
    private int tuitionClassId;

    private List<MultipartFile> files;

}
