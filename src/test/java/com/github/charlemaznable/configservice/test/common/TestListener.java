package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.ConfigListener;
import lombok.Getter;

@Getter
public class TestListener implements ConfigListener {

    private boolean changed;
    private String keyset;
    private String key;
    private String value;

    @Override
    public void onChange(String keyset, String key, String value) {
        this.keyset = keyset;
        this.key = key;
        this.value = value;
        this.changed = true;
    }

    public void reset() {
        this.changed = false;
    }
}
