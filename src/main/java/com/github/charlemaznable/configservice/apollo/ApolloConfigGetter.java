package com.github.charlemaznable.configservice.apollo;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.util.function.Functions;
import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.DefaultConfigGetter;
import lombok.AllArgsConstructor;

import java.util.Properties;
import java.util.function.Function;

import static com.github.charlemaznable.core.lang.Condition.checkBlank;
import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static com.github.charlemaznable.core.lang.Str.isBlank;

public interface ApolloConfigGetter extends ConfigGetter {

    String getPropertyValue(String namespace, String propertyName);

    @AllArgsConstructor
    final class ApolloNamespaceGetter extends DefaultConfigGetter implements ApolloConfigGetter {

        private Config config;

        @Override
        public String getPropertyValue(String namespace, String propertyName) {
            if (isBlank(namespace)) return getString(propertyName);
            return ConfigService.getConfig(namespace).getProperty(propertyName, null);
        }

        @Override
        public String getString(String key, String defaultValue) {
            return config.getProperty(key, defaultValue);
        }

        @Override
        public int getInt(String key, int defaultValue) {
            return config.getIntProperty(key, defaultValue);
        }

        @Override
        public long getLong(String key, long defaultValue) {
            return config.getLongProperty(key, defaultValue);
        }

        @Override
        public short getShort(String key, short defaultValue) {
            return config.getShortProperty(key, defaultValue);
        }

        @Override
        public float getFloat(String key, float defaultValue) {
            return config.getFloatProperty(key, defaultValue);
        }

        @Override
        public double getDouble(String key, double defaultValue) {
            return config.getDoubleProperty(key, defaultValue);
        }

        @Override
        public byte getByte(String key, byte defaultValue) {
            return config.getByteProperty(key, defaultValue);
        }

        @Override
        public boolean getBoolean(String key, boolean defaultValue) {
            return config.getBooleanProperty(key, defaultValue);
        }

        @Override
        public long getDuration(String key, long defaultValue) {
            return config.getDurationProperty(key, defaultValue);
        }
    }

    @AllArgsConstructor
    final class ApolloPropertiesGetter extends DefaultConfigGetter implements ApolloConfigGetter {

        private Properties properties;

        @Override
        public String getPropertyValue(String namespace, String propertyName) {
            return getString(isBlank(namespace) ? propertyName : (namespace + "." + propertyName));
        }

        @Override
        public String getString(String key, String defaultValue) {
            return nullThen(properties.getProperty(key), () -> defaultValue);
        }

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
            return getValue(key, defaultValue, Functions.TO_BOOLEAN_FUNCTION);
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
    }
}