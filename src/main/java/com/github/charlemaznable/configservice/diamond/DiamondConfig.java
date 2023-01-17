package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

@Documented
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DiamondConfig {

    /**
     * default "DEFAULT_GROUP"  when annotated on ElementType.TYPE
     * default ""               when annotated on ElementType.METHOD
     */
    String group() default "";

    @AliasFor("value")
    String dataId() default "";

    @AliasFor("dataId")
    String value() default "";

    /**
     * effective when annotated on ElementType.METHOD
     */
    String defaultValue() default "";

    long cacheSeconds() default 0;

    Class<? extends GroupProvider> groupProvider() default GroupProvider.class;

    Class<? extends DataIdProvider> dataIdProvider() default DataIdProvider.class;

    Class<? extends DefaultValueProvider> defaultValueProvider() default DefaultValueProvider.class;

    interface GroupProvider extends Config.KeysetProvider {

        default String group(Class<?> configClass) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#group(Class<?>) need be overwritten");
        }

        default String group(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#group(Class<?>, Method) need be overwritten");
        }

        @Override
        default String keyset(Class<?> configClass) {
            return group(configClass);
        }

        @Override
        default String keyset(Class<?> configClass, Method method) {
            return group(configClass, method);
        }
    }

    interface DataIdProvider extends Config.KeyProvider {

        default String dataId(Class<?> configClass) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#dataId(Class<?>) need be overwritten");
        }

        default String dataId(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#dataId(Class<?>, Method) need be overwritten");
        }

        @Override
        default String key(Class<?> configClass) {
            return dataId(configClass);
        }

        @Override
        default String key(Class<?> configClass, Method method) {
            return dataId(configClass, method);
        }
    }

    interface DefaultValueProvider extends Config.DefaultValueProvider {
    }
}
