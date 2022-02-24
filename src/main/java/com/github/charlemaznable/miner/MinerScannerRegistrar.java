package com.github.charlemaznable.miner;

import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import com.github.charlemaznable.miner.MinerFactory.MinerLoader;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.miner.MinerFactory.springMinerLoader;

public final class MinerScannerRegistrar extends SpringScannerRegistrar {

    private static MinerLoader springMinerLoader = springMinerLoader();

    public MinerScannerRegistrar() {
        super(MinerScan.class, MinerFactoryBean.class, MinerConfig.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class MinerFactoryBean extends SpringFactoryBean {

        public MinerFactoryBean() {
            super(springMinerLoader::getMiner);
        }
    }
}
