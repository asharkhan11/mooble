package in.ashar.mooble.dto;

import in.ashar.mooble.entity.*;
import in.ashar.mooble.utility.enums.Standard;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TuitionClassResponseDto {

    private int tuitionClassId;
    private String standard;
    private char section;

    private int tuitionId;
    private String tuitionName;

    private List<Map<Integer, String>> students;
    private List<Map<Integer, String>> teachers;
    private List<Map<Integer, String>> subjects;

}
