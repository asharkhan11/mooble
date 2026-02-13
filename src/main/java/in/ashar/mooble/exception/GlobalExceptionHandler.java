package in.ashar.mooble.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler{

    private final ErrorResponse errorResponse;

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        errorResponse.setCode("CONFLICT");
        errorResponse.setReason(ex.getMessage());
        return ResponseEntity.status(409).body(errorResponse);
    }


    @ExceptionHandler(PlanLimitExceededException.class)
    public ResponseEntity<ErrorResponse> planLimitExceeded(PlanLimitExceededException ex) {
        errorResponse.setCode("PAYMENT REQUIRED");
        errorResponse.setReason(ex.getMessage());
        return ResponseEntity.status(402).body(errorResponse);
    }
    

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> jsonParsingError(HttpMessageNotReadableException ex){
        errorResponse.setCode("BAD REQUEST");
        errorResponse.setReason(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidOptionException.class)
    public ResponseEntity<ErrorResponse> generalExceptionHandler(InvalidOptionException ex){
        errorResponse.setCode("INVALID REQUEST");
        errorResponse.setReason(ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AlreadyExists.class)
    public ResponseEntity<ErrorResponse> alreadyExistsExceptionHandler(AlreadyExists ex){
        errorResponse.setCode("CONFLICT");
        errorResponse.setReason(ex.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validationError(MethodArgumentNotValidException ex){
        errorResponse.setCode("BAD REQUEST");
        errorResponse.setReason(ex.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> notFoundException(NotFoundException ex){
        errorResponse.setCode("NOT FOUND");
        errorResponse.setReason(ex.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnAuthorizedException.class)
    public ResponseEntity<ErrorResponse> unAuthorizedException(UnAuthorizedException ex){
        errorResponse.setCode("UNAUTHORIZED");
        errorResponse.setReason(ex.getMessage());
        return new ResponseEntity<>(errorResponse,HttpStatus.FORBIDDEN);
    }


}
