package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.Config;

@Config("DEFAULT_DATA")
public interface TestWired {

    String name();

    String full();

    String abc(String defaultValue);
}
