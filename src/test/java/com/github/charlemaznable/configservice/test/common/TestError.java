package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.apollo.ApolloConfig.NamespaceProvider;
import com.github.charlemaznable.configservice.apollo.ApolloConfig.PropertyNameProvider;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig.DataIdProvider;
import com.github.charlemaznable.configservice.diamond.DiamondConfig.GroupProvider;

import java.lang.reflect.Method;

public interface TestError {

    @ApolloConfig(
            namespaceProvider = ErrorNamespaceProvider.class
    )
    @DiamondConfig(
            groupProvider = ErrorGroupProvider.class
    )
    interface ProvideError1 {

        String prop();
    }

    @ApolloConfig(
            namespaceProvider = NoErrorNamespaceProvider.class,
            propertyNameProvider = ErrorPropertyNameProvider.class
    )
    @DiamondConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = ErrorDataIdProvider.class
    )
    interface ProvideError2 {

        String prop();
    }

    @ApolloConfig(
            namespaceProvider = NoErrorNamespaceProvider.class,
            propertyNameProvider = NoErrorPropertyNameProvider.class
    )
    @DiamondConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = NoErrorDataIdProvider.class
    )
    interface ProvideError3 {

        @ApolloConfig(
                namespaceProvider = ErrorNamespaceProvider.class
        )
        @DiamondConfig(
                groupProvider = ErrorGroupProvider.class
        )
        String prop();
    }

    @ApolloConfig(
            namespaceProvider = NoErrorNamespaceProvider.class,
            propertyNameProvider = NoErrorPropertyNameProvider.class
    )
    @DiamondConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = NoErrorDataIdProvider.class
    )
    interface ProvideError4 {

        @ApolloConfig(
                namespaceProvider = NoErrorNamespaceProvider.class,
                propertyNameProvider = ErrorPropertyNameProvider.class
        )
        @DiamondConfig(
                groupProvider = NoErrorGroupProvider.class,
                dataIdProvider = ErrorDataIdProvider.class
        )
        String prop();
    }

    @ApolloConfig(
            namespaceProvider = NoErrorNamespaceProvider.class,
            propertyNameProvider = NoErrorPropertyNameProvider.class
    )
    @DiamondConfig(
            groupProvider = NoErrorGroupProvider.class,
            dataIdProvider = NoErrorDataIdProvider.class
    )
    interface ProvideError5 {

        @ApolloConfig(
                namespaceProvider = NoErrorNamespaceProvider.class,
                propertyNameProvider = NoErrorPropertyNameProvider.class,
                defaultValueProvider = ErrorApolloDefaultValueProvider.class
        )
        @DiamondConfig(
                groupProvider = NoErrorGroupProvider.class,
                dataIdProvider = NoErrorDataIdProvider.class,
                defaultValueProvider = ErrorDiamondDefaultValueProvider.class
        )
        String prop();
    }

    class ErrorGroupProvider implements GroupProvider {}

    class ErrorDataIdProvider implements DataIdProvider {}

    class ErrorDiamondDefaultValueProvider implements DiamondConfig.DefaultValueProvider {}

    class NoErrorGroupProvider implements GroupProvider {

        @Override
        public String group(Class<?> configClass) {
            return "${group}Group";
        }

        @Override
        public String group(Class<?> configClass, Method method) {
            return "";
        }
    }

    class NoErrorDataIdProvider implements DataIdProvider {

        @Override
        public String dataId(Class<?> configClass) {
            return "Data${data}";
        }

        @Override
        public String dataId(Class<?> configClass, Method method) {
            return "Prop${prop}";
        }
    }

    class ErrorNamespaceProvider implements NamespaceProvider {}

    class ErrorPropertyNameProvider implements PropertyNameProvider {}

    class ErrorApolloDefaultValueProvider implements ApolloConfig.DefaultValueProvider {}

    class NoErrorNamespaceProvider implements NamespaceProvider {

        @Override
        public String namespace(Class<?> configClass) {
            return "${group}Group";
        }

        @Override
        public String namespace(Class<?> configClass, Method method) {
            return "";
        }
    }

    class NoErrorPropertyNameProvider implements PropertyNameProvider {

        @Override
        public String propertyName(Class<?> configClass) {
            return "Data${data}";
        }

        @Override
        public String propertyName(Class<?> configClass, Method method) {
            return "Prop${prop}";
        }
    }
}
