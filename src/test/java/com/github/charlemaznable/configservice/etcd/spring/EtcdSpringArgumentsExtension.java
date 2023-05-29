package com.github.charlemaznable.configservice.etcd.spring;

import com.github.charlemaznable.core.config.Arguments;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class EtcdSpringArgumentsExtension implements BeforeAllCallback, AfterAllCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        Arguments.initial("--ConfigService=etcd");
    }

    @Override
    public void afterAll(ExtensionContext context) {
        Arguments.initial();
    }
}
