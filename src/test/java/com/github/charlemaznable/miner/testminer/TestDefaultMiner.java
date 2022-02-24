package com.github.charlemaznable.miner.testminer;

import com.github.charlemaznable.miner.MinerConfig;

@MinerConfig("THREAD_DATA")
public interface TestDefaultMiner {

    @MinerConfig(defaultValueProvider = TestDefaultMinerProvider.class)
    String thread();
}
