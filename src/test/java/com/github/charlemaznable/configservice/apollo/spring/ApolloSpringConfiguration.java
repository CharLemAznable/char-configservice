package com.github.charlemaznable.configservice.apollo.spring;

import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.configservice.apollo.ApolloScan;
import com.github.charlemaznable.configservice.test.TestWiredPropertyName;
import com.github.charlemaznable.configservice.test.TestWiredScanAnchor;
import com.github.charlemaznable.core.spring.ElvesImport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
@ApolloScan(basePackageClasses = TestWiredScanAnchor.class)
public class ApolloSpringConfiguration {

    @Bean
    public TestWiredPropertyName testWiredDataId() {
        return new TestWiredPropertyName() {
            @Override
            public String propertyName(Class<?> configClass, Method method) {
                return "long";
            }
        };
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
