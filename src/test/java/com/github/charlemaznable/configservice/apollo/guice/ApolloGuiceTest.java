package com.github.charlemaznable.configservice.apollo.guice;

import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.configservice.ConfigModular;
import com.github.charlemaznable.configservice.apollo.ApolloModular;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.configservice.test.TestWiredPropertyName;
import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.google.inject.AbstractModule;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.util.Providers;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

import static java.util.Collections.emptyList;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ApolloGuiceTest {

    private static final String DEFAULT_NAMESPACE = "application";
    private static final String DEFAULT_PROPERTYNAME = "DEFAULT_DATA";
    private static final String SUB_DATA = "SUB_DATA";
    private static final String DEFAULT_CONTENT = "name=John\nfull=John Doe\nlong=John Doe Richard";
    private static final String SUB_CONTENT = "name=Joe\nfull=Joe Doe\nlong=Joe Doe Richard";
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
        MockApolloServer.setUpMockServer();
        MockApolloServer.addOrModifyProperty(DEFAULT_NAMESPACE, DEFAULT_PROPERTYNAME, DEFAULT_CONTENT);
        MockApolloServer.addOrModifyProperty(DEFAULT_NAMESPACE, SUB_DATA, SUB_CONTENT);
        await().forever().untilAsserted(() -> assertEquals(DEFAULT_CONTENT,
                ConfigService.getConfig(DEFAULT_NAMESPACE).getProperty(DEFAULT_PROPERTYNAME, "")));
        await().forever().untilAsserted(() -> assertEquals(SUB_CONTENT,
                ConfigService.getConfig(DEFAULT_NAMESPACE).getProperty(SUB_DATA, "")));
    }

    @AfterAll
    public static void afterAll() {
        MockApolloServer.tearDownMockServer();
    }

    @Test
    public void testWired() {
        Arguments.initial("--ConfigService=apollo");

        val configModular = new ConfigModular(new AbstractModule() {
            @Override
            public void configure() {
                bind(TestWiredPropertyName.class).toProvider(Providers.of(new TestWiredPropertyName() {
                    @Override
                    public String propertyName(Class<?> configClass, Method method) {
                        return "long";
                    }
                }));
            }
        }).bindClasses(TestWired.class, TestWiredConcrete.class, TestWiredNone.class);
        val injector = Guice.createInjector(configModular.createModule());

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

        Arguments.initial();
    }

    @Test
    public void testWiredError() {
        val apolloModular = new ApolloModular(emptyList()).bindClasses(
                TestWired.class, TestWiredConcrete.class, TestWiredNone.class);
        val injector = Guice.createInjector(apolloModular.createModule());

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
        val apolloModular = new ApolloModular();

        val testWired = apolloModular.getApollo(TestWired.class);
        assertNotNull(testWired);
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertThrows(NullPointerException.class, testWired::longName);
        assertNull(testWired.longWrap());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));

        assertThrows(ConfigServiceException.class,
                () -> apolloModular.getApollo(TestWiredConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> apolloModular.getApollo(TestWiredNone.class));

        val injector = Guice.createInjector(apolloModular.createModule());
        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestWired.class));
        assertNull(new GuiceFactory(injector).build(TestWired.class));
    }

    @Test
    public void testWiredSub() {
        val apolloModular = new ApolloModular(new AbstractModule() {
            @Override
            public void configure() {
                bind(TestWiredPropertyName.class).toProvider(Providers.of(new TestWiredPropertyName() {
                    @Override
                    public String propertyName(Class<?> configClass, Method method) {
                        return "long";
                    }
                }));
            }
        }).scanPackages(ClassUtils.getPackageName(TestWiredSub.class)).scanPackageClasses(TestWiredSub.class);
        val injector = Guice.createInjector(apolloModular.createModule());

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
