package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig
@DiamondConfig
public interface TestBase {

    @Config("base.data")
    String abc();

    @Config(keyset = "base.group", key = "base.data")
    String xyz();
}
