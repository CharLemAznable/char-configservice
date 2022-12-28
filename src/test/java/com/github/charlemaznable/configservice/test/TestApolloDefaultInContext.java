package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.apollo.ApolloConfig.DefaultValueProvider;
import com.github.charlemaznable.configservice.apollo.ApolloModular;

import java.lang.reflect.Method;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.configservice.apollo.ApolloFactory.getApollo;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public class TestApolloDefaultInContext implements DefaultValueProvider {

    private final TestDefaultWired current;
    private final TestDefaultWired spring;
    private final TestDefaultWired guice;

    public TestApolloDefaultInContext() {
        this.current = getApollo(TestDefaultWired.class);
        this.spring = apolloLoader(springFactory()).getApollo(TestDefaultWired.class);
        this.guice = new ApolloModular().getApollo(TestDefaultWired.class);
    }

    @Override
    public String defaultValue(Class<?> configClass, Method method) {
        return current.thread() + "&" + spring.thread() + "&" + guice.thread();
    }
}
