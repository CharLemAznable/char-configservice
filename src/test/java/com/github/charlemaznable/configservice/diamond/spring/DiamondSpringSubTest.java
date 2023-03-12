package com.github.charlemaznable.configservice.diamond.spring;

import com.github.charlemaznable.configservice.TestUnWired;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringJUnitConfig(DiamondSpringSubConfiguration.class)
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
