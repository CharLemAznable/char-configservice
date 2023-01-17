package com.github.charlemaznable.configservice.impl;

import com.github.charlemaznable.configservice.annotation.ConfigValueParseBy;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.vertx.VertxElf.parsePropertiesToVertxOptions;

@Documented
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ConfigValueParseBy(ParseAsVertxOptions.VertxOptionsParser.class)
public @interface ParseAsVertxOptions {

    final class VertxOptionsParser implements ConfigValueParseBy.ConfigValueParser {

        @Override
        public Object parse(String value, Class<?> clazz) {
            return parsePropertiesToVertxOptions(parseStringToProperties(value));
        }
    }
}
