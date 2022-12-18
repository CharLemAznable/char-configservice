package com.github.charlemaznable.configservice.elf;

import java.io.Serial;

public final class ConfigServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1468891602796981081L;

    public ConfigServiceException(String msg) {
        super(msg);
    }
}
