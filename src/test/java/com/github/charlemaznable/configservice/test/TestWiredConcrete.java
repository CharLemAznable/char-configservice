package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.etcd.EtcdConfig;

@ApolloConfig("DEFAULT_DATA")
@DiamondConfig("DEFAULT_DATA")
@EtcdConfig("DEFAULT_DATA")
public class TestWiredConcrete {
}
