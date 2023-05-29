package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.etcd.EtcdConfig;

@ApolloConfig(namespace = "CACHE_GROUP", propertyName = "CACHE_KEY")
@DiamondConfig(group = "CACHE_GROUP", dataId = "CACHE_KEY")
@EtcdConfig(namespace = "CACHE_GROUP", key = "CACHE_KEY")
public interface TestCacheNone {

    String key1();

    String key2();
}
