package com.github.charlemaznable.configservice;

import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import lombok.NoArgsConstructor;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.apolloLoader;
import static com.github.charlemaznable.configservice.diamond.DiamondFactory.diamondLoader;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ConfigFactory {

    private static final String ENV_SOURCE = "${ConfigService:-diamond}";

    public static <T> T getConfig(Class<T> configClass) {
        return configLoader(FactoryContext.get()).getConfig(configClass);
    }

    public static ConfigLoader configLoader(Factory factory) {
        if ("apollo".equalsIgnoreCase(substitute(ENV_SOURCE))) {
            return apolloLoader(factory);
        }
        return diamondLoader(factory);
    }
}
