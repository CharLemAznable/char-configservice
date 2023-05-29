package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.annotation.DefaultEmptyValue;
import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.etcd.EtcdConfig;

import java.util.Properties;

@ApolloConfig
@DiamondConfig
@EtcdConfig
public interface TestDefaultDataEmpty {

    @ApolloConfig("DEFAULT_DATA_EMPTY")
    @DiamondConfig("DEFAULT_DATA_EMPTY")
    @EtcdConfig("DEFAULT_DATA_EMPTY")
    Properties propertiesNull();

    @DefaultEmptyValue
    @ApolloConfig("DEFAULT_DATA_EMPTY")
    @DiamondConfig("DEFAULT_DATA_EMPTY")
    @EtcdConfig("DEFAULT_DATA_EMPTY")
    Properties propertiesEmpty();
}
