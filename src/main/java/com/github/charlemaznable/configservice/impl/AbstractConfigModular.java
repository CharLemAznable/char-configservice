package com.github.charlemaznable.configservice.impl;

import com.github.charlemaznable.configservice.ConfigLoader;
import com.github.charlemaznable.core.guice.CommonModular;
import com.github.charlemaznable.core.lang.Factory;
import com.google.inject.Module;
import com.google.inject.Provider;
import lombok.experimental.Delegate;
import lombok.val;

import java.util.function.Function;

import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

public abstract class AbstractConfigModular<M extends AbstractConfigModular<M>> extends CommonModular<M> {

    @Delegate
    private final ConfigLoader loader;

    public AbstractConfigModular(Iterable<? extends Module> baseModules,
                                 Function<Factory, ConfigLoader> loaderBuilder) {
        super(baseModules);
        this.loader = loaderBuilder.apply(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class<?> clazz) {
        for (val annoClass : loader.annotationClasses()) {
            if (isAnnotated(clazz, annoClass)) return true;
        }
        return false;
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getConfig(clazz);
    }
}
