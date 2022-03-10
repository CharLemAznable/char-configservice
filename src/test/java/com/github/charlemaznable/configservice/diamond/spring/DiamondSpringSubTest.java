package com.github.charlemaznable.configservice.diamond.spring;

import com.github.charlemaznable.core.spring.SpringContext;
import com.github.charlemaznable.configservice.test.TestWired;
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
@ContextConfiguration(classes = DiamondSpringSubConfiguration.class)
public class DiamondSpringSubTest {

    @Test
    public void testWired() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "SUB_DATA",
                "name=Joe\nfull=${this.name} Doe\nlong=${this.full} Richard");

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
