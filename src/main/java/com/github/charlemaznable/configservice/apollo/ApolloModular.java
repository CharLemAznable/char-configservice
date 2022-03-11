package com.github.charlemaznable.configservice.apollo;

import com.github.charlemaznable.configservice.apollo.ApolloFactory.ApolloLoader;
import com.github.charlemaznable.core.guice.CommonModular;
import com.google.inject.Module;
import com.google.inject.Provider;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.core.lang.Listt.newArrayList;
import static com.github.charlemaznable.core.spring.AnnotationElf.findAnnotation;
import static java.util.Objects.nonNull;

public final class ApolloModular extends CommonModular<ApolloModular> {

    private ApolloLoader loader;

    public ApolloModular(Module... modules) {
        this(newArrayList(modules));
    }

    public ApolloModular(Iterable<? extends Module> modules) {
        super(modules);
        this.loader = apolloLoader(guiceFactory);
    }

    @Override
    public boolean isCandidateClass(Class<?> clazz) {
        return nonNull(findAnnotation(clazz, ApolloConfig.class));
    }

    @Override
    public <T> Provider<T> createProvider(Class<T> clazz) {
        return () -> getApollo(clazz);
    }

    public <T> T getApollo(Class<T> configClass) {
        return this.loader.getApollo(configClass);
    }
}
