package com.github.charlemaznable.configservice.apollo;

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
public @interface ApolloConfig {

    /**
     * default "application"  when annotated on ElementType.TYPE
     * default ""             when annotated on ElementType.METHOD
     */
    String namespace() default "";

    @AliasFor("value")
    String propertyName() default "";

    @AliasFor("propertyName")
    String value() default "";

    /**
     * effective when annotated on ElementType.METHOD
     */
    String defaultValue() default "";

    long cacheSeconds() default 0;

    Class<? extends NamespaceProvider> namespaceProvider() default NamespaceProvider.class;

    Class<? extends PropertyNameProvider> propertyNameProvider() default PropertyNameProvider.class;

    Class<? extends DefaultValueProvider> defaultValueProvider() default DefaultValueProvider.class;

    interface NamespaceProvider {

        default String namespace(Class<?> configClass) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#namespace(Class<?>) need be overwritten");
        }

        default String namespace(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#namespace(Class<?>, Method) need be overwritten");
        }
    }

    interface PropertyNameProvider {

        default String propertyName(Class<?> configClass) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#propertyName(Class<?>) need be overwritten");
        }

        default String propertyName(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#propertyName(Class<?>, Method) need be overwritten");
        }
    }

    interface DefaultValueProvider {

        default String defaultValue(Class<?> configClass, Method method) {
            throw new ConfigServiceException(this.getClass().getName()
                    + "#defaultValue(Class<?>, Method) need be overwritten");
        }
    }
}
