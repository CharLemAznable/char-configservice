package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.diamond.DiamondConfig.DefaultValueProvider;
import com.github.charlemaznable.configservice.diamond.DiamondModular;

import java.lang.reflect.Method;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.configservice.diamond.DiamondFactory.getDiamond;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public class TestDiamondDefaultInContext implements DefaultValueProvider {

    private TestDefaultWired current;
    private TestDefaultWired spring;
    private TestDefaultWired guice;

    public TestDiamondDefaultInContext() {
        this.current = getDiamond(TestDefaultWired.class);
        this.spring = diamondLoader(springFactory()).getDiamond(TestDefaultWired.class);
        this.guice = new DiamondModular().getDiamond(TestDefaultWired.class);
    }

    @Override
    public String defaultValue(Class<?> configClass, Method method) {
        return current.thread() + "&" + spring.thread() + "&" + guice.thread();
    }
}
