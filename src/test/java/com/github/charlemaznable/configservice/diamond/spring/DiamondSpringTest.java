package com.github.charlemaznable.configservice.diamond.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DiamondSpringConfiguration.class)
public class DiamondSpringTest {

    @Test
    public void testWired() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

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
