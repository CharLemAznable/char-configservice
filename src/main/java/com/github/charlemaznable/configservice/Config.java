package com.github.charlemaznable.configservice;

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
public @interface Config {

    String keyset() default "";

    @AliasFor("value")
    String key() default "";

    @AliasFor("key")
    String value() default "";

    String defaultValue() default "";

    long cacheSeconds() default 0;

    Class<? extends KeysetProvider> keysetProvider() default KeysetProvider.class;

    Class<? extends KeyProvider> keyProvider() default KeyProvider.class;

    Class<? extends DefaultValueProvider> defaultValueProvider() default DefaultValueProvider.class;

    interface KeysetProvider {

        default String keyset(Class<?> configClass) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#keyset(Class<?>) need be overwritten");
        }

        default String keyset(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#keyset(Class<?>, Method) need be overwritten");
        }
    }

    interface KeyProvider {

        default String key(Class<?> configClass) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#key(Class<?>) need be overwritten");
        }

        default String key(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#key(Class<?>, Method) need be overwritten");
        }
    }

    interface DefaultValueProvider {

        default String defaultValue(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#defaultValue(Class<?>, Method) need be overwritten");
        }
    }
}
