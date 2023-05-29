package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.etcd.EtcdConfig;

import java.util.Properties;

@ApolloConfig
@DiamondConfig
@EtcdConfig
public interface TestDefaultData {

    @ApolloConfig("DEFAULT_DATA")
    @DiamondConfig("DEFAULT_DATA")
    @EtcdConfig("DEFAULT_DATA")
    Properties properties();
}
