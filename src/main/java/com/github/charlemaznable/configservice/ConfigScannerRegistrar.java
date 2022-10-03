package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.apollo.ApolloFactory.ApolloLoader;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.configservice.ConfigFactory.configLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public final class ConfigScannerRegistrar extends SpringScannerRegistrar {

    private static ConfigLoader loader = configLoader(springFactory());

    public ConfigScannerRegistrar() {
        super(ConfigScan.class, ConfigFactoryBean.class,
                loader instanceof ApolloLoader ? ApolloConfig.class : DiamondConfig.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class ConfigFactoryBean extends SpringFactoryBean {

        public ConfigFactoryBean() {
            super(loader::getConfig);
        }
    }
}
