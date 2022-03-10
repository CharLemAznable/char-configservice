package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

import java.util.Properties;

@ApolloConfig
@DiamondConfig
public interface TestDefaultData {

    @ApolloConfig("DEFAULT_DATA")
    @DiamondConfig("DEFAULT_DATA")
    Properties properties();
}
