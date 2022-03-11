package com.github.charlemaznable.configservice.apollo.spring;

import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith({SpringExtension.class, ApolloSpringArgumentsExtension.class})
@ContextConfiguration(classes = ApolloSpringConfiguration.class)
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
        assertEquals("John Doe Richard", testWired.longName());
        assertEquals("John Doe Richard", testWired.longWrap());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testWired.defaultInContext());
        assertEquals("John", testWired.name());
        assertEquals("John Doe", testWired.full());
        assertEquals("John Doe Richard", testWired.longName());
        assertEquals("John Doe Richard", testWired.longWrap());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));
        assertEquals("springspring&springspring&guiceguice",
                testWired.defaultInContext());

        val testWiredConcrete = SpringContext.getBean(TestWiredConcrete.class);
        assertNull(testWiredConcrete);

        val testWiredNone = SpringContext.getBean(TestWiredNone.class);
        assertNull(testWiredNone);
    }
}
