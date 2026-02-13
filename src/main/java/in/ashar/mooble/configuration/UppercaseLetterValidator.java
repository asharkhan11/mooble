package in.ashar.mooble.configuration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class UppercaseLetterValidator implements ConstraintValidator<UppercaseLetter, Character> {

    @Override
    public boolean isValid(Character value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotNull will handle nulls separately
        }
        return value >= 'A' && value <= 'Z';
    }
}
