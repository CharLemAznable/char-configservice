package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.apollo.ApolloConfig.PropertyNameProvider;
import com.google.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestWiredPropertyNameWrapper implements PropertyNameProvider {

    private final TestWiredPropertyName testWiredPropertyName;

    public TestWiredPropertyNameWrapper() {
        this(null);
    }

    @Inject
    @Autowired
    public TestWiredPropertyNameWrapper(@Nullable TestWiredPropertyName testWiredPropertyName) {
        this.testWiredPropertyName = nullThen(testWiredPropertyName, () -> new TestWiredPropertyName() {
            @Override
            public String propertyName(Class<?> configClass, Method method) {
                return "error";
            }
        });
    }

    @Override
    public String propertyName(Class<?> configClass, Method method) {
        return testWiredPropertyName.propertyName(configClass, method);
    }
}
