package com.github.charlemaznable.miner.spring;

import com.github.charlemaznable.core.spring.ElvesImport;
import com.github.charlemaznable.miner.MinerScan;
import com.github.charlemaznable.miner.testminer.TestMinerDataId;
import com.github.charlemaznable.miner.testminer.TestMinerScanAnchor;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Method;

import static com.github.charlemaznable.miner.MinerFactory.springMinerLoader;
import static org.joor.Reflect.on;

@Configuration
@ElvesImport
@MinerScan(basePackageClasses = TestMinerScanAnchor.class)
public class MinerSpringConfiguration {

    @Bean
    public TestMinerDataId testMinerDataId() {
        return new TestMinerDataId() {
            @Override
            public String dataId(Class<?> minerClass, Method method) {
                return "long";
            }
        };
    }

    @PostConstruct
    public void postConstruct() {
        on(springMinerLoader()).field("minerCache").call("invalidateAll");
        MockDiamondServer.setUpMockServer();
    }

    @PreDestroy
    public void preDestroy() {
        MockDiamondServer.tearDownMockServer();
    }
}
