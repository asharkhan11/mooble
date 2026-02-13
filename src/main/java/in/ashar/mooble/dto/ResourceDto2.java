package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResourceDto2 {

    private int resourceId;
    private String name;
    private String type;
    private long size;
    private LocalDateTime uploadedAt;
}
