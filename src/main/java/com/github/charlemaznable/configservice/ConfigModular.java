package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.impl.AbstractConfigModular;
import com.google.inject.Module;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

public final class ConfigModular extends AbstractConfigModular<ConfigModular> {

    public ConfigModular(Module... modules) {
        this(newArrayList(modules));
    }

    public ConfigModular(Iterable<? extends Module> modules) {
        super(modules, ConfigFactory::configLoader);
    }
}
