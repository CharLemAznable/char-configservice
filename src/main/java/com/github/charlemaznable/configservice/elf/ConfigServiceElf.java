package com.github.charlemaznable.configservice.elf;

import com.github.charlemaznable.configservice.annotation.ConfigValueParse;
import com.github.charlemaznable.configservice.annotation.ConfigValueParse.ConfigValueParser;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.google.common.primitives.Primitives;
import com.moandjiezana.toml.Toml;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.text.StringSubstitutor;
import org.n3r.diamond.client.impl.DiamondUtils;

import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

import static com.github.charlemaznable.core.config.Arguments.argumentsAsSubstitutor;
import static com.github.charlemaznable.core.lang.ClzPath.classResourceAsSubstitutor;
import static com.github.charlemaznable.core.lang.Propertiess.tryDecrypt;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.BooleanUtils.toBoolean;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.apache.commons.lang3.math.NumberUtils.toByte;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.lang3.math.NumberUtils.toFloat;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.apache.commons.lang3.math.NumberUtils.toShort;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;

@Slf4j
@NoArgsConstructor(access = PRIVATE)
public final class ConfigServiceElf {

    private static StringSubstitutor classPathSubstitutor;

    public static String substitute(String source) {
        if (isNull(classPathSubstitutor)) {
            classPathSubstitutor = classResourceAsSubstitutor("config.env.props");
        }
        return classPathSubstitutor.replace(argumentsAsSubstitutor().replace(source));
    }

    public static void ensureClassIsAnInterface(Class<?> clazz) {
        if (clazz.isInterface()) return;
        throw new ConfigServiceException(clazz + " is not An Interface");
    }

    public static Object convertType(String value, Method method, Factory factory, String password) {
        val rt = Primitives.unwrap(method.getReturnType());
        if (rt == String.class) return value;
        if (rt.isPrimitive()) return parsePrimitive(rt, value);

        val configValueParse = getMergedAnnotation(method, ConfigValueParse.class);
        if (nonNull(configValueParse)) {
            val parserClass = configValueParse.value();
            if (ConfigValueParser.class != parserClass) {
                return FactoryContext.apply(factory, parserClass,
                        parser -> parser.parse(value, rt));
            }
        }

        if (Map.class.isAssignableFrom(rt))
            return parseStringToProperties(value, password);

        val grt = method.getGenericReturnType();
        val isCollection = grt instanceof ParameterizedType
                && Collection.class.isAssignableFrom(rt);

        if (!isCollection) return DiamondUtils.parseObject(value, rt);
        return DiamondUtils.parseObjects(value,
                (Class<?>) ((ParameterizedType) grt).getActualTypeArguments()[0]);
    }

    public static Object parsePrimitive(Class<?> rt, String value) {
        if (rt == boolean.class) return toBoolean(value);
        if (rt == short.class) return toShort(value);
        if (rt == int.class) return toInt(value);
        if (rt == long.class) return toLong(value);
        if (rt == float.class) return toFloat(value);
        if (rt == double.class) return toDouble(value);
        if (rt == byte.class) return toByte(value);
        if (rt == char.class) return value.length() > 0 ? value.charAt(0) : '\0';
        return null;
    }

    public static Properties parseStringToProperties(String string, String password) {
        val properties = new Properties();
        if (string != null) {
            try {
                if (startsWithIgnoreCase(string, "# toml")) {
                    properties.putAll(new Toml().read(string).toMap());
                } else {
                    properties.load(new StringReader(string));
                }
            } catch (Exception e) {
                log.warn("Parse string to properties failed:", e);
            }
        }
        return null == password ? properties : tryDecrypt(properties, password);
    }
}
