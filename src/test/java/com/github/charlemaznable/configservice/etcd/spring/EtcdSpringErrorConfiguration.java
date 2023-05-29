package com.github.charlemaznable.configservice.etcd.spring;

import com.github.charlemaznable.configservice.etcd.EtcdScan;
import com.github.charlemaznable.configservice.test.TestWiredScanAnchor;
import com.github.charlemaznable.core.spring.ElvesImport;
import com.github.charlemaznable.etcdconf.MockEtcdServer;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.context.annotation.Configuration;

import static com.github.charlemaznable.configservice.etcd.EtcdFactory.etcdLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
@EtcdScan(basePackageClasses = TestWiredScanAnchor.class)
public class EtcdSpringErrorConfiguration {

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
