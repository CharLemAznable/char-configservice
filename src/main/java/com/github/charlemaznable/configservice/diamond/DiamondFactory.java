package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.ConfigLoader;
import com.github.charlemaznable.configservice.ConfigProxy;
import com.github.charlemaznable.configservice.annotation.DefaultEmptyValue;
import com.github.charlemaznable.configservice.diamond.DiamondConfig.DataIdProvider;
import com.github.charlemaznable.configservice.diamond.DiamondConfig.DefaultValueProvider;
import com.github.charlemaznable.configservice.diamond.DiamondConfig.GroupProvider;
import com.github.charlemaznable.configservice.elf.ConfigServiceException;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;
import net.jodah.expiringmap.ExpiringValue;
import org.n3r.diamond.client.AbstractMiner;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.impl.PropertiesBasedMiner;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.parseStringToProperties;
import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.substitute;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.Condition.checkNotNull;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.github.charlemaznable.core.spring.AnnotationElf.findAnnotation;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;

@NoArgsConstructor(access = PRIVATE)
public final class DiamondFactory {

    private static final LoadingCache<Factory, DiamondLoader>
            loaderCache = simpleCache(from(DiamondLoader::new));

    public static <T> T getDiamond(Class<T> configClass) {
        return diamondLoader(FactoryContext.get()).getDiamond(configClass);
    }

    public static DiamondLoader diamondLoader(Factory factory) {
        return get(loaderCache, factory);
    }

    public static final class DiamondLoader extends ConfigLoader {

        DiamondLoader(Factory factory) {
            super(factory);
        }

        public <T> T getDiamond(Class<T> configClass) {
            return getConfig(configClass);
        }

        @Override
        protected void checkClassConfig(Class<?> configClass) {
            checkNotNull(findAnnotation(configClass, DiamondConfig.class),
                    new ConfigServiceException(configClass + " has no DiamondConfig"));
        }

        @Override
        protected <T> ConfigProxy<T> buildConfigProxy(Class<T> configClass, Factory factory) {
            return new DiamondProxy<>(configClass, factory, this);
        }

        @Override
        protected <T> ExpiringValue<ConfigGetter> loadConfigGetter(Class<T> configClass) {
            val diamondConfig = checkDiamondConfig(configClass);
            val group = checkDiamondGroup(configClass, diamondConfig);
            val dataId = checkDiamondDataId(configClass, diamondConfig);
            val cacheSeconds = Math.max(0, diamondConfig.cacheSeconds());

            val miner = new Miner(blankThen(group, () -> "DEFAULT_GROUP"));
            val configGetter = new DiamondConfigGetter(isBlank(dataId) ? miner :
                    new PropertiesBasedMiner(parseStringToProperties(miner.getString(dataId), dataId)));
            return new ExpiringValue<>(configGetter, cacheSeconds, TimeUnit.SECONDS);
        }

        private <T> DiamondConfig checkDiamondConfig(Class<T> configClass) {
            return checkNotNull(getMergedAnnotation(configClass, DiamondConfig.class));
        }

        private <T> String checkDiamondGroup(Class<T> configClass, DiamondConfig diamondConfig) {
            val providerClass = diamondConfig.groupProvider();
            return substitute(GroupProvider.class == providerClass ? diamondConfig.group()
                    : FactoryContext.apply(factory, providerClass, p -> p.group(configClass)));
        }

        private <T> String checkDiamondDataId(Class<T> configClass, DiamondConfig diamondConfig) {
            val providerClass = diamondConfig.dataIdProvider();
            return substitute(DataIdProvider.class == providerClass ? diamondConfig.dataId()
                    : FactoryContext.apply(factory, providerClass, p -> p.dataId(configClass)));
        }
    }

    public static final class DiamondProxy<T> extends ConfigProxy<T> {

        DiamondProxy(Class<T> configClass, Factory factory, ConfigLoader configLoader) {
            super(configClass, factory, configLoader);
        }

        @Override
        protected ExpiringValue<ConfigEntry> loadConfigEntry(Method method) {
            val minerConfig = getMergedAnnotation(method, DiamondConfig.class);
            val group = checkDiamondGroup(method, minerConfig);
            val dataId = checkDiamondDataId(method, minerConfig);
            val defaultEmptyValue = nonNull(findAnnotation(method, DefaultEmptyValue.class));
            val defaultValue = checkDiamondDefaultValue(method, minerConfig, defaultEmptyValue);
            val cacheSeconds = checkDiamondCacheSeconds(minerConfig);
            val configGetter = (DiamondConfigGetter) configLoader.getConfigGetter(configClass);
            val minerable = configGetter.getMinerable();
            // group blank:
            //   if minerable instanceof AbstractMiner
            //     use its defaultGroupName
            //   else do nothing
            String stoneGroup = blankThen(group, () ->
                    minerable instanceof AbstractMiner abstractMiner ?
                            abstractMiner.getDefaultGroupName() : group);
            String stoneDataId = blankThen(dataId, method::getName);
            String stone = minerable.getStone(stoneGroup, stoneDataId);
            return new ExpiringValue<>(new ConfigEntry(stoneDataId,
                    stone, defaultValue), cacheSeconds, TimeUnit.SECONDS);
        }

        private String checkDiamondGroup(Method method, DiamondConfig diamondConfig) {
            if (isNull(diamondConfig)) return "";
            val providerClass = diamondConfig.groupProvider();
            return substitute(GroupProvider.class == providerClass ? diamondConfig.group()
                    : FactoryContext.apply(factory, providerClass, p -> p.group(configClass, method)));
        }

        private String checkDiamondDataId(Method method, DiamondConfig diamondConfig) {
            if (isNull(diamondConfig)) return "";
            val providerClass = diamondConfig.dataIdProvider();
            return substitute(DataIdProvider.class == providerClass ? diamondConfig.dataId()
                    : FactoryContext.apply(factory, providerClass, p -> p.dataId(configClass, method)));
        }

        @SuppressWarnings("Duplicates")
        private String checkDiamondDefaultValue(Method method, DiamondConfig diamondConfig, boolean defaultEmptyValue) {
            if (isNull(diamondConfig)) return defaultEmptyValue ? "" : null;
            val providerClass = diamondConfig.defaultValueProvider();
            String defaultValue = diamondConfig.defaultValue();
            if (DefaultValueProvider.class != providerClass) {
                defaultValue = FactoryContext.apply(factory, providerClass,
                        p -> p.defaultValue(configClass, method));
            }
            return substitute(blankThen(defaultValue, () -> defaultEmptyValue ? "" : null));
        }

        private long checkDiamondCacheSeconds(DiamondConfig diamondConfig) {
            if (isNull(diamondConfig)) return 0;
            return Math.max(0, diamondConfig.cacheSeconds());
        }
    }
}
