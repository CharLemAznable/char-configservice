package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.impl.AbstractConfigModular;
import com.google.inject.Module;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

public final class DiamondModular extends AbstractConfigModular<DiamondModular> {

    public DiamondModular(Module... modules) {
        this(newArrayList(modules));
    }

    public DiamondModular(Iterable<? extends Module> modules) {
        super(modules, DiamondFactory::diamondLoader);
    }

    public <T> T getDiamond(Class<T> configClass) {
        return getConfig(configClass);
    }
}
