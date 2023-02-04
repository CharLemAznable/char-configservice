package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.elf.ConfigSetting;
import com.github.charlemaznable.core.lang.BuddyEnhancer;
import com.github.charlemaznable.core.lang.ExpiringEntryLoaderr;
import com.github.charlemaznable.core.lang.Factory;
import com.google.common.cache.LoadingCache;
import lombok.AllArgsConstructor;
import lombok.val;
import net.jodah.expiringmap.ExpiringMap;
import net.jodah.expiringmap.ExpiringValue;

import javax.annotation.Nonnull;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.ensureClassIsAnInterface;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.ExpiringMapp.expiringMap;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.google.common.cache.CacheLoader.from;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@SuppressWarnings("unchecked")
public abstract class ConfigLoader {

    protected final Factory factory;
    private final LoadingCache<Class<?>, Object> configCache
            = simpleCache(from(this::loadConfig));
    private final ExpiringMap<Class<?>, ConfigGetter> configGetterCache
            = expiringMap(ExpiringEntryLoaderr.from(this::loadConfigGetter));

    public ConfigLoader(Factory factory) {
        this.factory = checkNotNull(factory);
    }

    public <T> T getConfig(Class<T> configClass) {
        return (T) get(configCache, configClass);
    }

    public <T> ConfigGetter getConfigGetter(Class<T> configClass) {
        return configGetterCache.get(configClass);
    }

    public ConfigGetter buildConfigGetter(String keyset, String key) {
        val configSetting = ConfigSetting.builder().keyset(keyset).key(key).build();
        return buildConfigGetter(configSetting);
    }

    public abstract Class<? extends Annotation>[] annotationClasses();

    protected void checkClassConfig(Class<?> configClass) {
        if (isAnnotated(configClass, Config.class)) return;
        throw new ConfigServiceException(configClass + " not annotated with Config Annotations");
    }

    protected abstract <T> ConfigProxy<T> buildConfigProxy(Class<T> configClass, Factory factory);

    public Config fetchConfigAnno(AnnotatedElement element) {
        return getMergedAnnotation(element, Config.class);
    }

    public <T> ConfigSetting fetchConfigSetting(Class<T> configClass) {
        val configAnno = checkNotNull(fetchConfigAnno(configClass));
        return ConfigSetting.builder()
                .keyset(fetchKeyset(configClass, configAnno))
                .key(fetchKey(configClass, configAnno)).build();
    }

    protected abstract ConfigGetter buildConfigGetter(ConfigSetting configSetting);

    @Nonnull
    private <T> Object loadConfig(@Nonnull Class<T> configClass) {
        ensureClassIsAnInterface(configClass);
        checkClassConfig(configClass);

        val configProxy = buildConfigProxy(configClass, factory);
        return BuddyEnhancer.create(ConfigDummy.class,
                new Object[]{configClass},
                new Class[]{configClass, ConfigGetter.class, ConfigListenerRegister.class},
                invocation -> {
                    if (invocation.getMethod().isDefault() ||
                            invocation.getMethod().getDeclaringClass()
                                    .equals(ConfigDummy.class)) return 1;
                    return 0;
                },
                new BuddyEnhancer.Delegate[]{configProxy, BuddyEnhancer.CALL_SUPER});
    }

    private <T> ExpiringValue<ConfigGetter> loadConfigGetter(Class<T> configClass) {
        val configAnno = checkNotNull(fetchConfigAnno(configClass));
        val configGetter = buildConfigGetter(
                fetchKeyset(configClass, configAnno),
                fetchKey(configClass, configAnno));
        val cacheSeconds = Math.max(0, configAnno.cacheSeconds());
        return new ExpiringValue<>(configGetter, cacheSeconds, TimeUnit.SECONDS);
    }

    private <T> String fetchKeyset(Class<T> configClass, Config configAnno) {
        return substitute(configAnno.keyset());
    }

    private <T> String fetchKey(Class<T> configClass, Config configAnno) {
        return substitute(configAnno.key());
    }

    @AllArgsConstructor
    private static class ConfigDummy {

        @Nonnull
        private Class<?> implClass;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof ConfigDummy && hashCode() == obj.hashCode();
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public String toString() {
            return "ConfigService:" + implClass.getSimpleName() + "@" + Integer.toHexString(hashCode());
        }
    }
}
