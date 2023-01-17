package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.elf.ConfigSetting;
import com.github.charlemaznable.core.context.FactoryContext;
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

    public abstract Class<? extends Annotation>[] annotationClasses();

    protected void checkClassConfig(Class<?> configClass) {
        if (isAnnotated(configClass, Config.class)) return;
        throw new ConfigServiceException(configClass + " not annotated with Config Annotations");
    }

    protected abstract <T> ConfigProxy<T> buildConfigProxy(Class<T> configClass, Factory factory);

    public Config fetchConfigAnno(AnnotatedElement element) {
        return getMergedAnnotation(element, Config.class);
    }

    protected abstract ConfigGetter buildConfigGetter(ConfigSetting configSetting);

    @Nonnull
    private <T> Object loadConfig(@Nonnull Class<T> configClass) {
        ensureClassIsAnInterface(configClass);
        checkClassConfig(configClass);

        val configProxy = buildConfigProxy(configClass, factory);
        return BuddyEnhancer.create(ConfigDummy.class,
                new Object[]{configClass},
                new Class[]{configClass, ConfigGetter.class},
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
        val configSetting = ConfigSetting.builder()
                .keyset(fetchKeyset(configClass, configAnno))
                .key(fetchKey(configClass, configAnno)).build();
        val cacheSeconds = Math.max(0, configAnno.cacheSeconds());
        return new ExpiringValue<>(buildConfigGetter(configSetting), cacheSeconds, TimeUnit.SECONDS);
    }

    private <T> String fetchKeyset(Class<T> configClass, Config configAnno) {
        val providerClass = configAnno.keysetProvider();
        return substitute(ignoredKeysetProvider(providerClass) ? configAnno.keyset()
                : FactoryContext.apply(factory, providerClass, p -> p.keyset(configClass)));
    }

    protected boolean ignoredKeysetProvider(Class<? extends Config.KeysetProvider> providerClass) {
        return Config.KeysetProvider.class == providerClass;
    }

    private <T> String fetchKey(Class<T> configClass, Config configAnno) {
        val providerClass = configAnno.keyProvider();
        return substitute(ignoredKeyProvider(providerClass) ? configAnno.key()
                : FactoryContext.apply(factory, providerClass, p -> p.key(configClass)));
    }

    protected boolean ignoredKeyProvider(Class<? extends Config.KeyProvider> providerClass) {
        return Config.KeyProvider.class == providerClass;
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
