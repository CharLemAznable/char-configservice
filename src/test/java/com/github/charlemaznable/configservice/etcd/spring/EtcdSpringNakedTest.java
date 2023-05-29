package com.github.charlemaznable.configservice.etcd.spring;

import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.etcdconf.EtcdConfigService;
import com.github.charlemaznable.etcdconf.MockEtcdServer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.configservice.etcd.EtcdFactory.getEtcd;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(EtcdSpringNakedConfiguration.class)
public class EtcdSpringNakedTest {

    @Test
    public void testWiredNaked() {
        MockEtcdServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "name=John\nfull=John Doe\nlong=John Doe Richard");
        await().forever().untilAsserted(() -> assertEquals("name=John\nfull=John Doe\nlong=John Doe Richard",
                EtcdConfigService.getConfig("application").getString("DEFAULT_DATA", "")));

        val testWired = getEtcd(TestWired.class);
        assertNotNull(testWired);
        assertEquals("John", testWired.name());
        assertEquals("John Doe", testWired.full());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));

        assertThrows(ConfigServiceException.class,
                () -> getEtcd(TestWiredConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> getEtcd(TestWiredNone.class));

        ApplicationContext applicationContext = onClass(SpringContext.class)
                .field("applicationContext").get();
        assertThrows(NoSuchBeanDefinitionException.class, () ->
                applicationContext.getBean(TestWired.class));
        assertNull(SpringContext.getBeanOrCreate(TestWired.class));
    }
}
