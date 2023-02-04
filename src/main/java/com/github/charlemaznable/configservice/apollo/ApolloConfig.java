package com.github.charlemaznable.configservice.apollo;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ApolloConfig {

    /**
     * default "application"  when annotated on ElementType.TYPE
     * default ""             when annotated on ElementType.METHOD
     */
    String namespace() default "";

    @AliasFor("value")
    String propertyName() default "";

    @AliasFor("propertyName")
    String value() default "";

    /**
     * effective when annotated on ElementType.METHOD
     */
    String defaultValue() default "";

    long cacheSeconds() default 0;
}
