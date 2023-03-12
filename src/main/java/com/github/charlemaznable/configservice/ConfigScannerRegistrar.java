package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.impl.AbstractConfigScannerRegistrar;

import static com.github.charlemaznable.configservice.ConfigFactory.configLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public final class ConfigScannerRegistrar extends AbstractConfigScannerRegistrar {

    public ConfigScannerRegistrar() {
        super(ConfigScan.class, configLoader(springFactory()));
    }

    public static ConfigFactoryBean buildFactoryBean(Class<?> xyzInterface) {
        return AbstractConfigScannerRegistrar.buildFactoryBean(
                xyzInterface, configLoader(springFactory()));
    }
}
