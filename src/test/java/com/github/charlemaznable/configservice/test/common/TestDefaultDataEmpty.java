package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.annotation.DefaultEmptyValue;
import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

import java.util.Properties;

@ApolloConfig
@DiamondConfig
public interface TestDefaultDataEmpty {

    @ApolloConfig("DEFAULT_DATA_EMPTY")
    @DiamondConfig("DEFAULT_DATA_EMPTY")
    Properties propertiesNull();

    @DefaultEmptyValue
    @ApolloConfig("DEFAULT_DATA_EMPTY")
    @DiamondConfig("DEFAULT_DATA_EMPTY")
    Properties propertiesEmpty();
}
