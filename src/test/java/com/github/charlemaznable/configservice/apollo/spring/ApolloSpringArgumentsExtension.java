package com.github.charlemaznable.configservice.apollo.spring;

import com.github.charlemaznable.configservice.ConfigScannerRegistrar;
import com.github.charlemaznable.core.config.Arguments;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import static com.github.charlemaznable.configservice.ConfigFactory.configLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.n3r.eql.joor.Reflect.on;

public class ApolloSpringArgumentsExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        Arguments.initial("--ConfigService=apollo");
        on(ConfigScannerRegistrar.class).set("loader", configLoader(springFactory()));
    }

    @Override
    public void afterAll(ExtensionContext context) {
        Arguments.initial();
    }
}
