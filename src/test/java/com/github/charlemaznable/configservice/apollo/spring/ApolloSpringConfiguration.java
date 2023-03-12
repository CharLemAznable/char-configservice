package com.github.charlemaznable.configservice.apollo.spring;

import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.configservice.ConfigScan;
import com.github.charlemaznable.configservice.ConfigScannerRegistrar;
import com.github.charlemaznable.configservice.TestUnWired;
import com.github.charlemaznable.configservice.impl.AbstractConfigScannerRegistrar;
import com.github.charlemaznable.configservice.test.TestWiredScanAnchor;
import com.github.charlemaznable.core.spring.ElvesImport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
@ConfigScan(basePackageClasses = TestWiredScanAnchor.class)
public class ApolloSpringConfiguration {

    @Bean
    public AbstractConfigScannerRegistrar.ConfigFactoryBean testUnWired() {
        return ConfigScannerRegistrar.buildFactoryBean(TestUnWired.class);
    }

    @PostConstruct
    public void postConstruct() {
        on(apolloLoader(springFactory())).field("configCache").call("invalidateAll");
        MockApolloServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockApolloServer.tearDownMockServer();
    }
}
