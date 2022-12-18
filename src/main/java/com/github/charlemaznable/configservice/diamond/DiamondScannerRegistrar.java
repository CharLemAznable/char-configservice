package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.diamond.DiamondFactory.DiamondLoader;
import com.github.charlemaznable.core.spring.SpringFactoryBean;
import com.github.charlemaznable.core.spring.SpringScannerRegistrar;
import org.springframework.core.type.ClassMetadata;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public final class DiamondScannerRegistrar extends SpringScannerRegistrar {

    private static final DiamondLoader loader = diamondLoader(springFactory());

    public DiamondScannerRegistrar() {
        super(DiamondScan.class, DiamondFactoryBean.class, DiamondConfig.class);
    }

    @Override
    protected boolean isCandidateClass(ClassMetadata classMetadata) {
        return classMetadata.isInterface();
    }

    public static class DiamondFactoryBean extends SpringFactoryBean {

        public DiamondFactoryBean() {
            super(loader::getDiamond);
        }
    }
}
