package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.Config;
import com.github.charlemaznable.configservice.ConfigGetter;
import com.github.charlemaznable.configservice.ConfigListener;
import com.github.charlemaznable.configservice.ConfigLoader;
import com.github.charlemaznable.configservice.ConfigProxy;
import com.github.charlemaznable.configservice.elf.ConfigImpl;
import com.github.charlemaznable.configservice.elf.ConfigListenerProxy;
import com.github.charlemaznable.configservice.elf.ConfigListenerRegisterProxy;
import com.github.charlemaznable.configservice.elf.ConfigSetting;
import com.github.charlemaznable.core.context.FactoryContext;
import com.github.charlemaznable.core.lang.Factory;
import com.google.common.cache.LoadingCache;
import lombok.NoArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.tuple.Triple;
import org.n3r.diamond.client.AbstractMiner;
import org.n3r.diamond.client.DiamondAxis;
import org.n3r.diamond.client.DiamondListener;
import org.n3r.diamond.client.DiamondStone;
import org.n3r.diamond.client.Miner;
import org.n3r.diamond.client.impl.DiamondSubscriber;
import org.n3r.diamond.client.impl.PropertiesBasedMiner;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.github.charlemaznable.configservice.elf.ConfigServiceElf.parseStringToProperties;
import static com.github.charlemaznable.core.lang.Condition.blankThen;
import static com.github.charlemaznable.core.lang.LoadingCachee.get;
import static com.github.charlemaznable.core.lang.LoadingCachee.simpleCache;
import static com.github.charlemaznable.core.lang.Str.isBlank;
import static com.google.common.cache.CacheLoader.from;
import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.isAnnotated;

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

        @SuppressWarnings("unchecked")
        @Override
        public Class<? extends Annotation>[] annotationClasses() {
            return new Class[]{Config.class, DiamondConfig.class};
        }

        @Override
        protected void checkClassConfig(Class<?> configClass) {
            if (isAnnotated(configClass, DiamondConfig.class)) return;
            super.checkClassConfig(configClass);
        }

        @Override
        protected <T> ConfigProxy<T> buildConfigProxy(Class<T> configClass, Factory factory) {
            return new DiamondProxy<>(configClass, factory, this);
        }

        @Override
        public Config fetchConfigAnno(AnnotatedElement element) {
            val diamondConfig = getMergedAnnotation(element, DiamondConfig.class);
            if (isNull(diamondConfig)) return super.fetchConfigAnno(element);
            return ConfigImpl.builder()
                    .keyset(diamondConfig.group())
                    .key(diamondConfig.dataId())
                    .value(diamondConfig.value())
                    .defaultValue(diamondConfig.defaultValue())
                    .cacheSeconds(diamondConfig.cacheSeconds()).build();
        }

        @Override
        protected ConfigGetter buildConfigGetter(ConfigSetting configSetting) {
            val group = configSetting.keyset();
            val dataId = configSetting.key();
            val miner = new Miner(blankThen(group, () -> "DEFAULT_GROUP"));
            return new DiamondConfigGetter(isBlank(dataId) ? miner :
                    new PropertiesBasedMiner(parseStringToProperties(miner.getString(dataId), dataId)));
        }
    }

    public static final class DiamondProxy<T> extends ConfigProxy<T> {

        DiamondProxy(Class<T> configClass, Factory factory, ConfigLoader configLoader) {
            super(configClass, factory, configLoader);
        }

        @Override
        protected ConfigListenerRegisterProxy<? extends ConfigListenerProxy> buildListenerRegisterProxy(Class<?> configClass, ConfigLoader configLoader) {
            return new DiamondConfigListenerRegister(configClass, configLoader);
        }

        @Override
        protected String loadConfigValue(ConfigGetter configGetter, ConfigSetting configSetting) {
            val minerable = ((DiamondConfigGetter) configGetter).getMinerable();
            // group blank:
            //   if minerable instanceof AbstractMiner
            //     use its defaultGroupName
            //   else do nothing
            String stoneGroup = blankThen(configSetting.keyset(), () -> minerable instanceof AbstractMiner ?
                    ((AbstractMiner) minerable).getDefaultGroupName() : configSetting.keyset());
            return minerable.getStone(stoneGroup, configSetting.key());
        }
    }

    public static final class DiamondConfigListener extends ConfigListenerProxy implements DiamondListener {

        public DiamondConfigListener(String keyset, String key, ConfigListener listener) {
            super(keyset, key, listener);
        }

        @Override
        public void accept(DiamondStone diamondStone) {
            this.onChange(diamondStone.getContent());
        }
    }

    public static final class DiamondConfigListenerRegister extends ConfigListenerRegisterProxy<DiamondConfigListener> {

        private static final Map<Triple<String, String, ConfigListener>, DiamondConfigListener> listenerMap = new ConcurrentHashMap<>();

        public DiamondConfigListenerRegister(Class<?> configClass, ConfigLoader configLoader) {
            super(configClass, configLoader);
        }

        @Override
        protected String defaultListeningKeyset() {
            return blankThen(super.defaultListeningKeyset(), () -> "DEFAULT_GROUP");
        }

        @Override
        protected Map<Triple<String, String, ConfigListener>, DiamondConfigListener> listenerMap() {
            return listenerMap;
        }

        @Override
        protected DiamondConfigListener addConfigListenerProxy(String keyset, String key, ConfigListener listener) {
            val diamondListener = new DiamondConfigListener(keyset, key, listener);
            submitListenerEvent(() -> DiamondSubscriber.getInstance()
                    .addDiamondListener(DiamondAxis.makeAxis(keyset, key), diamondListener));
            return diamondListener;
        }

        @Override
        protected void removeConfigListenerProxy(String keyset, String key, DiamondConfigListener listenerProxy) {
            submitListenerEvent(() -> DiamondSubscriber.getInstance()
                    .removeDiamondListener(DiamondAxis.makeAxis(keyset, key), listenerProxy));
        }
    }
}
