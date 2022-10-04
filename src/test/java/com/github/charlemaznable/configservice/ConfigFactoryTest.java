package com.github.charlemaznable.configservice;

import com.github.charlemaznable.configservice.apollo.ApolloFactory.ApolloLoader;
import com.github.charlemaznable.configservice.diamond.DiamondFactory.DiamondLoader;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ConfigFactoryTest {

    @Test
    public void testConfigLoader() {
        val reflectConfigFactory = onClass(ConfigFactory.class);
        boolean hasApollo = reflectConfigFactory.get("hasApollo");
        boolean hasDiamond = reflectConfigFactory.get("hasDiamond");

        reflectConfigFactory.set("hasApollo", true);
        reflectConfigFactory.set("hasDiamond", false);
        assertTrue(ConfigFactory.configLoader(reflectFactory()) instanceof ApolloLoader);

        reflectConfigFactory.set("hasApollo", false);
        reflectConfigFactory.set("hasDiamond", true);
        assertTrue(ConfigFactory.configLoader(reflectFactory()) instanceof DiamondLoader);

        reflectConfigFactory.set("hasApollo", false);
        reflectConfigFactory.set("hasDiamond", false);
        assertThrows(IllegalStateException.class,
                () -> ConfigFactory.configLoader(reflectFactory()));

        reflectConfigFactory.set("hasApollo", hasApollo);
        reflectConfigFactory.set("hasDiamond", hasDiamond);
    }
}
