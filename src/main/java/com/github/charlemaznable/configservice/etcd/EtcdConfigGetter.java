package com.github.charlemaznable.configservice.etcd;

import com.github.charlemaznable.configservice.impl.DefaultPropertyConfigGetter;
import com.github.charlemaznable.etcdconf.EtcdConfig;
import com.github.charlemaznable.etcdconf.EtcdConfigService;
import lombok.AllArgsConstructor;

import java.util.Properties;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static org.apache.commons.text.StringSubstitutor.replace;

public abstract class EtcdConfigGetter extends DefaultPropertyConfigGetter {

    @AllArgsConstructor
    static final class EtcdNamespaceGetter extends EtcdConfigGetter {

        private EtcdConfig config;

        @Override
        public String getPropertyValue(String namespace, String key) {
            if (isBlank(namespace)) return getString(key);
            return new EtcdNamespaceGetter(EtcdConfigService
                    .getConfig(namespace)).getString(key);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return notNullThen(config.getString(key, defaultValue),
                    value -> replace(value, parseStringToProperties(value, key)));
        }
    }

    @AllArgsConstructor
    static final class EtcdPropertiesGetter extends EtcdConfigGetter {

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
