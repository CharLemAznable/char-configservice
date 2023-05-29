package com.github.charlemaznable.configservice.etcd.guice;

import com.github.charlemaznable.configservice.ConfigModular;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.etcd.EtcdModular;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.core.guice.GuiceFactory;
import com.github.charlemaznable.etcdconf.EtcdConfigService;
import com.github.charlemaznable.etcdconf.MockEtcdServer;
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.util.ClassUtils;

import static java.util.Collections.emptyList;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EtcdGuiceTest {

    private static final String DEFAULT_NAMESPACE = "application";
    private static final String DEFAULT_PROPERTYNAME = "DEFAULT_DATA";
    private static final String SUB_DATA = "SUB_DATA";
    private static final String DEFAULT_CONTENT = "name=John\nfull=John Doe\nlong=John Doe Richard";
    private static final String SUB_CONTENT = "name=Joe\nfull=Joe Doe\nlong=Joe Doe Richard";
    private static final String NAME = "John";
    private static final String FULL = "John Doe";
    private static final String XYZ = "xyz";
    private static final String SUB_NAME = "Joe";
    private static final String SUB_FULL = "Joe Doe";

    @BeforeAll
    public static void beforeAll() {
        MockEtcdServer.setUpMockServer();
        MockEtcdServer.addOrModifyProperty(DEFAULT_NAMESPACE, DEFAULT_PROPERTYNAME, DEFAULT_CONTENT);
        MockEtcdServer.addOrModifyProperty(DEFAULT_NAMESPACE, SUB_DATA, SUB_CONTENT);
        await().forever().untilAsserted(() -> assertEquals(DEFAULT_CONTENT,
                EtcdConfigService.getConfig(DEFAULT_NAMESPACE).getString(DEFAULT_PROPERTYNAME, "")));
        await().forever().untilAsserted(() -> assertEquals(SUB_CONTENT,
                EtcdConfigService.getConfig(DEFAULT_NAMESPACE).getString(SUB_DATA, "")));
    }

    @AfterAll
    public static void afterAll() {
        MockEtcdServer.tearDownMockServer();
    }

    @Test
    public void testWired() {
        Arguments.initial("--ConfigService=etcd");

        val configModular = new ConfigModular()
                .bindClasses(TestWired.class, TestWiredConcrete.class, TestWiredNone.class);
        val injector = Guice.createInjector(configModular.createModule());

        val testWired = injector.getInstance(TestWired.class);
        assertNotNull(testWired);
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));

        val testWiredConcrete = injector.getInstance(TestWiredConcrete.class);
        assertNull(testWiredConcrete);

        val testWiredNone = injector.getInstance(TestWiredNone.class);
        assertNull(testWiredNone);

        Arguments.initial();
    }

    @Test
    public void testWiredError() {
        val etcdModular = new EtcdModular(emptyList()).bindClasses(
                TestWired.class, TestWiredConcrete.class, TestWiredNone.class);
        val injector = Guice.createInjector(etcdModular.createModule());

        val testWired = injector.getInstance(TestWired.class);
        assertNotNull(testWired);
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));

        val testWiredConcrete = injector.getInstance(TestWiredConcrete.class);
        assertNull(testWiredConcrete);

        val testWiredNone = injector.getInstance(TestWiredNone.class);
        assertNull(testWiredNone);
    }

    @Test
    public void testWiredNaked() {
        val etcdModular = new EtcdModular();

        val testWired = etcdModular.getEtcd(TestWired.class);
        assertNotNull(testWired);
        assertEquals(NAME, testWired.name());
        assertEquals(FULL, testWired.full());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));

        assertThrows(ConfigServiceException.class,
                () -> etcdModular.getEtcd(TestWiredConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> etcdModular.getEtcd(TestWiredNone.class));

        val injector = Guice.createInjector(etcdModular.createModule());
        assertThrows(ConfigurationException.class, () ->
                injector.getInstance(TestWired.class));
        assertNull(new GuiceFactory(injector).build(TestWired.class));
    }

    @Test
    public void testWiredSub() {
        val etcdModular = new EtcdModular()
                .scanPackages(ClassUtils.getPackageName(com.github.charlemaznable.configservice.etcd.guice.TestWiredSub.class))
                .scanPackageClasses(com.github.charlemaznable.configservice.etcd.guice.TestWiredSub.class);
        val injector = Guice.createInjector(etcdModular.createModule());

        val testWired = injector.getInstance(TestWired.class);
        assertNotNull(testWired);
        assertEquals(SUB_NAME, testWired.name());
        assertEquals(SUB_FULL, testWired.full());
        assertEquals(XYZ, testWired.abc(XYZ));
        assertNull(testWired.abc(null));

        val testWiredSub = injector.getInstance(TestWiredSub.class);
        assertNotNull(testWiredSub);
        assertEquals(SUB_NAME, testWiredSub.name());
        assertEquals(SUB_FULL, testWiredSub.full());
        assertEquals(XYZ, testWiredSub.abc(XYZ));
        assertNull(testWiredSub.abc(null));
    }
}
