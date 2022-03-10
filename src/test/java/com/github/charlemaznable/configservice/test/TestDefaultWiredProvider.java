package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.github.charlemaznable.core.spring.SpringFactory;

import java.lang.reflect.Method;

public class TestDefaultWiredProvider implements DiamondConfig.DefaultValueProvider, ApolloConfig.DefaultValueProvider {

    private String init;

    public TestDefaultWiredProvider() {
        if (FactoryContext.get() instanceof SpringFactory) {
            init = "spring";
        } else if (FactoryContext.get() instanceof GuiceFactory) {
            init = "guice";
        } else init = "";
    }

    @Override
    public String defaultValue(Class<?> configClass, Method method) {
        if (FactoryContext.get() instanceof SpringFactory) {
            return init + "spring";
        } else if (FactoryContext.get() instanceof GuiceFactory) {
            return init + "guice";
        } else return init;
    }
}
