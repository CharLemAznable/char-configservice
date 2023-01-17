package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.impl.AbstractConfigScannerRegistrar;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.core.spring.SpringFactory.springFactory;

public final class DiamondScannerRegistrar extends AbstractConfigScannerRegistrar {

    public DiamondScannerRegistrar() {
        super(DiamondScan.class, diamondLoader(springFactory()));
    }
}
