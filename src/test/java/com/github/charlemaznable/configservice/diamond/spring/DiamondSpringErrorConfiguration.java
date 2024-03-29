package com.github.charlemaznable.configservice.diamond.spring;

import com.github.charlemaznable.configservice.diamond.DiamondScan;
import com.github.charlemaznable.configservice.test.TestWiredScanAnchor;
import com.github.charlemaznable.core.spring.ElvesImport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.context.annotation.Configuration;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
@DiamondScan(basePackageClasses = TestWiredScanAnchor.class)
public class DiamondSpringErrorConfiguration {

    @PostConstruct
    public void postConstruct() {
        on(diamondLoader(springFactory())).field("configCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
