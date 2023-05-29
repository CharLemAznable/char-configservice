package com.github.charlemaznable.configservice.etcd;

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
import com.github.charlemaznable.etcdconf.EtcdConfigChangeListener;
import com.github.charlemaznable.etcdconf.EtcdConfigService;
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
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

@NoArgsConstructor(access = PRIVATE)
public final class EtcdFactory {

    private static final LoadingCache<Factory, EtcdLoader>
            loaderCache = simpleCache(from(EtcdLoader::new));

    public static <T> T getEtcd(Class<T> configClass) {
        return etcdLoader(FactoryContext.get()).getEtcd(configClass);
    }

    public static EtcdLoader etcdLoader(Factory factory) {
        return get(loaderCache, factory);
    }

    public static final class EtcdLoader extends ConfigLoader {

        EtcdLoader(Factory factory) {
            super(factory);
        }

        public <T> T getEtcd(Class<T> configClass) {
            return getConfig(configClass);
        }

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Annotation>[] annotationClasses() {
            return new Class[]{Config.class, EtcdConfig.class};
        }

        @Override
        protected void checkClassConfig(Class<?> configClass) {
            if (isAnnotated(configClass, EtcdConfig.class)) return;
            super.checkClassConfig(configClass);
        }

        @Override
        protected <T> ConfigProxy<T> buildConfigProxy(Class<T> configClass, Factory factory) {
            return new EtcdProxy<>(configClass, factory, this);
        }

        @Override
        public Config fetchConfigAnno(AnnotatedElement element) {
            val etcdConfig = getMergedAnnotation(element, EtcdConfig.class);
            if (isNull(etcdConfig)) return super.fetchConfigAnno(element);
            return ConfigImpl.builder()
                    .keyset(etcdConfig.namespace())
                    .key(etcdConfig.key())
                    .value(etcdConfig.value())
                    .defaultValue(etcdConfig.defaultValue())
                    .cacheSeconds(etcdConfig.cacheSeconds()).build();
        }

        @Override
        protected ConfigGetter buildConfigGetter(ConfigSetting configSetting) {
            val namespace = configSetting.keyset();
            val key = configSetting.key();
            val config = EtcdConfigService.getConfig(blankThen(namespace, () -> "application"));
            if (isBlank(key)) return new EtcdConfigGetter.EtcdNamespaceGetter(config);
            return new EtcdConfigGetter.EtcdPropertiesGetter(
                    parseStringToProperties(config.getString(key, ""), key));
        }
    }

    public static final class EtcdProxy<T> extends ConfigProxy<T> {

        EtcdProxy(Class<T> configClass, Factory factory, ConfigLoader configLoader) {
            super(configClass, factory, configLoader);
        }

        @Override
        protected ConfigListenerRegisterProxy<? extends ConfigListenerProxy> buildListenerRegisterProxy(Class<?> configClass, ConfigLoader configLoader) {
            return new EtcdConfigListenerRegister(configClass, configLoader);
        }

        @Override
        protected String loadConfigValue(ConfigGetter configGetter, ConfigSetting configSetting) {
            return ((EtcdConfigGetter) configGetter)
                    .getPropertyValue(configSetting.keyset(), configSetting.key());
        }
    }

    public static final class EtcdConfigListener extends ConfigListenerProxy implements EtcdConfigChangeListener {

        public EtcdConfigListener(String keyset, String key, ConfigListener listener) {
            super(keyset, key, listener);
        }

        @Override
        public void onChange(ChangeEvent changeEvent) {
            this.onChange(changeEvent.getValue());
        }
    }

    public static final class EtcdConfigListenerRegister extends ConfigListenerRegisterProxy<EtcdConfigListener> {

        private static final Map<Triple<String, String, ConfigListener>, EtcdConfigListener> listenerMap = new ConcurrentHashMap<>();

        public EtcdConfigListenerRegister(Class<?> configClass, ConfigLoader configLoader) {
            super(configClass, configLoader);
        }

        @Override
        protected String defaultListeningKeyset() {
            return blankThen(super.defaultListeningKeyset(), () -> "application");
        }

        @Override
        protected Map<Triple<String, String, ConfigListener>, EtcdConfigListener> listenerMap() {
            return listenerMap;
        }

        @Override
        protected EtcdConfigListener addConfigListenerProxy(String keyset, String key, ConfigListener listener) {
            val etcdListener = new EtcdConfigListener(keyset, key, listener);
            new Thread(() -> EtcdConfigService.getConfig(keyset)
                    .addChangeListener(key, etcdListener)).start();
            return etcdListener;
        }

        @Override
        protected void removeConfigListenerProxy(String keyset, String key, EtcdConfigListener listenerProxy) {
            new Thread(() -> EtcdConfigService.getConfig(keyset)
                    .removeChangeListener(listenerProxy)).start();
        }
    }
}
