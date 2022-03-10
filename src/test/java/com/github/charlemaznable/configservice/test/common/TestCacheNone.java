package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig(namespace = "CACHE_GROUP", propertyName = "CACHE_KEY")
@DiamondConfig(group = "CACHE_GROUP", dataId = "CACHE_KEY")
public interface TestCacheNone {

    String key1();

    String key2();
}
