package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig(namespace = "Arg", propertyName = "data")
@DiamondConfig(group = "Arg", dataId = "data")
public interface TestConfigArg {

    @ApolloConfig("custom1.${customKey1}")
    @DiamondConfig("custom1.${customKey1}")
    String custom1();

    @ApolloConfig("custom2.${customKey2}")
    @DiamondConfig("custom2.${customKey2}")
    String custom2();
}
