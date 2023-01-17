package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.Config;

@Config("THREAD_DATA")
public interface TestDefaultWired {

    @Config(defaultValueProvider = TestDefaultWiredProvider.class)
    String thread();
}
