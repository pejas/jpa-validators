package me.pejas.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Piotr Pejas
 */
@Target( {ElementType.METHOD, ElementType.FIELD, ANNOTATION_TYPE })
@Retention(RUNTIME)
@Constraint(validatedBy = PeselValidator.class)
@Documented
public @interface Pesel {
  boolean allowPastNow() default false;
  boolean allowBefore1850() default false;
  String message() default "{com.mycompany.constraints.checkcase}";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}
