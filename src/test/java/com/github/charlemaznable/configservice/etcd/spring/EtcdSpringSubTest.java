package com.github.charlemaznable.configservice.etcd.spring;

import com.github.charlemaznable.configservice.TestUnWired;
import com.github.charlemaznable.configservice.test.TestWired;
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

@SpringJUnitConfig(EtcdSpringSubConfiguration.class)
public class EtcdSpringSubTest {

    @Test
    public void testWired() {
        MockEtcdServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "name=John\nfull=John Doe\nlong=John Doe Richard");
        MockEtcdServer.addOrModifyProperty("application", "SUB_DATA",
                "name=Joe\nfull=Joe Doe\nlong=Joe Doe Richard");
        await().forever().untilAsserted(() -> assertEquals("name=John\nfull=John Doe\nlong=John Doe Richard",
                EtcdConfigService.getConfig("application").getString("DEFAULT_DATA", "")));
        await().forever().untilAsserted(() -> assertEquals("name=Joe\nfull=Joe Doe\nlong=Joe Doe Richard",
                EtcdConfigService.getConfig("application").getString("SUB_DATA", "")));

        val testWired = SpringContext.getBean(TestWired.class);
        assertNotNull(testWired);
        assertEquals("Joe", testWired.name());
        assertEquals("Joe Doe", testWired.full());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));

        val testWiredSub = SpringContext.getBean(TestWiredSub.class);
        assertNotNull(testWiredSub);
        assertEquals("Joe", testWiredSub.name());
        assertEquals("Joe Doe", testWiredSub.full());
        assertEquals("xyz", testWiredSub.abc("xyz"));
        assertNull(testWiredSub.abc(null));

        val testUnWired = SpringContext.getBean(TestUnWired.class);
        assertNotNull(testUnWired);
        assertEquals("John", testUnWired.name());
        assertEquals("John Doe", testUnWired.full());
        assertEquals("xyz", testUnWired.abc("xyz"));
        assertNull(testUnWired.abc(null));
    }
}
