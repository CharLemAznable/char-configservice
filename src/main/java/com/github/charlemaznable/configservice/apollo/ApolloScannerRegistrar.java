package com.github.charlemaznable.configservice.apollo;

import com.github.charlemaznable.configservice.apollo.ApolloFactory.ApolloLoader;
import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public final class ApolloScannerRegistrar extends SpringScannerRegistrar {

    private static ApolloLoader loader = apolloLoader(springFactory());

    public ApolloScannerRegistrar() {
        super(ApolloScan.class, ApolloFactoryBean.class, ApolloConfig.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class ApolloFactoryBean extends SpringFactoryBean {

        public ApolloFactoryBean() {
            super(loader::getApollo);
        }
    }
}
