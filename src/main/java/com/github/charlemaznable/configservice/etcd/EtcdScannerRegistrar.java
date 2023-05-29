package com.github.charlemaznable.configservice.etcd;

import com.github.charlemaznable.configservice.impl.AbstractConfigScannerRegistrar;

import static com.github.charlemaznable.configservice.etcd.EtcdFactory.etcdLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public final class EtcdScannerRegistrar extends AbstractConfigScannerRegistrar {

    public EtcdScannerRegistrar() {
        super(EtcdScan.class, etcdLoader(springFactory()));
    }

    public static ConfigFactoryBean buildFactoryBean(Class<?> xyzInterface) {
        return AbstractConfigScannerRegistrar.buildFactoryBean(
                xyzInterface, etcdLoader(springFactory()));
    }
}
