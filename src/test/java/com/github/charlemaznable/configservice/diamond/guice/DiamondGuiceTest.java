package com.github.charlemaznable.configservice.diamond.guice;

import com.github.charlemaznable.configservice.diamond.DiamondModular;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredDataId;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.util.Providers;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DiamondGuiceTest {

    private static final String DEFAULT_GROUP = "DEFAULT_GROUP";
    private static final String DEFAULT_DATA = "DEFAULT_DATA";
    private static final String SUB_DATA = "SUB_DATA";
    private static final String DEFAULT_CONTENT = "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard";
    private static final String SUB_CONTENT = "name=Joe\nfull=${this.name} Doe\nlong=${this.full} Richard";
    private static final String NAME = "John";
    private static final String FULL = "John Doe";
    private static final String LONG = "John Doe Richard";
    private static final String XYZ = "xyz";
    private static final String SUB_NAME = "Joe";
    private static final String SUB_FULL = "Joe Doe";
    private static final String SUB_LONG = "Joe Doe Richard";
    private static final String GUICE_CONTEXT = "guiceguice&springspring&guiceguice";

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
        MockDiamondServer.setConfigInfo(DEFAULT_GROUP, DEFAULT_DATA, DEFAULT_CONTENT);
        MockDiamondServer.setConfigInfo(DEFAULT_GROUP, SUB_DATA, SUB_CONTENT);
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testWired() {
        val diamondModular = new DiamondModular(new AbstractModule() {
            @Override
            public void configure() {
                bind(TestWiredDataId.class).toProvider(Providers.of(new TestWiredDataId() {
                    @Override
                    public String dataId(Class<?> configClass, Method method) {
                        return "long";
                    }
                }));
            }
        }).bindClasses(TestWired.class, TestWiredConcrete.class, TestWiredNone.class);
        val injector = Guice.createInjector(diamondModular.createModule());

        val testWired = injector.getInstance(TestWired.class);
        assertNotNull(testWired);
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertEquals(LONG, testWired.longName());
        assertEquals(LONG, testWired.longWrap());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));
        assertEquals(GUICE_CONTEXT, testWired.defaultInContext());
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertEquals(LONG, testWired.longName());
        assertEquals(LONG, testWired.longWrap());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));
        assertEquals(GUICE_CONTEXT, testWired.defaultInContext());

        val testWiredConcrete = injector.getInstance(TestWiredConcrete.class);
        assertNull(testWiredConcrete);

        val testWiredNone = injector.getInstance(TestWiredNone.class);
        assertNull(testWiredNone);
    }

    @Test
    public void testWiredError() {
        val diamondModular = new DiamondModular(emptyList()).bindClasses(
                TestWired.class, TestWiredConcrete.class, TestWiredNone.class);
        val injector = Guice.createInjector(diamondModular.createModule());

        val testWired = injector.getInstance(TestWired.class);
        assertNotNull(testWired);
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertThrows(NullPointerException.class, testWired::longName);
        assertNull(testWired.longWrap());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));

        val testWiredConcrete = injector.getInstance(TestWiredConcrete.class);
        assertNull(testWiredConcrete);

        val testWiredNone = injector.getInstance(TestWiredNone.class);
        assertNull(testWiredNone);
    }

    @Test
    public void testWiredNaked() {
        val diamondModular = new DiamondModular();

        val testWired = diamondModular.getDiamond(TestWired.class);
        assertNotNull(testWired);
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertThrows(NullPointerException.class, testWired::longName);
        assertNull(testWired.longWrap());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));

        assertThrows(ConfigServiceException.class,
                () -> diamondModular.getDiamond(TestWiredConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> diamondModular.getDiamond(TestWiredNone.class));

        val injector = Guice.createInjector(diamondModular.createModule());
        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestWired.class));
        assertNull(new GuiceFactory(injector).build(TestWired.class));
    }

    @Test
    public void testWiredSub() {
        val diamondModular = new DiamondModular(new AbstractModule() {
            @Override
            public void configure() {
                bind(TestWiredDataId.class).toProvider(Providers.of(new TestWiredDataId() {
                    @Override
                    public String dataId(Class<?> configClass, Method method) {
                        return "long";
                    }
                }));
            }
        }).scanPackages(ClassUtils.getPackageName(TestWiredSub.class)).scanPackageClasses(TestWiredSub.class);
        val injector = Guice.createInjector(diamondModular.createModule());

        val testWired = injector.getInstance(TestWired.class);
        assertNotNull(testWired);
        assertEquals(SUB_NAME, testWired.name());
        assertEquals(SUB_FULL, testWired.full());
        assertEquals(SUB_LONG, testWired.longName());
        assertEquals(SUB_LONG, testWired.longWrap());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));
        assertEquals(GUICE_CONTEXT, testWired.defaultInContext());

        val testWiredSub = injector.getInstance(TestWiredSub.class);
        assertNotNull(testWiredSub);
        assertEquals(SUB_NAME, testWiredSub.name());
        assertEquals(SUB_FULL, testWiredSub.full());
        assertEquals(SUB_LONG, testWiredSub.longName());
        assertEquals(SUB_LONG, testWiredSub.longWrap());
        assertEquals(XYZ, testWiredSub.abc(XYZ));
        assertNull(testWiredSub.abc(null));
        assertEquals(GUICE_CONTEXT, testWiredSub.defaultInContext());
    }
}
