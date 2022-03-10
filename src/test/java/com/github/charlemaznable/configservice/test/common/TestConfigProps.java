package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.apollo.ApolloConfig.NamespaceProvider;
import com.github.charlemaznable.configservice.apollo.ApolloConfig.PropertyNameProvider;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig.DataIdProvider;
import com.github.charlemaznable.configservice.diamond.DiamondConfig.GroupProvider;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ApolloConfig(
        namespaceProvider = TestConfigProps.TestNamespaceProvider.class,
        propertyNameProvider = TestConfigProps.TestPropertyNameProvider.class
)
@DiamondConfig(
        groupProvider = TestConfigProps.TestGroupProvider.class,
        dataIdProvider = TestConfigProps.TestDataIdProvider.class
)
public interface TestConfigProps {

    String name();

    String full();

    @ApolloConfig("long")
    @DiamondConfig("long")
    String longName();

    @ApolloConfig(
            namespaceProvider = TestNamespaceProvider.class,
            propertyNameProvider = TestPropertyNameProvider.class,
            defaultValueProvider = TestApolloDefaultValueProvider.class
    )
    @DiamondConfig(
            groupProvider = TestGroupProvider.class,
            dataIdProvider = TestDataIdProvider.class,
            defaultValueProvider = TestDiamondDefaultValueProvider.class
    )
    String prop();

    class TestGroupProvider implements GroupProvider {

        @Override
        public String group(Class<?> configClass) {
            assertEquals(TestConfigProps.class, configClass);
            return "${group}Group";
        }

        @Override
        public String group(Class<?> configClass, Method method) {
            assertEquals(TestConfigProps.class, configClass);
            assertEquals("prop", method.getName());
            return "";
        }
    }

    class TestDataIdProvider implements DataIdProvider {

        @Override
        public String dataId(Class<?> configClass) {
            assertEquals(TestConfigProps.class, configClass);
            return "Data${data}";
        }

        @Override
        public String dataId(Class<?> configClass, Method method) {
            assertEquals(TestConfigProps.class, configClass);
            assertEquals("prop", method.getName());
            return "Prop${prop}";
        }
    }

    class TestDiamondDefaultValueProvider implements DiamondConfig.DefaultValueProvider {

        @Override
        public String defaultValue(Class<?> configClass, Method method) {
            assertEquals(TestConfigProps.class, configClass);
            assertEquals("prop", method.getName());
            return "${default}Default";
        }
    }

    class TestNamespaceProvider implements NamespaceProvider {

        @Override
        public String namespace(Class<?> configClass) {
            assertEquals(TestConfigProps.class, configClass);
            return "${group}Group";
        }

        @Override
        public String namespace(Class<?> configClass, Method method) {
            assertEquals(TestConfigProps.class, configClass);
            assertEquals("prop", method.getName());
            return "";
        }
    }

    class TestPropertyNameProvider implements PropertyNameProvider {

        @Override
        public String propertyName(Class<?> configClass) {
            assertEquals(TestConfigProps.class, configClass);
            return "Data${data}";
        }

        @Override
        public String propertyName(Class<?> configClass, Method method) {
            assertEquals(TestConfigProps.class, configClass);
            assertEquals("prop", method.getName());
            return "Prop${prop}";
        }
    }

    class TestApolloDefaultValueProvider implements ApolloConfig.DefaultValueProvider {

        @Override
        public String defaultValue(Class<?> configClass, Method method) {
            assertEquals(TestConfigProps.class, configClass);
            assertEquals("prop", method.getName());
            return "${default}Default";
        }
    }
}
