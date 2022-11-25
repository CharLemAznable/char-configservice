package com.github.charlemaznable.configservice.elf;

import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;

@AllArgsConstructor
public class ConfigDummy {

    @Nonnull
    private Class<?> implClass;

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ConfigDummy && hashCode() == obj.hashCode();
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return "ConfigService:" + implClass.getSimpleName() + "@" + Integer.toHexString(hashCode());
    }
}
