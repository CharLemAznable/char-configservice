package com.github.charlemaznable.configservice.etcd;

import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.etcd.EtcdFactory.EtcdLoader;
import com.github.charlemaznable.configservice.test.common.TestBase;
import com.github.charlemaznable.configservice.test.common.TestBaseConcrete;
import com.github.charlemaznable.configservice.test.common.TestBaseNone;
import com.github.charlemaznable.configservice.test.common.TestCache;
import com.github.charlemaznable.configservice.test.common.TestCacheNone;
import com.github.charlemaznable.configservice.test.common.TestConfigArg;
import com.github.charlemaznable.configservice.test.common.TestDefData;
import com.github.charlemaznable.configservice.test.common.TestDefault;
import com.github.charlemaznable.configservice.test.common.TestDefaultData;
import com.github.charlemaznable.configservice.test.common.TestDefaultDataEmpty;
import com.github.charlemaznable.configservice.test.common.TestGetterDefault;
import com.github.charlemaznable.configservice.test.common.TestListener;
import com.github.charlemaznable.configservice.test.common.TestListenerRegister;
import com.github.charlemaznable.configservice.test.common.TestParseData;
import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.etcdconf.EtcdConfigChangeListener;
import com.github.charlemaznable.etcdconf.EtcdConfigService;
import com.github.charlemaznable.etcdconf.MockEtcdServer;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.configservice.ConfigFactory.getConfig;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Await.awaitForMicros;
import static com.github.charlemaznable.core.lang.Await.awaitForMillis;
import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EtcdFactoryTest implements EtcdConfigChangeListener {

    private static final EtcdLoader etcdLoader = EtcdFactory.etcdLoader(reflectFactory());
    private static boolean changed = false;

    @BeforeAll
    public static void beforeAll() {
        MockEtcdServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockEtcdServer.tearDownMockServer();
    }

    @Override
    public void onChange(ChangeEvent changeEvent) {
        changed = true;
    }

    @Test
    public void testBase() {
        EtcdConfigService.getConfig("application").addChangeListener("base.data", this);
        EtcdConfigService.getConfig("base.group").addChangeListener("base.data", this);

        changed = false;
        MockEtcdServer.addOrModifyProperty("application", "base.data", "abc");
        await().forever().until(() -> changed);
        changed = false;
        MockEtcdServer.addOrModifyProperty("base.group", "base.data", "xyz");
        await().forever().until(() -> changed);

        FactoryContext.set(reflectFactory());
        Arguments.initial("--ConfigService=etcd");

        val testBase = getConfig(TestBase.class);
        assertEquals("abc", testBase.abc());
        assertEquals("xyz", testBase.xyz());

        val testBaseGetter = (ConfigGetter) testBase;
        assertEquals(0, testBaseGetter.getInt("base.data"));
        assertEquals(0, testBaseGetter.getLong("base.data"));
        assertEquals(0, testBaseGetter.getShort("base.data"));
        assertEquals(0, testBaseGetter.getFloat("base.data"));
        assertEquals(0, testBaseGetter.getDouble("base.data"));
        assertEquals(0, testBaseGetter.getByte("base.data"));
        assertFalse(testBaseGetter.getBoolean("base.data"));
        assertEquals(0, testBaseGetter.getDuration("base.data"));

        assertEquals("ConfigService:" + TestBase.class.getSimpleName() + "@"
                + Integer.toHexString(testBase.hashCode()), testBase.toString());
        assertEquals(testBase, testBase);

        assertThrows(ConfigServiceException.class,
                () -> getConfig(TestBaseConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> getConfig(TestBaseNone.class));

        Arguments.initial();
        FactoryContext.unload();

        EtcdConfigService.getConfig("application").removeChangeListener(this);
        EtcdConfigService.getConfig("base.group").removeChangeListener(this);
    }

    @Test
    public void testCache() {
        EtcdConfigService.getConfig("CACHE_GROUP").addChangeListener("CACHE_KEY", this);

        changed = false;
        MockEtcdServer.addOrModifyProperty("CACHE_GROUP", "CACHE_KEY", "key1=value1");
        await().forever().until(() -> changed);

        val testCacheNone = etcdLoader.getEtcd(TestCacheNone.class);
        val testCache = etcdLoader.getEtcd(TestCache.class);
        assertEquals("value1", testCacheNone.key1());
        assertEquals("value1", testCache.key1());
        assertNull(testCacheNone.key2());
        assertNull(testCache.key2());

        changed = false;
        MockEtcdServer.addOrModifyProperty("CACHE_GROUP", "CACHE_KEY", "key2=value2");
        await().forever().until(() -> changed);
        awaitForMillis(100);
        assertNull(testCacheNone.key1());
        assertEquals("value1", testCache.key1());
        assertEquals("value2", testCacheNone.key2());
        assertNull(testCache.key2());

        awaitForSeconds(2);
        assertNull(testCacheNone.key1());
        assertNull(testCache.key1());
        assertEquals("value2", testCacheNone.key2());
        assertEquals("value2", testCache.key2());

        EtcdConfigService.getConfig("CACHE_GROUP").removeChangeListener(this);
    }

    @SneakyThrows
    @Test
    public void testApollo() {
        EtcdConfigService.getConfig("application").addChangeListener("DEFAULT_DATA", this);

        changed = false;
        // no substitute
        MockEtcdServer.addOrModifyProperty("application", "DEFAULT_DATA", """
                name=John
                full=${name} Doe
                long=${full} Richard
                testMode=yes
                testMode2=TRUE
                content=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${long})
                list=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${name}) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${full}) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${long})""");
        await().forever().until(() -> changed);

        val testDefault = etcdLoader.getEtcd(TestDefault.class);
        assertNotNull(testDefault);

        assertEquals("John", testDefault.name());
        assertEquals("John Doe", testDefault.full());
        assertEquals("John Doe Richard", testDefault.longName());

        assertEquals(3, testDefault.longSplit().size());
        assertEquals("John", testDefault.longSplit().get(0));
        assertEquals("Doe", testDefault.longSplit().get(1));
        assertEquals("Richard", testDefault.longSplit().get(2));

        assertEquals("xyz", testDefault.abc("xyz"));
        assertEquals("abc", testDefault.abc(null));

        assertNull(testDefault.xyzNull());
        assertEquals("", testDefault.xyzEmpty());
        assertEquals("", testDefault.xyzEmpty2());

        assertEquals(3, testDefault.count(3));
        assertEquals(0, testDefault.count(null));
        assertEquals(1, testDefault.count1());

        assertTrue(testDefault.testMode());
        assertEquals(Boolean.TRUE, testDefault.testMode2());

        assertEquals("John Doe Richard", testDefault.content().getName());

        assertEquals(3, testDefault.list().size());
        assertEquals("John", testDefault.list().get(0).getName());
        assertEquals("John Doe", testDefault.list().get(1).getName());
        assertEquals("John Doe Richard", testDefault.list().get(2).getName());

        val ConfigGetterDefault = (ConfigGetter) testDefault;
        assertNotNull(ConfigGetterDefault);
        assertEquals("John", ConfigGetterDefault.getString("name"));
        assertEquals("John Doe", ConfigGetterDefault.getString("full"));
        assertEquals("John Doe Richard", ConfigGetterDefault.getString("long"));

        assertEquals(10, testDefault.shortValue());
        assertEquals(200, testDefault.intValue());
        assertEquals(3000L, testDefault.longValue());
        assertEquals(40000F, testDefault.floatValue());
        assertEquals(5D, testDefault.doubleValue());
        assertEquals('a', testDefault.byteValue());
        assertEquals('a', testDefault.charValue());

        assertEquals(0, testDefault.shortValueDefault());
        assertEquals(0, testDefault.intValueDefault());
        assertEquals(0, testDefault.longValueDefault());
        assertEquals(0, testDefault.floatValueDefault());
        assertEquals(0, testDefault.doubleValueDefault());
        assertEquals(0, testDefault.byteValueDefault());
        assertEquals('\0', testDefault.charValueDefault());

        val testDefaultData = etcdLoader.getEtcd(TestDefaultData.class);
        val properties = testDefaultData.properties();

        assertEquals("John", properties.getProperty("name"));
        assertEquals("John Doe", properties.getProperty("full"));
        assertEquals("John Doe Richard", properties.getProperty("long"));

        assertEquals("yes", properties.getProperty("testMode"));
        assertEquals("TRUE", properties.getProperty("testMode2"));

        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe Richard)",
                properties.getProperty("content"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe Richard)",
                properties.getProperty("list"));

        assertNotEquals(testDefault.hashCode(), testDefaultData.hashCode());
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(testDefault, testDefaultData);

        val testDefaultDataEmpty = etcdLoader.getEtcd(TestDefaultDataEmpty.class);
        assertNull(testDefaultDataEmpty.propertiesNull());
        assertTrue(testDefaultDataEmpty.propertiesEmpty().isEmpty());

        EtcdConfigService.getConfig("application").removeChangeListener(this);
    }

    @Test
    public void testConfigGetter() {
        EtcdConfigService.getConfig("application").addChangeListener("DEFAULT_DATA", this);

        changed = false;
        MockEtcdServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "name=John\nfull=${name} Doe\nlong=${full} Richard");
        await().forever().until(() -> changed);

        val testGetterDefault = etcdLoader.getEtcd(TestGetterDefault.class);
        assertNotNull(testGetterDefault);
        assertEquals("John", testGetterDefault.getString("name"));
        assertEquals("John Doe", testGetterDefault.getString("full"));
        assertEquals("John Doe Richard", testGetterDefault.getString("long"));
        assertEquals(0, testGetterDefault.getInt("name"));
        assertEquals(0, testGetterDefault.getLong("name"));
        assertEquals(0, testGetterDefault.getShort("name"));
        assertEquals(0, testGetterDefault.getFloat("name"));
        assertEquals(0, testGetterDefault.getDouble("name"));
        assertEquals(0, testGetterDefault.getByte("name"));
        assertFalse(testGetterDefault.getBoolean("name"));
        assertEquals(0, testGetterDefault.getDuration("name"));

        awaitForMillis(100);

        changed = false;
        MockEtcdServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "# toml\nname=John\nfull=${name} Doe\nlong=${full} Richard");
        await().forever().until(() -> changed);

        val testGetterDefaultError = etcdLoader.getEtcd(TestGetterDefault.class);
        assertNull(testGetterDefaultError.getString("name"));
        assertNull(testGetterDefaultError.getString("full"));
        assertNull(testGetterDefaultError.getString("long"));

        awaitForMillis(100);

        changed = false;
        MockEtcdServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "# TOML\nname='John'\nfull='John Doe'\nlong='John Doe Richard'");
        await().forever().until(() -> changed);

        val testGetterDefaultToml = etcdLoader.getEtcd(TestGetterDefault.class);
        assertEquals("John", testGetterDefaultToml.getString("name"));
        assertEquals("John Doe", testGetterDefaultToml.getString("full"));
        assertEquals("John Doe Richard", testGetterDefaultToml.getString("long"));

        EtcdConfigService.getConfig("application").removeChangeListener(this);
    }

    @Test
    public void testConfigGetterDefault() {
        EtcdConfigService.getConfig("DEF_GROUP").addChangeListener("DEF_DATA", this);

        changed = false;
        // no substitute
        MockEtcdServer.addOrModifyProperty("DEF_GROUP", "DEF_DATA", """
                name=John
                full=${name} Doe
                long=${full} Richard
                testMode=yes
                testMode2=TRUE
                content=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${long})
                list=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${name}) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${full}) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${long})""");
        await().forever().until(() -> changed);

        val testDefData = etcdLoader.getEtcd(TestDefData.class);

        val properties = testDefData.properties();
        assertEquals("John", properties.getProperty("name"));
        assertEquals("John Doe", properties.getProperty("full"));
        assertEquals("John Doe Richard", properties.getProperty("long"));
        assertEquals("yes", properties.getProperty("testMode"));
        assertEquals("TRUE", properties.getProperty("testMode2"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe Richard)",
                properties.getProperty("content"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe Richard)",
                properties.getProperty("list"));

        val map = testDefData.map();
        assertEquals("John", map.get("name"));
        assertEquals("John Doe", map.get("full"));
        assertEquals("John Doe Richard", map.get("long"));
        assertEquals("yes", map.get("testMode"));
        assertEquals("TRUE", map.get("testMode2"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe Richard)",
                map.get("content"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe) @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(John Doe Richard)",
                map.get("list"));

        awaitForMicros(TimeUnit.MILLISECONDS.toMicros(100));

        changed = false;
        MockEtcdServer.addOrModifyProperty("DEF_GROUP", "DEF_DATA", """
                # Toml
                name='John'
                full='John Doe'
                long='John Doe Richard'
                testMode='yes'
                testMode2='TRUE'
                content='@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean("John Doe Richard")'
                list='@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean("John") @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean("John Doe") @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean("John Doe Richard")'""");
        await().forever().until(() -> changed);

        val testDefDataToml = etcdLoader.getEtcd(TestDefData.class);

        val propertiesToml = testDefDataToml.properties();
        assertEquals("John", propertiesToml.getProperty("name"));
        assertEquals("John Doe", propertiesToml.getProperty("full"));
        assertEquals("John Doe Richard", propertiesToml.getProperty("long"));
        assertEquals("yes", propertiesToml.getProperty("testMode"));
        assertEquals("TRUE", propertiesToml.getProperty("testMode2"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe Richard\")",
                propertiesToml.getProperty("content"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John\") @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe\") @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe Richard\")",
                propertiesToml.getProperty("list"));

        val mapToml = testDefDataToml.map();
        assertEquals("John", mapToml.get("name"));
        assertEquals("John Doe", mapToml.get("full"));
        assertEquals("John Doe Richard", mapToml.get("long"));
        assertEquals("yes", mapToml.get("testMode"));
        assertEquals("TRUE", mapToml.get("testMode2"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe Richard\")",
                mapToml.get("content"));
        assertEquals("@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John\") @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe\") @com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe Richard\")",
                mapToml.get("list"));

        EtcdConfigService.getConfig("DEF_GROUP").removeChangeListener(this);
    }

    @Test
    public void testParseData() {
        EtcdConfigService.getConfig("PARSE_GROUP").addChangeListener("PARSE_DATA", this);

        changed = false;
        MockEtcdServer.addOrModifyProperty("PARSE_GROUP", "PARSE_DATA",
                "workerPoolSize=30\nuris=http://a.b.c:9200");
        await().forever().until(() -> changed);

        val testParseData = etcdLoader.getEtcd(TestParseData.class);

        val properties = testParseData.properties();
        assertEquals("30", properties.getProperty("workerPoolSize"));
        assertEquals("http://a.b.c:9200", properties.getProperty("uris"));

        val vertxOptions = testParseData.vertxOptions();
        assertEquals(30, vertxOptions.getWorkerPoolSize());

        val esConfig = testParseData.esConfig();
        assertEquals("http://a.b.c:9200", esConfig.getUris().get(0));

        val parseVertxOptions = testParseData.parseRawToVertxOptions();
        assertEquals(30, parseVertxOptions.getWorkerPoolSize());

        val parseEsConfig = testParseData.parseRawToEsConfig();
        assertEquals("http://a.b.c:9200", parseEsConfig.getUris().get(0));

        EtcdConfigService.getConfig("PARSE_GROUP").removeChangeListener(this);
    }

    @Test
    public void testConfigArg() {
        EtcdConfigService.getConfig("Arg").addChangeListener("data", this);

        changed = false;
        MockEtcdServer.addOrModifyProperty("Arg", "data",
                "custom1.key1=value1\ncustom1.key2=value2\ncustom2.key1=value2\ncustom2.key2=value1\n");
        await().forever().until(() -> changed);

        val testConfigArg = etcdLoader.getEtcd(TestConfigArg.class);
        assertNull(testConfigArg.custom1());
        assertNull(testConfigArg.custom2());

        Arguments.initial("--customKey1=key1", "--customKey2=key2");
        awaitForSeconds(1);
        assertEquals("value1", testConfigArg.custom1());
        assertEquals("value1", testConfigArg.custom2());

        Arguments.initial("--customKey1=key2", "--customKey2=key1");
        awaitForSeconds(1);
        assertEquals("value2", testConfigArg.custom1());
        assertEquals("value2", testConfigArg.custom2());

        Arguments.initial();
        EtcdConfigService.getConfig("Arg").removeChangeListener(this);
    }

    @Test
    public void testConfigListener() {
        EtcdConfigService.getConfig("Listener").addChangeListener("data", this);

        changed = false;
        MockEtcdServer.addOrModifyProperty("Listener", "data", "value1");
        await().forever().until(() -> changed);

        val testListenerRegister = etcdLoader.getEtcd(TestListenerRegister.class);
        val testListener1 = new TestListener();
        val testListener2 = new TestListener();

        testListenerRegister.addConfigListener(testListener1);
        testListenerRegister.addConfigListener(testListener2);
        awaitForSeconds(1);

        MockEtcdServer.addOrModifyProperty("Listener", "data", "value2");
        await().forever().until(testListener1::isChanged);
        assertEquals("Listener", testListener1.getKeyset());
        assertEquals("data", testListener1.getKey());
        assertEquals("value2", testListener1.getValue());
        await().forever().until(testListener2::isChanged);
        assertEquals("Listener", testListener2.getKeyset());
        assertEquals("data", testListener2.getKey());
        assertEquals("value2", testListener2.getValue());

        testListener1.reset();
        testListener2.reset();
        testListenerRegister.removeConfigListener(testListener2);
        awaitForSeconds(1);

        MockEtcdServer.addOrModifyProperty("Listener", "data", "value3");
        await().forever().until(testListener1::isChanged);
        assertEquals("Listener", testListener1.getKeyset());
        assertEquals("data", testListener1.getKey());
        assertEquals("value3", testListener1.getValue());
        assertFalse(testListener2.isChanged());
        assertEquals("Listener", testListener2.getKeyset());
        assertEquals("data", testListener2.getKey());
        assertEquals("value2", testListener2.getValue());

        testListener1.reset();
        testListenerRegister.addConfigListener("data2", testListener1);
        awaitForSeconds(1);

        MockEtcdServer.addOrModifyProperty("Listener", "data2", "value4");
        await().forever().until(testListener1::isChanged);
        assertEquals("Listener", testListener1.getKeyset());
        assertEquals("data2", testListener1.getKey());
        assertEquals("value4", testListener1.getValue());

        testListenerRegister.addConfigListener("", testListener1);
        testListenerRegister.removeConfigListener("", testListener1);

        EtcdConfigService.getConfig("Listener").removeChangeListener(this);
    }
}
