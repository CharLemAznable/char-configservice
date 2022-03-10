package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig(namespace = "CACHE_GROUP", propertyName = "CACHE_KEY", cacheSeconds = 1)
@DiamondConfig(group = "CACHE_GROUP", dataId = "CACHE_KEY", cacheSeconds = 1)
public interface TestCache {

    String key1();

    String key2();
}
