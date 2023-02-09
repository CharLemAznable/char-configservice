package com.github.charlemaznable.configservice.elf;

import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.configservice.ConfigListenerRegister;
import com.github.charlemaznable.configservice.ConfigLoader;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;

import java.util.Map;

import static com.github.charlemaznable.core.lang.Condition.checkNotBlank;
import static com.github.charlemaznable.core.lang.Str.isBlank;

@Slf4j(topic = "ConfigService")
public abstract class ConfigListenerRegisterProxy<P extends ConfigListenerProxy> implements ConfigListenerRegister {

    protected final Class<?> configClass;
    protected final ConfigLoader configLoader;

    public ConfigListenerRegisterProxy(Class<?> configClass, ConfigLoader configLoader) {
        this.configClass = configClass;
        this.configLoader = configLoader;
    }

    @Override
    public void addConfigListener(ConfigListener listener) {
        addConfigListener(defaultListeningKey(), listener);
    }

    @Override
    public void removeConfigListener(ConfigListener listener) {
        removeConfigListener(defaultListeningKey(), listener);
    }

    @Override
    public void addConfigListener(String key, ConfigListener listener) {
        val keyset = checkNotBlank(defaultListeningKeyset());
        if (isBlank(key)) {
            log.warn("Ignored blank listening key of config class: {}", configClass);
            return;
        }
        listenerMap().computeIfAbsent(Triple.of(keyset, key, listener),
                t -> addConfigListenerProxy(keyset, key, listener));
    }

    @Override
    public void removeConfigListener(String key, ConfigListener listener) {
        val keyset = checkNotBlank(defaultListeningKeyset());
        if (isBlank(key)) {
            log.warn("Ignored blank listening key of config class: {}", configClass);
            return;
        }
        listenerMap().computeIfPresent(Triple.of(keyset, key, listener), (t, listenerProxy) -> {
            removeConfigListenerProxy(keyset, key, listenerProxy);
            return null;
        });
    }

    protected String defaultListeningKeyset() {
        return configLoader.fetchConfigSetting(configClass).keyset();
    }

    protected String defaultListeningKey() {
        return configLoader.fetchConfigSetting(configClass).key();
    }

    protected abstract Map<Triple<String, String, ConfigListener>, P> listenerMap();

    protected abstract P addConfigListenerProxy(String keyset, String key, ConfigListener listener);

    protected abstract void removeConfigListenerProxy(String keyset, String key, P listenerProxy);
}
