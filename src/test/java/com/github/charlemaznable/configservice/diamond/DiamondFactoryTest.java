package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.diamond.DiamondFactory.DiamondLoader;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.test.common.TestBase;
import com.github.charlemaznable.configservice.test.common.TestBaseConcrete;
import com.github.charlemaznable.configservice.test.common.TestBaseNone;
import com.github.charlemaznable.configservice.test.common.TestCache;
import com.github.charlemaznable.configservice.test.common.TestCacheNone;
import com.github.charlemaznable.configservice.test.common.TestConfigArg;
import com.github.charlemaznable.configservice.test.common.TestConfigProps;
import com.github.charlemaznable.configservice.test.common.TestDefData;
import com.github.charlemaznable.configservice.test.common.TestDefault;
import com.github.charlemaznable.configservice.test.common.TestDefaultData;
import com.github.charlemaznable.configservice.test.common.TestDefaultDataEmpty;
import com.github.charlemaznable.configservice.test.common.TestError.ProvideError1;
import com.github.charlemaznable.configservice.test.common.TestError.ProvideError2;
import com.github.charlemaznable.configservice.test.common.TestError.ProvideError3;
import com.github.charlemaznable.configservice.test.common.TestError.ProvideError4;
import com.github.charlemaznable.configservice.test.common.TestError.ProvideError5;
import com.github.charlemaznable.configservice.test.common.TestGetterDefault;
import com.github.charlemaznable.configservice.test.common.TestParseData;
import com.github.charlemaznable.core.config.Arguments;
import com.github.charlemaznable.core.context.FactoryContext;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;

