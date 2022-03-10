package com.github.charlemaznable.configservice.apollo.spring;

import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.core.spring.ElvesImport;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
public class ApolloSpringNakedConfiguration {

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
