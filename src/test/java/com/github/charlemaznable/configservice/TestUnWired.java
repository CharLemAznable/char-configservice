package com.github.charlemaznable.configservice;

@Config("DEFAULT_DATA")
public interface TestUnWired {

    String name();

    String full();

    String abc(String defaultValue);
}
