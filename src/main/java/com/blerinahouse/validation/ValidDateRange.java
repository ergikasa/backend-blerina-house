package com.blerinahouse.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DateRangeValidator.class)
@Target(ElementType.TYPE)              // në nivel klase/record
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDateRange {

    String message() default "End date must be after start date";

    String start();                    // emri i fushës fillestare
    String end();                      // emri i fushës përfundimtare

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}