package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig("DEFAULT_DATA")
@DiamondConfig("DEFAULT_DATA")
public interface TestGetterDefault extends ConfigGetter {
}
