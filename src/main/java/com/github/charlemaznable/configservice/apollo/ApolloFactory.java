package com.github.charlemaznable.configservice.apollo;

import com.ctrip.framework.apollo.ConfigChangeListener;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.configservice.ConfigLoader;
import com.github.charlemaznable.configservice.ConfigProxy;
import com.github.charlemaznable.configservice.elf.ConfigImpl;
import com.github.charlemaznable.configservice.elf.ConfigListenerProxy;
import com.github.charlemaznable.configservice.elf.ConfigListenerRegisterProxy;
import com.github.charlemaznable.configservice.elf.ConfigSetting;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.google.common.cache.CacheLoader.from;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@NoArgsConstructor(access = PRIVATE)
public final class ApolloFactory {

    private static final LoadingCache<Factory, ApolloLoader>
            loaderCache = simpleCache(from(ApolloLoader::new));

    public static <T> T getApollo(Class<T> configClass) {
        return apolloLoader(FactoryContext.get()).getApollo(configClass);
    }

    public static ApolloLoader apolloLoader(Factory factory) {
        return get(loaderCache, factory);
    }

    public static final class ApolloLoader extends ConfigLoader {

        ApolloLoader(Factory factory) {
            super(factory);
        }

        public <T> T getApollo(Class<T> configClass) {
            return getConfig(configClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Annotation>[] annotationClasses() {
            return new Class[]{Config.class, ApolloConfig.class};
        }

        @Override
        protected void checkClassConfig(Class<?> configClass) {
            if (isAnnotated(configClass, ApolloConfig.class)) return;
            super.checkClassConfig(configClass);
        }

        @Override
        protected <T> ConfigProxy<T> buildConfigProxy(Class<T> configClass, Factory factory) {
            return new ApolloProxy<>(configClass, factory, this);
        }

        @Override
        public Config fetchConfigAnno(AnnotatedElement element) {
            val apolloConfig = getMergedAnnotation(element, ApolloConfig.class);
            if (isNull(apolloConfig)) return super.fetchConfigAnno(element);
            return ConfigImpl.builder()
                    .keyset(apolloConfig.namespace())
                    .key(apolloConfig.propertyName())
                    .value(apolloConfig.value())
                    .defaultValue(apolloConfig.defaultValue())
                    .cacheSeconds(apolloConfig.cacheSeconds())
                    .keysetProvider(apolloConfig.namespaceProvider())
                    .keyProvider(apolloConfig.propertyNameProvider())
                    .defaultValueProvider(apolloConfig.defaultValueProvider()).build();
        }

        @Override
        protected ConfigGetter buildConfigGetter(ConfigSetting configSetting) {
            val namespace = configSetting.keyset();
            val propertyName = configSetting.key();
            val config = ConfigService.getConfig(blankThen(namespace, () -> "application"));
            if (isBlank(propertyName)) return new ApolloConfigGetter.ApolloNamespaceGetter(config);
            return new ApolloConfigGetter.ApolloPropertiesGetter(parseStringToProperties(
                    config.getProperty(propertyName, ""), propertyName));
        }

        @Override
        protected boolean ignoredKeysetProvider(Class<? extends Config.KeysetProvider> providerClass) {
            return super.ignoredKeysetProvider(providerClass) ||
                    ApolloConfig.NamespaceProvider.class == providerClass;
        }

        @Override
        protected boolean ignoredKeyProvider(Class<? extends Config.KeyProvider> providerClass) {
            return super.ignoredKeyProvider(providerClass) ||
                    ApolloConfig.PropertyNameProvider.class == providerClass;
        }
    }

    public static final class ApolloProxy<T> extends ConfigProxy<T> {

        ApolloProxy(Class<T> configClass, Factory factory, ConfigLoader configLoader) {
            super(configClass, factory, configLoader);
        }

        @Override
        protected ConfigListenerRegisterProxy<? extends ConfigListenerProxy> buildListenerRegisterProxy(Class<?> configClass, ConfigLoader configLoader) {
            return new ApolloConfigListenerRegister(configClass, configLoader);
        }

        @Override
        protected String loadConfigValue(ConfigGetter configGetter, ConfigSetting configSetting) {
            return ((ApolloConfigGetter) configGetter)
                    .getPropertyValue(configSetting.keyset(), configSetting.key());
        }

        @Override
        protected boolean ignoredKeysetProvider(Class<? extends Config.KeysetProvider> providerClass) {
            return super.ignoredKeysetProvider(providerClass) ||
                    ApolloConfig.NamespaceProvider.class == providerClass;
        }

        @Override
        protected boolean ignoredKeyProvider(Class<? extends Config.KeyProvider> providerClass) {
            return super.ignoredKeyProvider(providerClass) ||
                    ApolloConfig.PropertyNameProvider.class == providerClass;
        }

        @Override
        protected boolean ignoredDefaultValueProvider(Class<? extends Config.DefaultValueProvider> providerClass) {
            return super.ignoredDefaultValueProvider(providerClass) ||
                    ApolloConfig.DefaultValueProvider.class == providerClass;
        }
    }

    public static final class ApolloConfigListener extends ConfigListenerProxy implements ConfigChangeListener {

        public ApolloConfigListener(String keyset, String key, ConfigListener listener) {
            super(keyset, key, listener);
        }

        @Override
        public void onChange(ConfigChangeEvent changeEvent) {
            if (!changeEvent.isChanged(this.key)) return;
            this.onChange(changeEvent.getChange(this.key).getNewValue());
        }
    }

    public static final class ApolloConfigListenerRegister extends ConfigListenerRegisterProxy<ApolloConfigListener> {

        private static final Map<Triple<String, String, ConfigListener>, ApolloConfigListener> listenerMap = new ConcurrentHashMap<>();

        public ApolloConfigListenerRegister(Class<?> configClass, ConfigLoader configLoader) {
            super(configClass, configLoader);
        }

        @Override
        protected String defaultListeningKeyset() {
            return blankThen(super.defaultListeningKeyset(), () -> "application");
        }

        @Override
        protected Map<Triple<String, String, ConfigListener>, ApolloConfigListener> listenerMap() {
            return listenerMap;
        }

        @Override
        protected ApolloConfigListener addConfigListenerProxy(String keyset, String key, ConfigListener listener) {
            val apolloListener = new ApolloConfigListener(keyset, key, listener);
            new Thread(() -> ConfigService.getConfig(keyset)
                    .addChangeListener(apolloListener, newHashSet(key))).start();
            return apolloListener;
        }

        @Override
        protected void removeConfigListenerProxy(String keyset, String key, ApolloConfigListener listenerProxy) {
            new Thread(() -> ConfigService.getConfig(keyset)
                    .removeChangeListener(listenerProxy)).start();
        }
    }
}
