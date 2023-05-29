package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.etcd.EtcdConfig;

import java.util.Map;
import java.util.Properties;

@ApolloConfig(namespace = "DEF_GROUP")
@DiamondConfig(group = "DEF_GROUP")
@EtcdConfig(namespace = "DEF_GROUP")
public interface TestDefData {

    @ApolloConfig("DEF_DATA")
    @DiamondConfig("DEF_DATA")
    @EtcdConfig("DEF_DATA")
    Properties properties();

    @ApolloConfig("DEF_DATA")
    @DiamondConfig("DEF_DATA")
    @EtcdConfig("DEF_DATA")
    Map<String, Object> map();
}
