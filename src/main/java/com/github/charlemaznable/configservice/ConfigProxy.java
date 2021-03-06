package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.elf.ConfigDummy;
import com.github.charlemaznable.core.lang.ExpiringEntryLoaderr;
import com.github.charlemaznable.core.lang.Factory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import net.jodah.expiringmap.ExpiringMap;
import net.jodah.expiringmap.ExpiringValue;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.convertType;
import static com.github.charlemaznable.core.lang.ExpiringMapp.expiringMap;
import static java.util.Objects.nonNull;

public abstract class ConfigProxy<T> implements MethodInterceptor {

    protected final Class<T> configClass;
    protected final Factory factory;
    protected final ConfigLoader configLoader;
    private ExpiringMap<Method, ConfigEntry> entryCache
            = expiringMap(ExpiringEntryLoaderr.from(this::loadConfigEntry));

    public ConfigProxy(Class<T> configClass, Factory factory, ConfigLoader configLoader) {
        this.configClass = configClass;
        this.factory = factory;
        this.configLoader = configLoader;
    }

    @Override
    public Object intercept(Object o, Method method, Object[] args,
                            MethodProxy methodProxy) throws Throwable {
        if (method.getDeclaringClass().equals(ConfigDummy.class)) {
            return methodProxy.invokeSuper(o, args);
        }

        if (method.getDeclaringClass().equals(ConfigGetter.class)) {
            return method.invoke(configLoader.getConfigGetter(configClass), args);
        }

        val configEntry = entryCache.get(method);
        val configKey = configEntry.getConfigKey();
        val configValue = configEntry.getConfigValue();
        val defaultValue = configEntry.getDefaultValue();
        val defaultArgument = args.length > 0 ? args[0] : null;

        if (nonNull(configValue))
            return convertType(configValue, method, factory, configKey);
        if (nonNull(defaultArgument))
            return defaultArgument;
        if (nonNull(defaultValue))
            return convertType(defaultValue, method, factory, configKey);
        return null;
    }

    protected abstract ExpiringValue<ConfigEntry> loadConfigEntry(Method method);

    @AllArgsConstructor
    @Getter
    public static class ConfigEntry {

        private String configKey;
        private String configValue;
        private String defaultValue;
    }
}
