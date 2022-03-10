package com.github.charlemaznable.configservice.apollo.spring;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.test.TestWired;

@ApolloConfig("SUB_DATA")
public interface TestWiredSub extends TestWired {
}
