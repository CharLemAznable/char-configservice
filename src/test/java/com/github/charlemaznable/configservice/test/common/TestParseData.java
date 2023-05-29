package com.github.charlemaznable.configservice.test.common;

import com.github.charlemaznable.configservice.annotation.ConfigValueParseBy;
import com.github.charlemaznable.configservice.annotation.ConfigValueParseBy.ConfigValueParser;
import com.github.charlemaznable.configservice.apollo.ApolloConfig;
import com.github.charlemaznable.configservice.diamond.DiamondConfig;
import com.github.charlemaznable.configservice.etcd.EtcdConfig;
import com.github.charlemaznable.configservice.impl.ParseAsEsConfig;
import com.github.charlemaznable.configservice.impl.ParseAsVertxOptions;
import com.github.charlemaznable.core.es.EsConfig;
import io.vertx.core.VertxOptions;

import java.util.Properties;

import static com.github.charlemaznable.core.es.EsClientElf.parsePropertiesToEsConfig;
import static com.github.charlemaznable.core.lang.Propertiess.parseStringToProperties;
import static com.github.charlemaznable.core.vertx.VertxElf.parsePropertiesToVertxOptions;

@ApolloConfig(namespace = "PARSE_GROUP")
@DiamondConfig(group = "PARSE_GROUP")
@EtcdConfig(namespace = "PARSE_GROUP")
public interface TestParseData {

    @ApolloConfig("PARSE_DATA")
    @DiamondConfig("PARSE_DATA")
    @EtcdConfig("PARSE_DATA")
    @ConfigValueParseBy(ConfigValueParser.class)
    Properties properties();

    @ApolloConfig("PARSE_DATA")
    @DiamondConfig("PARSE_DATA")
    @EtcdConfig("PARSE_DATA")
    @ParseAsVertxOptions
    VertxOptions vertxOptions();

    @ApolloConfig("PARSE_DATA")
    @DiamondConfig("PARSE_DATA")
    @EtcdConfig("PARSE_DATA")
    @ParseAsEsConfig
    EsConfig esConfig();

    @ApolloConfig("PARSE_DATA")
    @DiamondConfig("PARSE_DATA")
    @EtcdConfig("PARSE_DATA")
    String raw();

    default VertxOptions parseRawToVertxOptions() {
        return parsePropertiesToVertxOptions(parseStringToProperties(raw()));
    }

    default EsConfig parseRawToEsConfig() {
        return parsePropertiesToEsConfig(parseStringToProperties(raw()));
    }
}
