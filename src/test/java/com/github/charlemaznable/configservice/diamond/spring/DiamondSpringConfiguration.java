package com.github.charlemaznable.configservice.diamond.spring;

import com.github.charlemaznable.configservice.ConfigScan;
import com.github.charlemaznable.configservice.test.TestWiredDataId;
import com.github.charlemaznable.configservice.test.TestWiredScanAnchor;
import com.github.charlemaznable.core.spring.ElvesImport;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
@ConfigScan(basePackageClasses = TestWiredScanAnchor.class)
public class DiamondSpringConfiguration {

    @Bean
    public TestWiredDataId testWiredDataId() {
        return new TestWiredDataId() {
            @Override
            public String dataId(Class<?> configClass, Method method) {
                return "long";
            }
        };
    }

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
