package com.github.charlemaznable.configservice.apollo;

import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.ConfigLoader;
import com.github.charlemaznable.configservice.ConfigProxy;
import com.github.charlemaznable.configservice.annotation.DefaultEmptyValue;
import com.github.charlemaznable.configservice.apollo.ApolloConfig.DefaultValueProvider;
import com.github.charlemaznable.configservice.apollo.ApolloConfig.NamespaceProvider;
import com.github.charlemaznable.configservice.apollo.ApolloConfig.PropertyNameProvider;
import com.github.charlemaznable.configservice.apollo.ApolloConfigGetter.ApolloNamespaceGetter;
import com.github.charlemaznable.configservice.apollo.ApolloConfigGetter.ApolloPropertiesGetter;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;
import net.jodah.expiringmap.ExpiringValue;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.parseStringToProperties;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.spring.AnnotationElf.findAnnotation;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;

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

        @Override
        protected void checkClassConfig(Class<?> configClass) {
            checkNotNull(findAnnotation(configClass, ApolloConfig.class),
                    new ConfigServiceException(configClass + " has no ApolloConfig"));
        }

        @Override
        protected <T> ConfigProxy<T> buildConfigProxy(Class<T> configClass, Factory factory) {
            return new ApolloProxy<>(configClass, factory, this);
        }

        @Override
        protected <T> ExpiringValue<ConfigGetter> loadConfigGetter(Class<T> configClass) {
            val apolloConfig = checkApolloConfig(configClass);
            val namespace = checkApolloNamespace(configClass, apolloConfig);
            val propertyName = checkApolloPropertyName(configClass, apolloConfig);
            val cacheSeconds = Math.max(0, apolloConfig.cacheSeconds());

            val config = ConfigService.getConfig(blankThen(namespace, () -> "application"));
            if (isBlank(propertyName)) return new ExpiringValue<>(
                    new ApolloNamespaceGetter(config), cacheSeconds, TimeUnit.SECONDS);
            return new ExpiringValue<>(new ApolloPropertiesGetter(parseStringToProperties(
                    config.getProperty(propertyName, ""), propertyName)), cacheSeconds, TimeUnit.SECONDS);
        }

        private <T> ApolloConfig checkApolloConfig(Class<T> configClass) {
            return checkNotNull(getMergedAnnotation(configClass, ApolloConfig.class));
        }

        private <T> String checkApolloNamespace(Class<T> configClass, ApolloConfig apolloConfig) {
            val providerClass = apolloConfig.namespaceProvider();
            return substitute(NamespaceProvider.class == providerClass ? apolloConfig.namespace()
                    : FactoryContext.apply(factory, providerClass, p -> p.namespace(configClass)));
        }

        private <T> String checkApolloPropertyName(Class<T> configClass, ApolloConfig apolloConfig) {
            val providerClass = apolloConfig.propertyNameProvider();
            return substitute(PropertyNameProvider.class == providerClass ? apolloConfig.propertyName()
                    : FactoryContext.apply(factory, providerClass, p -> p.propertyName(configClass)));
        }
    }

    public static final class ApolloProxy<T> extends ConfigProxy<T> {

        ApolloProxy(Class<T> configClass, Factory factory, ConfigLoader configLoader) {
            super(configClass, factory, configLoader);
        }

        @Override
        protected ExpiringValue<ConfigEntry> loadConfigEntry(Method method) {
            val apolloConfig = getMergedAnnotation(method, ApolloConfig.class);
            val namespace = checkApolloNamespace(method, apolloConfig);
            val propertyName = blankThen(checkApolloPropertyName(method, apolloConfig), method::getName);
            val defaultEmptyValue = nonNull(findAnnotation(method, DefaultEmptyValue.class));
            val defaultValue = checkApolloDefaultValue(method, apolloConfig, defaultEmptyValue);
            val cacheSeconds = checkApolloCacheSeconds(apolloConfig);
            val configGetter = (ApolloConfigGetter) configLoader.getConfigGetter(configClass);
            val propertyValue = configGetter.getPropertyValue(namespace, propertyName);
            return new ExpiringValue<>(new ConfigEntry(propertyName,
                    propertyValue, defaultValue), cacheSeconds, TimeUnit.SECONDS);
        }

        private String checkApolloNamespace(Method method, ApolloConfig apolloConfig) {
            if (isNull(apolloConfig)) return "";
            val providerClass = apolloConfig.namespaceProvider();
            return substitute(NamespaceProvider.class == providerClass ? apolloConfig.namespace()
                    : FactoryContext.apply(factory, providerClass, p -> p.namespace(configClass, method)));
        }

        private String checkApolloPropertyName(Method method, ApolloConfig apolloConfig) {
            if (isNull(apolloConfig)) return "";
            val providerClass = apolloConfig.propertyNameProvider();
            return substitute(PropertyNameProvider.class == providerClass ? apolloConfig.propertyName()
                    : FactoryContext.apply(factory, providerClass, p -> p.propertyName(configClass, method)));
        }

        @SuppressWarnings("Duplicates")
        private String checkApolloDefaultValue(Method method, ApolloConfig apolloConfig, boolean defaultEmptyValue) {
            if (isNull(apolloConfig)) return defaultEmptyValue ? "" : null;
            val providerClass = apolloConfig.defaultValueProvider();
            String defaultValue = apolloConfig.defaultValue();
            if (DefaultValueProvider.class != providerClass) {
                defaultValue = FactoryContext.apply(factory, providerClass,
                        p -> p.defaultValue(configClass, method));
            }
            return substitute(blankThen(defaultValue, () -> defaultEmptyValue ? "" : null));
        }

        private long checkApolloCacheSeconds(ApolloConfig apolloConfig) {
            if (isNull(apolloConfig)) return 0;
            return Math.max(0, apolloConfig.cacheSeconds());
        }
    }
}
