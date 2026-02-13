package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDetail {

    private int studentId;
    private String studentName;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private int totalClassesEnrolled;

}
