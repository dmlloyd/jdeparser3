package com.example.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Documented;

/**
 * Marks a method as a REST endpoint with routing and content-type configuration.
 *
 * @since 2.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RestEndpoint {
    String path();

    String method() default "GET";

    String produces() default "application/json";

    String consumes() default "application/json";

    int timeout() default 30;

    boolean async() default false;

    /**
     * The API version.
     */
    String VERSION = "2.0";
}
