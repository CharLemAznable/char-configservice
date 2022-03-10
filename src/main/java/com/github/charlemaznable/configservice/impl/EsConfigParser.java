package com.github.charlemaznable.configservice.impl;

import com.github.charlemaznable.configservice.annotation.ConfigValueParse.ConfigValueParser;

import static com.github.charlemaznable.core.es.EsClientElf.parsePropertiesToEsConfig;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;

public final class EsConfigParser implements ConfigValueParser {

    @Override
    public Object parse(String value, Class<?> clazz) {
        return parsePropertiesToEsConfig(parseStringToProperties(value));
    }
}
