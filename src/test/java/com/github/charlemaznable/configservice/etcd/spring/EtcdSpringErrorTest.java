package com.github.charlemaznable.configservice.etcd.spring;

import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.etcdconf.EtcdConfigService;
import com.github.charlemaznable.etcdconf.MockEtcdServer;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(EtcdSpringErrorConfiguration.class)
public class EtcdSpringErrorTest {

    @Test
    public void testWiredError() {
        MockEtcdServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "name=John\nfull=John Doe\nlong=John Doe Richard");
        await().forever().untilAsserted(() -> assertEquals("name=John\nfull=John Doe\nlong=John Doe Richard",
                EtcdConfigService.getConfig("application").getString("DEFAULT_DATA", "")));

        val testWired = SpringContext.getBean(TestWired.class);
        assertNotNull(testWired);
        assertEquals("John", testWired.name());
        assertEquals("John Doe", testWired.full());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));

        val testWiredConcrete = SpringContext.getBean(TestWiredConcrete.class);
        assertNull(testWiredConcrete);

        val testWiredNone = SpringContext.getBean(TestWiredNone.class);
        assertNull(testWiredNone);
    }
}
