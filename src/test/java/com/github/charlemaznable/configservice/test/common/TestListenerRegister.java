package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.configservice.ConfigListenerRegister;

@Config(keyset = "Listener", key = "data")
public interface TestListenerRegister extends ConfigListenerRegister {
}
