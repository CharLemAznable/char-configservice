package com.github.charlemaznable.configservice.elf;

import com.github.charlemaznable.configservice.Config;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.lang.annotation.Annotation;

@SuppressWarnings("ClassExplicitlyAnnotation")
@Builder
@Getter
@Accessors(fluent = true)
public final class ConfigImpl implements Config {

    private String keyset;
    private String key;
    private String value;
    private String defaultValue;
    private long cacheSeconds;

    @Override
    public Class<? extends Annotation> annotationType() {
        return Config.class;
    }
}
