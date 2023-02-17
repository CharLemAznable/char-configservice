package com.github.charlemaznable.configservice.apollo.spring;

import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.configservice.TestUnWired;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(ApolloSpringArgumentsExtension.class)
@SpringJUnitConfig(ApolloSpringConfiguration.class)
public class ApolloSpringTest {

    @Test
    public void testWired() {
        MockApolloServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "name=John\nfull=John Doe\nlong=John Doe Richard");
        await().forever().untilAsserted(() -> assertEquals("name=John\nfull=John Doe\nlong=John Doe Richard",
                ConfigService.getConfig("application").getProperty("DEFAULT_DATA", "")));

        val testWired = SpringContext.getBean(TestWired.class);
        assertNotNull(testWired);
        assertEquals("John", testWired.name());
        assertEquals("John Doe", testWired.full());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));
        assertEquals("John", testWired.name());
        assertEquals("John Doe", testWired.full());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));

        val testWiredConcrete = SpringContext.getBean(TestWiredConcrete.class);
        assertNull(testWiredConcrete);

        val testWiredNone = SpringContext.getBean(TestWiredNone.class);
        assertNull(testWiredNone);

        val testUnWired = SpringContext.getBean(TestUnWired.class);
        assertNotNull(testUnWired);
        assertEquals("John", testUnWired.name());
        assertEquals("John Doe", testUnWired.full());
        assertEquals("xyz", testUnWired.abc("xyz"));
        assertNull(testUnWired.abc(null));
    }
}
