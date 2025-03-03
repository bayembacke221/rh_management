package sn.bmbacke.rh.validation;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SenegalPhoneValidator implements ConstraintValidator<SenegalPhone, String> {

    private final ValidationService validationService;

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        if (phoneNumber == null) {
            return true;
        }
        return validationService.isValidSenegalPhoneNumber(phoneNumber);
    }
}
