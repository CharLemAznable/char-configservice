package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.apollo.ApolloFactory.ApolloLoader;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.ClzPath;
import com.github.charlemaznable.core.lang.Factory;
import lombok.NoArgsConstructor;

import java.lang.annotation.Annotation;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ConfigFactory {

    private static final String APOLLO_CLZ = "com.ctrip.framework.apollo.ConfigService";
    private static final String DIAMOND_CLZ = "org.n3r.diamond.client.Miner";
    private static final String ENV_SOURCE = "${ConfigService:-diamond}";

    private static boolean hasApollo;
    private static boolean hasDiamond;

    static {
        hasApollo = ClzPath.classExists(APOLLO_CLZ);
        hasDiamond = ClzPath.classExists(DIAMOND_CLZ);
    }

    public static <T> T getConfig(Class<T> configClass) {
        return configLoader(FactoryContext.get()).getConfig(configClass);
    }

    public static ConfigLoader configLoader(Factory factory) {
        if (hasApollo && hasDiamond) {
            if ("apollo".equalsIgnoreCase(substitute(ENV_SOURCE))) {
                return apolloLoader(factory);
            }
            return diamondLoader(factory);
        } else if (hasApollo) return apolloLoader(factory);
        else if (hasDiamond) return diamondLoader(factory);
        else {
            throw new IllegalStateException("Neither Apollo nor Diamond found.");
        }
    }

    public static Class<? extends Annotation> configAnnotation(ConfigLoader configLoader) {
        return configLoader instanceof ApolloLoader ? ApolloConfig.class : DiamondConfig.class;
    }
}
