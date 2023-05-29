package com.github.charlemaznable.configservice.etcd;

import com.github.charlemaznable.configservice.impl.AbstractConfigModular;
import com.google.inject.Module;

import static com.github.charlemaznable.core.lang.Listt.newArrayList;

public final class EtcdModular extends AbstractConfigModular<EtcdModular> {

    public EtcdModular(Module... modules) {
        this(newArrayList(modules));
    }

    public EtcdModular(Iterable<? extends Module> modules) {
        super(modules, EtcdFactory::etcdLoader);
    }

    public <T> T getEtcd(Class<T> configClass) {
        return getConfig(configClass);
    }
}
