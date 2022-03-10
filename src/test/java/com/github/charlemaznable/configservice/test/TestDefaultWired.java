package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig("THREAD_DATA")
@DiamondConfig("THREAD_DATA")
public interface TestDefaultWired {

    @ApolloConfig(defaultValueProvider = TestDefaultWiredProvider.class)
    @DiamondConfig(defaultValueProvider = TestDefaultWiredProvider.class)
    String thread();
}
