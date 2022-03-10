package com.github.charlemaznable.configservice.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigValueParse {

    Class<? extends ConfigValueParser> value();

    interface ConfigValueParser {

        Object parse(String value, Class<?> clazz);
    }
}
