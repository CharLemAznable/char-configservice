package com.github.charlemaznable.configservice.elf;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ConfigDummy {

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
        return "ConfigService@" + Integer.toHexString(hashCode());
    }
}
