package com.github.charlemaznable.configservice.apollo.spring;

import com.ctrip.framework.apollo.ConfigService;
import com.github.charlemaznable.apollo.MockApolloServer;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.configservice.test.TestWired;
import com.github.charlemaznable.configservice.test.TestWiredConcrete;
import com.github.charlemaznable.configservice.test.TestWiredNone;
import com.github.charlemaznable.core.spring.SpringContext;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static com.github.charlemaznable.configservice.apollo.ApolloFactory.getApollo;
import static org.awaitility.Awaitility.await;
import static org.joor.Reflect.onClass;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringJUnitConfig(ApolloSpringNakedConfiguration.class)
public class ApolloSpringNakedTest {

    @Test
    public void testWiredNaked() {
        MockApolloServer.addOrModifyProperty("application", "DEFAULT_DATA",
                "name=John\nfull=John Doe\nlong=John Doe Richard");
        await().forever().untilAsserted(() -> assertEquals("name=John\nfull=John Doe\nlong=John Doe Richard",
                ConfigService.getConfig("application").getProperty("DEFAULT_DATA", "")));

        val testWired = getApollo(TestWired.class);
        assertNotNull(testWired);
        assertEquals("John", testWired.name());
        assertEquals("John Doe", testWired.full());
        assertThrows(NullPointerException.class, testWired::longName);
        assertNull(testWired.longWrap());
        assertEquals("xyz", testWired.abc("xyz"));
        assertNull(testWired.abc(null));

        assertThrows(ConfigServiceException.class,
                () -> getApollo(TestWiredConcrete.class));

        assertThrows(ConfigServiceException.class,
                () -> getApollo(TestWiredNone.class));

        ApplicationContext applicationContext = onClass(SpringContext.class)
                .field("applicationContext").get();
        assertThrows(NoSuchBeanDefinitionException.class, () ->
                applicationContext.getBean(TestWired.class));
        assertNull(SpringContext.getBeanOrCreate(TestWired.class));
    }
}
