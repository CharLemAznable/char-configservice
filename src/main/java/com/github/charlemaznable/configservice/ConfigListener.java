package com.github.charlemaznable.configservice;

public interface ConfigListener {

    void onChange(String keyset, String key, String value);
}
