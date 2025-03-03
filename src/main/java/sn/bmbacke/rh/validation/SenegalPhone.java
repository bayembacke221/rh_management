package sn.bmbacke.rh.validation;



import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SenegalPhoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SenegalPhone {
    String message() default "Invalid Senegal phone number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
