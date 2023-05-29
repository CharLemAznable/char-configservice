package com.github.charlemaznable.configservice;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.Factory;
import lombok.NoArgsConstructor;
import lombok.val;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static com.github.charlemaznable.configservice.etcd.EtcdFactory.etcdLoader;
import static lombok.AccessLevel.PRIVATE;

@SuppressWarnings("FieldMayBeFinal")
@NoArgsConstructor(access = PRIVATE)
public final class ConfigFactory {

    private static final String APOLLO_CLZ = "com.ctrip.framework.apollo.ConfigService";
    private static final String DIAMOND_CLZ = "org.n3r.diamond.client.Miner";
    private static final String ETCDCONF_CLZ = "com.github.charlemaznable.etcdconf.EtcdConfigService";
    private static final String ENV_SOURCE = "${ConfigService:-diamond}";

    private static boolean hasApollo;
    private static boolean hasDiamond;
    private static boolean hasEtcdconf;
    private static int count;

    static {
        hasApollo = ClzPath.classExists(APOLLO_CLZ);
        hasDiamond = ClzPath.classExists(DIAMOND_CLZ);
        hasEtcdconf = ClzPath.classExists(ETCDCONF_CLZ);
        int ct = 0;
        if (hasApollo) ct++;
        if (hasDiamond) ct++;
        if (hasDiamond) ct++;
        count = ct;
    }

    public static <T> T getConfig(Class<T> configClass) {
        return configLoader(FactoryContext.get()).getConfig(configClass);
    }

    public static ConfigLoader configLoader(Factory factory) {
        val env = substitute(ENV_SOURCE);
        if (count == 3) { // has all 3 dependencies
            if ("apollo".equalsIgnoreCase(env)) {
                return apolloLoader(factory);
            } else if ("diamond".equalsIgnoreCase(env)) {
                return diamondLoader(factory);
            } else if ("etcd".equalsIgnoreCase(env)) {
                return etcdLoader(factory);
            } else {
                throw new IllegalStateException("Illegal env setting: " + env);
            }

        } else if (count == 2) { // has some 2 dependencies
            if (hasApollo && "apollo".equalsIgnoreCase(env)) {
                return apolloLoader(factory);
            } else if (hasDiamond && "diamond".equalsIgnoreCase(env)) {
                return diamondLoader(factory);
            } else if (hasEtcdconf && "etcd".equalsIgnoreCase(env)) {
                return etcdLoader(factory);
            } else {
                throw new IllegalStateException("Illegal env setting: " + env);
            }

        } else if (count == 1) { // has only 1 dependency
            if (hasApollo) {
                return apolloLoader(factory);
            } else if (hasDiamond) {
                return diamondLoader(factory);
            } else if (hasEtcdconf) {
                return etcdLoader(factory);
            } else {
                throw new IllegalStateException("None of Apollo/Diamond/EtcdConf found.");
            }

        } else {
            throw new IllegalStateException("None of Apollo/Diamond/EtcdConf found.");
        }
    }
}
