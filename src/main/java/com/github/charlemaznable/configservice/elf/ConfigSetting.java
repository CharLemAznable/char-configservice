package com.github.charlemaznable.configservice.elf;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Builder
@Getter
@Accessors(fluent = true)
public final class ConfigSetting {

    private String keyset;
    private String key;
}
