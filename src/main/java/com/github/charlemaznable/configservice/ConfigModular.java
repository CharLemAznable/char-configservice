package com.github.charlemaznable.configservice;

import com.github.charlemaznable.core.guice.CommonModular;
import com.google.inject.Module;
import com.google.inject.Provider;
import lombok.experimental.Delegate;

import static com.github.charlemaznable.configservice.ConfigFactory.configAnnotation;
import static com.github.charlemaznable.configservice.ConfigFactory.configLoader;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.spring.AnnotationElf.findAnnotation;
import static java.util.Objects.nonNull;

public final class ConfigModular extends CommonModular<ConfigModular> {

    @Delegate
    private final ConfigLoader loader;

    public ConfigModular(Module... modules) {
        this(newArrayList(modules));
    }

    public ConfigModular(Iterable<? extends Module> modules) {
        super(modules);
        this.loader = configLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class<?> clazz) {
        return nonNull(findAnnotation(clazz, configAnnotation(loader)));
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getConfig(clazz);
    }
}
