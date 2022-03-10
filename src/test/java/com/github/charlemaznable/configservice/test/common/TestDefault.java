package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.annotation.DefaultEmptyValue;
import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.google.common.base.Splitter;

import java.util.List;

@ApolloConfig("DEFAULT_DATA")
@DiamondConfig("DEFAULT_DATA")
public interface TestDefault {

    String name();

    String full();

    @ApolloConfig("long")
    @DiamondConfig("long")
    String longName();

    default List<String> longSplit() {
        return Splitter.on(" ").omitEmptyStrings()
                .trimResults().splitToList(longName());
    }

    @ApolloConfig(defaultValue = "abc")
    @DiamondConfig(defaultValue = "abc")
    String abc(String defaultValue);

    String xyzNull();

    @DefaultEmptyValue
    String xyzEmpty();

    @DefaultEmptyValue
    @ApolloConfig
    @DiamondConfig
    String xyzEmpty2();

    int count(Integer defaultValue);

    @ApolloConfig(defaultValue = "1")
    @DiamondConfig(defaultValue = "1")
    int count1();

    boolean testMode();

    Boolean testMode2();

    TestDefaultContentBean content();

    List<TestDefaultContentBean> list();

    @ApolloConfig(defaultValue = "10")
    @DiamondConfig(defaultValue = "10")
    short shortValue();

    @ApolloConfig(defaultValue = "200")
    @DiamondConfig(defaultValue = "200")
    int intValue();

    @ApolloConfig(defaultValue = "3000")
    @DiamondConfig(defaultValue = "3000")
    long longValue();

    @ApolloConfig(defaultValue = "40000")
    @DiamondConfig(defaultValue = "40000")
    float floatValue();

    @ApolloConfig(defaultValue = "5")
    @DiamondConfig(defaultValue = "5")
    double doubleValue();

    @ApolloConfig(defaultValue = "97")
    @DiamondConfig(defaultValue = "97")
    byte byteValue();

    @ApolloConfig(defaultValue = "a")
    @DiamondConfig(defaultValue = "a")
    char charValue();

    short shortValueDefault();

    int intValueDefault();

    long longValueDefault();

    float floatValueDefault();

    double doubleValueDefault();

    byte byteValueDefault();

    char charValueDefault();
}
