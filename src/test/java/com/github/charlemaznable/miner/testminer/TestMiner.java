package com.github.charlemaznable.miner.testminer;

import com.github.charlemaznable.miner.MinerConfig;

@MinerConfig("DEFAULT_DATA")
public interface TestMiner {

    String name();

    String full();

    @MinerConfig(dataIdProvider = TestMinerDataId.class)
    String longName();

    @MinerConfig(dataIdProvider = TestMinerDataIdWrapper.class)
    String longWrap();

    String abc(String defaultValue);

    @MinerConfig(defaultValueProvider = TestDefaultInContext.class)
    String defaultInContext();
}
