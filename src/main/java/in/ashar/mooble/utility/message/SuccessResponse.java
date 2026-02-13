package in.ashar.mooble.utility.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SuccessResponse<T> {
    private String status; // SUCCESS , FAILURE
    private String detail;
    private T data;
}
