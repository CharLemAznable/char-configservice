package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.annotation.DefaultEmptyValue;
import com.github.charlemaznable.configservice.elf.ConfigListenerProxy;
import com.github.charlemaznable.configservice.elf.ConfigListenerRegisterProxy;
import com.github.charlemaznable.configservice.elf.ConfigSetting;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.ExpiringEntryLoaderr;
import com.github.charlemaznable.core.lang.Factory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import net.jodah.expiringmap.ExpiringMap;
import net.jodah.expiringmap.ExpiringValue;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.convertType;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.ExpiringMapp.expiringMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public abstract class ConfigProxy<T> implements BuddyEnhancer.Delegate {

    protected final Class<T> configClass;
    protected final Factory factory;
    protected final ConfigLoader configLoader;
    protected final ConfigListenerRegisterProxy<? extends ConfigListenerProxy> configListenerRegisterProxy;
    private final ExpiringMap<Method, ConfigEntry> entryCache
            = expiringMap(ExpiringEntryLoaderr.from(this::loadConfigEntry));

    public ConfigProxy(Class<T> configClass, Factory factory, ConfigLoader configLoader) {
        this.configClass = configClass;
        this.factory = factory;
        this.configLoader = configLoader;
        this.configListenerRegisterProxy = buildListenerRegisterProxy(configClass, configLoader);
    }

    @Override
    public Object invoke(BuddyEnhancer.Invocation invocation) throws Exception {
        val method = invocation.getMethod();
        val args = invocation.getArguments();
        if (method.getDeclaringClass().equals(ConfigGetter.class)) {
            return method.invoke(configLoader.getConfigGetter(configClass), args);
        }
        if (method.getDeclaringClass().equals(ConfigListenerRegister.class)) {
            return method.invoke(this.configListenerRegisterProxy, args);
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

    protected abstract ConfigListenerRegisterProxy<? extends ConfigListenerProxy> buildListenerRegisterProxy(Class<?> configClass, ConfigLoader configLoader);

    protected abstract String loadConfigValue(ConfigGetter configGetter, ConfigSetting configSetting);

    private ExpiringValue<ConfigEntry> loadConfigEntry(Method method) {
        val configAnno = configLoader.fetchConfigAnno(method);
        val keyset = fetchKeyset(configAnno);
        val key = blankThen(fetchKey(configAnno), method::getName);
        val configSetting = ConfigSetting.builder().keyset(keyset).key(key).build();
        val defaultEmptyValue = isAnnotated(method, DefaultEmptyValue.class);
        val defaultValue = fetchDefaultValue(configAnno, defaultEmptyValue);
        val cacheSeconds = fetchCacheSeconds(configAnno);
        return new ExpiringValue<>(new ConfigEntry(key,
                loadConfigValue(configLoader.getConfigGetter(configClass), configSetting),
                defaultValue), cacheSeconds, TimeUnit.SECONDS);
    }

    private String fetchKeyset(Config configAnno) {
        if (isNull(configAnno)) return "";
        return substitute(configAnno.keyset());
    }

    private String fetchKey(Config configAnno) {
        if (isNull(configAnno)) return "";
        return substitute(configAnno.key());
    }

    private String fetchDefaultValue(Config configAnno, boolean defaultEmptyValue) {
        if (isNull(configAnno)) return defaultEmptyValue ? "" : null;
        return substitute(blankThen(configAnno.defaultValue(), () -> defaultEmptyValue ? "" : null));
    }

    private long fetchCacheSeconds(Config configAnno) {
        if (isNull(configAnno)) return 0;
        return Math.max(0, configAnno.cacheSeconds());
    }

    @AllArgsConstructor
    @Getter
    public static final class ConfigEntry {

        private String configKey;
        private String configValue;
        private String defaultValue;
    }
}
