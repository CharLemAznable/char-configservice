package com.github.charlemaznable.miner;

import com.github.charlemaznable.core.guice.CommonModular;
import com.github.charlemaznable.miner.MinerFactory.MinerLoader;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.spring.AnnotationElf.findAnnotation;
import static com.github.charlemaznable.miner.MinerFactory.minerLoader;
import static java.util.Objects.nonNull;

public final class MinerModular extends CommonModular<MinerModular> {

    private MinerLoader minerLoader;

    public MinerModular(Module... modules) {
        this(newArrayList(modules));
    }

    public MinerModular(Iterable<? extends Module> modules) {
        super(modules);
        this.minerLoader = minerLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class clazz) {
        return nonNull(findAnnotation(clazz, MinerConfig.class));
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getMiner(clazz);
    }

    public <T> T getMiner(Class<T> minerClass) {
        return this.minerLoader.getMiner(minerClass);
    }
}
