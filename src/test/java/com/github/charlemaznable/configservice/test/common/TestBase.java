package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig
@DiamondConfig
public interface TestBase {

    @ApolloConfig("base.data")
    @DiamondConfig("base.data")
    String abc();

    @ApolloConfig(namespace = "base.group", propertyName = "base.data")
    @DiamondConfig(group = "base.group", dataId = "base.data")
    String xyz();
}
