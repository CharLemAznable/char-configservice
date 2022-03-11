package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.diamond.DiamondFactory.DiamondLoader;
import com.github.charlemaznable.core.guice.CommonModular;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.spring.AnnotationElf.findAnnotation;
import static java.util.Objects.nonNull;

public final class DiamondModular extends CommonModular<DiamondModular> {

    private DiamondLoader loader;

    public DiamondModular(Module... modules) {
        this(newArrayList(modules));
    }

    public DiamondModular(Iterable<? extends Module> modules) {
        super(modules);
        this.loader = diamondLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class<?> clazz) {
        return nonNull(findAnnotation(clazz, DiamondConfig.class));
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getDiamond(clazz);
    }

    public <T> T getDiamond(Class<T> configClass) {
        return this.loader.getDiamond(configClass);
    }
}
