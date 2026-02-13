package in.ashar.mooble.configuration;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = UppercaseLetterValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface UppercaseLetter {

    String message() default "Section must be a single uppercase letter (A-Z)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
