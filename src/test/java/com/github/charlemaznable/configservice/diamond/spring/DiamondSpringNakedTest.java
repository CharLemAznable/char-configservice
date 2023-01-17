package com.github.charlemaznable.configservice.diamond.spring;

import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.n3r.diamond.client.impl.MockDiamondServer;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.configservice.diamond.DiamondFactory.getDiamond;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(DiamondSpringNakedConfiguration.class)
public class DiamondSpringNakedTest {

    @Test
    public void testWiredNaked() {
        MockDiamondServer.setConfigInfo("DEFAULT_GROUP", "DEFAULT_DATA",
                "name=John\nfull=${this.name} Doe\nlong=${this.full} Richard");

        val testWired = getDiamond(TestWired.class);
        assertNotNull(testWired);
        assertEquals("John", testWired.name());
        assertEquals("John Doe", testWired.full());
        assertThrows(NullPointerException.class, testWired::longName);
        assertNull(testWired.longWrap());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));

        assertThrows(ConfigServiceException.class,
                () -> getDiamond(TestWiredConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> getDiamond(TestWiredNone.class));

        ApplicationContext applicationContext = onClass(SpringContext.class)
                .field("applicationContext").get();
        assertThrows(NoSuchBeanDefinitionException.class, () ->
                applicationContext.getBean(TestWired.class));
        assertNull(SpringContext.getBeanOrCreate(TestWired.class));
    }
}