import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.configservice.ConfigFactory.getConfig;
import static com.github.charlemaznable.core.context.FactoryContext.ReflectFactory.reflectFactory;
import static com.github.charlemaznable.core.lang.Await.awaitForMicros;
import static com.github.charlemaznable.core.lang.Await.awaitForMillis;
import static com.github.charlemaznable.core.lang.Await.awaitForSeconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DiamondFactoryTest {

    private static final DiamondLoader diamondLoader = DiamondFactory.diamondLoader(reflectFactory());

    @BeforeAll
    public static void beforeAll() {
        MockDiamondServer.setUpMockServer();
    }

    @AfterAll
    public static void afterAll() {
        MockDiamondServer.tearDownMockServer();
    }

    @Test
    public void testBase() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "base.data", "abc");
        MockDiamondServer.setConfigInfo("base.group", "base.data", "xyz");

        FactoryContext.set(reflectFactory());
        Arguments.initial("--ConfigService=diamond");

        val testBase = getConfig(TestBase.class);
        assertEquals("abc", testBase.abc());
        assertEquals("xyz", testBase.xyz());

        assertEquals("ConfigService:" + TestBase.class.getSimpleName() + "@"
                + Integer.toHexString(testBase.hashCode()), testBase.toString());
        assertEquals(testBase, testBase);

        assertThrows(ConfigServiceException.class,
                () -> getConfig(TestBaseConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> getConfig(TestBaseNone.class));

        Arguments.initial();
        FactoryContext.unload();
    }

    @Test
    public void testCache() {
        MockDiamondServer.setConfigInfo("CACHE_GROUP", "CACHE_KEY", "key1=value1");

        val testCacheNone = diamondLoader.getDiamond(TestCacheNone.class);
        val testCache = diamondLoader.getDiamond(TestCache.class);
        assertEquals("value1", testCacheNone.key1());
        assertEquals("value1", testCache.key1());
        assertNull(testCacheNone.key2());
        assertNull(testCache.key2());

        MockDiamondServer.setConfigInfo("CACHE_GROUP", "CACHE_KEY", "key2=value2");
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
    }

    @SneakyThrows
    @Test
    public void testDiamond() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA", "" +
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard\n" +
                "testMode=yes\ntestMode2=TRUE\n" +
                "content=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.long})\n" +
                "list=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.name}) " +
                "@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.full}) " +
                "@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.long})");

        val testDefault = diamondLoader.getDiamond(TestDefault.class);
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

        val testDefaultData = diamondLoader.getDiamond(TestDefaultData.class);
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

        val testDefaultDataEmpty = diamondLoader.getDiamond(TestDefaultDataEmpty.class);
        assertNull(testDefaultDataEmpty.propertiesNull());
        assertTrue(testDefaultDataEmpty.propertiesEmpty().isEmpty());
    }

    @Test
    public void testConfigGetter() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val testGetterDefault = diamondLoader.getDiamond(TestGetterDefault.class);
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

        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "# toml\nname=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val testGetterDefaultError = diamondLoader.getDiamond(TestGetterDefault.class);
        assertNull(testGetterDefaultError.getString("name"));
        assertNull(testGetterDefaultError.getString("full"));
        assertNull(testGetterDefaultError.getString("long"));

        awaitForMillis(100);

        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "# TOML\nname='John'\nfull='John Doe'\nlong='John Doe Richard'");

        val testGetterDefaultToml = diamondLoader.getDiamond(TestGetterDefault.class);
        assertEquals("John", testGetterDefaultToml.getString("name"));
        assertEquals("John Doe", testGetterDefaultToml.getString("full"));
        assertEquals("John Doe Richard", testGetterDefaultToml.getString("long"));
    }

    @Test
    public void testConfigGetterDefault() {
        MockDiamondServer.setConfigInfo("DEF_GROUP", "DEF_DATA", "" +
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard\n" +
                "testMode=yes\ntestMode2=TRUE\n" +
                "content=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.long})\n" +
                "list=@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.name}) " +
                "@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.full}) " +
                "@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(${this.long})");

        val testDefData = diamondLoader.getDiamond(TestDefData.class);

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

        MockDiamondServer.setConfigInfo("DEF_GROUP", "DEF_DATA", "# Toml\n" +
                "name='John'\nfull='John Doe'\nlong='John Doe Richard'\n" +
                "testMode='yes'\ntestMode2='TRUE'\n" +
                "content='@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe Richard\")'\n" +
                "list='@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John\") " +
                "@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe\") " +
                "@com.github.charlemaznable.configservice.test.common.TestDefaultContentBean(\"John Doe Richard\")'");

        val testDefDataToml = diamondLoader.getDiamond(TestDefData.class);

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
    }

    @Test
    public void testParseData() {
        MockDiamondServer.setConfigInfo("PARSE_GROUP", "PARSE_DATA", "workerPoolSize=30\nuris=http://a.b.c:9200");

        val testParseData = diamondLoader.getDiamond(TestParseData.class);

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
    }

    @Test
    public void testConfigProps() {
        MockDiamondServer.setConfigInfo("GROUPGroup", "DataDATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val stoneProps = diamondLoader.getDiamond(TestConfigProps.class);
        assertNotNull(stoneProps);
        assertEquals("John", stoneProps.name());
        assertEquals("John Doe", stoneProps.full());
        assertEquals("John Doe Richard", stoneProps.longName());
        assertEquals("DEFAULTDefault", stoneProps.prop());

        val error1 = diamondLoader.getDiamond(ProvideError1.class);
        assertThrows(ConfigServiceException.class, error1::prop);
        val error2 = diamondLoader.getDiamond(ProvideError2.class);
        assertThrows(ConfigServiceException.class, error2::prop);
        val error3 = diamondLoader.getDiamond(ProvideError3.class);
        assertThrows(ConfigServiceException.class, error3::prop);
        val error4 = diamondLoader.getDiamond(ProvideError4.class);
        assertThrows(ConfigServiceException.class, error4::prop);
        val error5 = diamondLoader.getDiamond(ProvideError5.class);
        assertThrows(ConfigServiceException.class, error5::prop);
    }

    @Test
    public void testConfigArg() {
        MockDiamondServer.setConfigInfo("Arg", "data",
                "custom1.key1=value1\ncustom1.key2=value2\ncustom2.key1=value2\ncustom2.key2=value1\n");

        val testConfigArg = diamondLoader.getDiamond(TestConfigArg.class);
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
    }
}
