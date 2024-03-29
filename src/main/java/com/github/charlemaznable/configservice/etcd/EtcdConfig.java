package com.github.charlemaznable.configservice.etcd;

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
public @interface EtcdConfig {

    /**
     * default "application"  when annotated on ElementType.TYPE
     * default ""             when annotated on ElementType.METHOD
     */
    String namespace() default "";

    @AliasFor("value")
    String key() default "";

    @AliasFor("key")
    String value() default "";

    /**
     * effective when annotated on ElementType.METHOD
     */
    String defaultValue() default "";

    long cacheSeconds() default 0;
}
