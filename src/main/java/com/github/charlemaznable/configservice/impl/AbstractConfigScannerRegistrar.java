package com.github.charlemaznable.configservice.impl;

import com.github.charlemaznable.configservice.ConfigLoader;
import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import lombok.Setter;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.core.type.ClassMetadata;

import java.lang.annotation.Annotation;

public abstract class AbstractConfigScannerRegistrar extends SpringScannerRegistrar {

    private final ConfigLoader loader;

    public AbstractConfigScannerRegistrar(Class<? extends Annotation> scanAnnotationClass,
                                          ConfigLoader loader) {
        super(scanAnnotationClass, ConfigFactoryBean.class, loader.annotationClasses());
        this.loader = loader;
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    @Override
    protected void postProcessBeanDefinition(BeanDefinition beanDefinition) {
        super.postProcessBeanDefinition(beanDefinition);
        beanDefinition.getPropertyValues().add("loader", loader);
    }

    public static class ConfigFactoryBean extends SpringFactoryBean {

        @Setter
        private ConfigLoader loader;

        @Override
        public Object buildObject(Class<?> xyzInterface) {
            return loader.getConfig(xyzInterface);
        }
    }
}
