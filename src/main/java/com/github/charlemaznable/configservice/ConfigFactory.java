package com.github.charlemaznable.configservice;

import lombok.NoArgsConstructor;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.getApollo;
import static com.github.charlemaznable.configservice.diamond.DiamondFactory.getDiamond;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public final class ConfigFactory {

    private static final String ENV_SOURCE = "${ConfigService:-diamond}";

    public static <T> T getConfig(Class<T> configClass) {
        if ("apollo".equalsIgnoreCase(substitute(ENV_SOURCE))) {
            return getApollo(configClass);
        }
        return getDiamond(configClass);
    }
}
