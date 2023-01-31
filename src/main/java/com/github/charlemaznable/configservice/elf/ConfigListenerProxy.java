package com.github.charlemaznable.configservice.elf;

import com.github.charlemaznable.configservice.ConfigListener;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class ConfigListenerProxy {

    protected String keyset;
    protected String key;
    protected ConfigListener listener;

    public void onChange(String value) {
        this.listener.onChange(this.keyset, this.key, value);
    }
}
