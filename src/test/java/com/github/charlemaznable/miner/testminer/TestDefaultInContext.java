package com.github.charlemaznable.miner.testminer;

import com.github.charlemaznable.miner.MinerConfig.DefaultValueProvider;
import com.github.charlemaznable.miner.MinerModular;

import java.lang.reflect.Method;

import static com.github.charlemaznable.miner.MinerFactory.getMiner;
import static com.github.charlemaznable.miner.MinerFactory.springMinerLoader;

public class TestDefaultInContext implements DefaultValueProvider {

    private TestDefaultMiner current;
    private TestDefaultMiner spring;
    private TestDefaultMiner guice;

    public TestDefaultInContext() {
        this.current = getMiner(TestDefaultMiner.class);
        this.spring = springMinerLoader().getMiner(TestDefaultMiner.class);
        this.guice = new MinerModular().getMiner(TestDefaultMiner.class);
    }

    @Override
    public String defaultValue(Class<?> minerClass, Method method) {
        return current.thread() + "&" + spring.thread() + "&" + guice.thread();
    }
}
