package com.github.charlemaznable.configservice.apollo;

import com.github.charlemaznable.configservice.impl.AbstractConfigModular;
import com.google.inject.Module;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

public final class ApolloModular extends AbstractConfigModular<ApolloModular> {

    public ApolloModular(Module... modules) {
        this(newArrayList(modules));
    }

    public ApolloModular(Iterable<? extends Module> modules) {
        super(modules, ApolloFactory::apolloLoader);
    }

    public <T> T getApollo(Class<T> configClass) {
        return getConfig(configClass);
    }
}
