package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.etcd.EtcdConfig;

@ApolloConfig
@DiamondConfig
@EtcdConfig
public interface TestBase {

    @Config("base.data")
    String abc();

    @Config(keyset = "base.group", key = "base.data")
    String xyz();
}
