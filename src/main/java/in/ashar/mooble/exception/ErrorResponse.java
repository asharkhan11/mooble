package in.ashar.mooble.exception;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class ErrorResponse{

    private String code;
    private String reason;
}
