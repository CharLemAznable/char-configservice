package com.github.charlemaznable.configservice.test;

import com.github.charlemaznable.configservice.diamond.DiamondConfig.DataIdProvider;
import com.google.inject.Inject;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

import static com.github.charlemaznable.core.lang.Condition.nullThen;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
public class TestWiredDataIdWrapper implements DataIdProvider {

    private final TestWiredDataId testWiredDataId;

    public TestWiredDataIdWrapper() {
        this(null);
    }

    @Inject
    @Autowired
    public TestWiredDataIdWrapper(@Nullable TestWiredDataId testWiredDataId) {
        this.testWiredDataId = nullThen(testWiredDataId, () -> new TestWiredDataId() {
            @Override
            public String dataId(Class<?> configClass, Method method) {
                return "error";
            }
        });
    }

    @Override
    public String dataId(Class<?> configClass, Method method) {
        return testWiredDataId.dataId(configClass, method);
    }
}
