package com.blerinahouse.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class DateRangeValidator implements ConstraintValidator<ValidDateRange, Object> {

    private String startField;
    private String endField;
    private String message;

    @Override
    public void initialize(ValidDateRange annotation) {
        this.startField = annotation.start();
        this.endField = annotation.end();
        this.message = annotation.message();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext ctx) {
        if (value == null) return true;
        try {
            // Record accessor: metoda quhet saktësisht si komponenti (p.sh. checkInDate())
            LocalDate start = (LocalDate) value.getClass().getMethod(startField).invoke(value);
            LocalDate end = (LocalDate) value.getClass().getMethod(endField).invoke(value);

            if (start == null || end == null) return true;   // @NotNull i mbulon null-et
            if (end.isAfter(start)) return true;

            // Lidh gabimin me fushën përfundimtare -> fieldErrors[].field = endField
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(endField)
                    .addConstraintViolation();
            return false;
        } catch (ReflectiveOperationException e) {
            return false;   // emër fushe i gabuar në anotim -> dështon (konfigurim i gabuar)
        }
    }
}