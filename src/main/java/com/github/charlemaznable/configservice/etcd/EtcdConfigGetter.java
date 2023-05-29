package com.github.charlemaznable.configservice.etcd;

import com.ctrip.framework.apollo.util.function.Functions;
import com.github.charlemaznable.configservice.impl.DefaultConfigGetter;
import com.github.charlemaznable.etcdconf.EtcdConfig;
import com.github.charlemaznable.etcdconf.EtcdConfigService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;

import java.util.Properties;
import java.util.function.Function;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.checkNull;
import static com.github.charlemaznable.core.lang.Condition.notNullThen;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static org.apache.commons.text.StringSubstitutor.replace;

public abstract class EtcdConfigGetter extends DefaultConfigGetter {

    public abstract String getPropertyValue(String namespace, String propertyName);

    @Override
    public int getInt(String key, int defaultValue) {
        return getValue(key, defaultValue, Functions.TO_INT_FUNCTION);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return getValue(key, defaultValue, Functions.TO_LONG_FUNCTION);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        return getValue(key, defaultValue, Functions.TO_SHORT_FUNCTION);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return getValue(key, defaultValue, Functions.TO_FLOAT_FUNCTION);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return getValue(key, defaultValue, Functions.TO_DOUBLE_FUNCTION);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        return getValue(key, defaultValue, Functions.TO_BYTE_FUNCTION);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return getValue(key, defaultValue, BooleanUtils::toBoolean);
    }

    @Override
    public long getDuration(String key, long defaultValue) {
        return getValue(key, defaultValue, Functions.TO_DURATION_FUNCTION);
    }

    private <T> T getValue(String key, T defaultValue, Function<String, T> parser) {
        return checkBlank(getString(key), () -> defaultValue, value -> {
            try {
                return parser.apply(value);
            } catch (Exception e) {
                return defaultValue;
            }
        });
    }

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