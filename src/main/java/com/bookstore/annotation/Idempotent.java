package com.bookstore.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {
    String value() default "Idempotency-Key";
    String resourceType() default "ORDER";
    int ttlHours() default 24;
}