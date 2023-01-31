package com.github.charlemaznable.configservice;

public interface ConfigListenerRegister {

    void addConfigListener(String keyset, String key, ConfigListener listener);

    void removeConfigListener(String keyset, String key, ConfigListener listener);
}
