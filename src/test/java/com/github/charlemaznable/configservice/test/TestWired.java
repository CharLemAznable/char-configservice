package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;

@ApolloConfig("DEFAULT_DATA")
@DiamondConfig("DEFAULT_DATA")
public interface TestWired {

    String name();

    String full();

    @ApolloConfig(propertyNameProvider = TestWiredPropertyName.class)
    @DiamondConfig(dataIdProvider = TestWiredDataId.class)
    String longName();

    @ApolloConfig(propertyNameProvider = TestWiredPropertyNameWrapper.class)
    @DiamondConfig(dataIdProvider = TestWiredDataIdWrapper.class)
    String longWrap();

    String abc(String defaultValue);

    @ApolloConfig(defaultValueProvider = TestApolloDefaultInContext.class)
    @DiamondConfig(defaultValueProvider = TestDiamondDefaultInContext.class)
    String defaultInContext();
}
