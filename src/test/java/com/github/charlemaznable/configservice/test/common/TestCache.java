package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.Config;

@Config(keyset = "CACHE_GROUP", key = "CACHE_KEY", cacheSeconds = 1)
public interface TestCache {

    String key1();

    String key2();
}
