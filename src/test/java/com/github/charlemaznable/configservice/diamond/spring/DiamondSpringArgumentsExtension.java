package com.github.charlemaznable.configservice.diamond.spring;

import com.github.charlemaznable.core.config.Arguments;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class DiamondSpringArgumentsExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        Arguments.initial("--ConfigService=diamond");
    }

    @Override
    public void afterAll(ExtensionContext context) {
        Arguments.initial();
    }
}
