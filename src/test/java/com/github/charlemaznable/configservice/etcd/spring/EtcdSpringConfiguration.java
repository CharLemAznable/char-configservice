package com.github.charlemaznable.configservice.etcd.spring;

import com.github.charlemaznable.configservice.ConfigScan;
import com.github.charlemaznable.configservice.ConfigScannerRegistrar;
import com.github.charlemaznable.configservice.TestUnWired;
import com.github.charlemaznable.configservice.impl.AbstractConfigScannerRegistrar;
import com.github.charlemaznable.configservice.test.TestWiredScanAnchor;
import com.github.charlemaznable.core.spring.ElvesImport;
import com.github.charlemaznable.etcdconf.MockEtcdServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.charlemaznable.configservice.etcd.EtcdFactory.etcdLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
@ConfigScan(basePackageClasses = TestWiredScanAnchor.class)
public class EtcdSpringConfiguration {

    @Bean
    public AbstractConfigScannerRegistrar.ConfigFactoryBean testUnWired() {
        return ConfigScannerRegistrar.buildFactoryBean(TestUnWired.class);
    }

    @PostConstruct
    public void postConstruct() {
        on(etcdLoader(springFactory())).field("configCache").call("invalidateAll");
        MockEtcdServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockEtcdServer.tearDownMockServer();
    }
}
