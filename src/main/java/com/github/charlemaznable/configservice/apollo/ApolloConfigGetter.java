package com.github.charlemaznable.configservice.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.configservice.impl.DefaultPropertyConfigGetter;
import lombok.AllArgsConstructor;

import java.util.Properties;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static org.apache.commons.text.StringSubstitutor.replace;

public abstract class ApolloConfigGetter extends DefaultPropertyConfigGetter {

    @AllArgsConstructor
    static final class ApolloNamespaceGetter extends ApolloConfigGetter {

        private Config config;

        @Override
        public String getPropertyValue(String namespace, String propertyName) {
            if (isBlank(namespace)) return getString(propertyName);
            return new ApolloNamespaceGetter(ConfigService
                    .getConfig(namespace)).getString(propertyName);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return notNullThen(config.getProperty(key, defaultValue),
                    value -> replace(value, parseStringToProperties(value, key)));
        }
    }

    @AllArgsConstructor
    static final class ApolloPropertiesGetter extends ApolloConfigGetter {

        private Properties properties;

        @Override
        public String getPropertyValue(String namespace, String propertyName) {
            return getString(isBlank(namespace) ? propertyName : (namespace + "." + propertyName));
        }

        @Override
        public String getString(String key, String defaultValue) {
            return checkNull(properties.getProperty(key),
                    () -> defaultValue, value -> replace(value, properties));
        }
    }
}
