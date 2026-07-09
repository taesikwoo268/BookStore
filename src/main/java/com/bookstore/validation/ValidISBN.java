package com.bookstore.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = com.bookstore.validation.ISBNValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidISBN {

    String message() default "Invalid ISBN format. Must be a valid ISBN-13 (e.g., 978-3-16-148410-0)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}