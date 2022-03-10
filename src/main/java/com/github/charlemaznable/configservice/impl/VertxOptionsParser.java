package com.github.charlemaznable.configservice.impl;

import com.github.charlemaznable.configservice.annotation.ConfigValueParse.ConfigValueParser;

import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.vertx.VertxElf.parsePropertiesToVertxOptions;

public final class VertxOptionsParser implements ConfigValueParser {

    @Override
    public Object parse(String value, Class<?> clazz) {
        return parsePropertiesToVertxOptions(parseStringToProperties(value));
    }
}
