package com.github.charlemaznable.configservice;

public interface ConfigListenerRegister {

    void addConfigListener(ConfigListener listener);

    void removeConfigListener(ConfigListener listener);

    void addConfigListener(String key, ConfigListener listener);

    void removeConfigListener(String key, ConfigListener listener);
}
