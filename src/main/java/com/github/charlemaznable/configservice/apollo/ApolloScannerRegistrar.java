package com.github.charlemaznable.configservice.apollo;

import com.github.charlemaznable.configservice.impl.AbstractConfigScannerRegistrar;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public final class ApolloScannerRegistrar extends AbstractConfigScannerRegistrar {

    public ApolloScannerRegistrar() {
        super(ApolloScan.class, apolloLoader(springFactory()));
    }
}
