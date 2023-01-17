package com.github.charlemaznable.configservice.apollo.spring;

import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(ApolloSpringSubConfiguration.class)
public class ApolloSpringSubTest {

    @Test
    public void testWired() {
        MockApolloServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "name=John\nfull=John Doe\nlong=John Doe Richard");
        MockApolloServer.addOrModifyProperty("application", "SUB_DATA",
                "name=Joe\nfull=Joe Doe\nlong=Joe Doe Richard");
        await().forever().untilAsserted(() -> assertEquals("name=John\nfull=John Doe\nlong=John Doe Richard",
                ConfigService.getConfig("application").getProperty("DEFAULT_DATA", "")));
        await().forever().untilAsserted(() -> assertEquals("name=Joe\nfull=Joe Doe\nlong=Joe Doe Richard",
                ConfigService.getConfig("application").getProperty("SUB_DATA", "")));

        val testWired = SpringContext.getBean(TestWired.class);
        assertNotNull(testWired);
        assertEquals("Joe", testWired.name());
        assertEquals("Joe Doe", testWired.full());
        assertEquals("Joe Doe Richard", testWired.longName());
        assertEquals("Joe Doe Richard", testWired.longWrap());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testWired.defaultInContext());

        val testWiredSub = SpringContext.getBean(TestWiredSub.class);
        assertNotNull(testWiredSub);
        assertEquals("Joe", testWiredSub.name());
        assertEquals("Joe Doe", testWiredSub.full());
        assertEquals("Joe Doe Richard", testWiredSub.longName());
        assertEquals("Joe Doe Richard", testWiredSub.longWrap());
        assertEquals("xyz", testWiredSub.abc("xyz"));
        assertNull(testWiredSub.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testWiredSub.defaultInContext());
    }
}
