package com.github.charlemaznable.configservice.diamond;

import com.github.charlemaznable.configservice.impl.DefaultConfigGetter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.n3r.diamond.client.Minerable;

import java.util.concurrent.TimeUnit;

import static com.github.charlemaznable.core.lang.Condition.nullThen;
import static org.apache.commons.lang3.BooleanUtils.toBooleanObject;
import static org.apache.commons.lang3.math.NumberUtils.toByte;
import static org.apache.commons.lang3.math.NumberUtils.toDouble;
import static org.apache.commons.lang3.math.NumberUtils.toFloat;
import static org.apache.commons.lang3.math.NumberUtils.toInt;
import static org.apache.commons.lang3.math.NumberUtils.toLong;
import static org.apache.commons.lang3.math.NumberUtils.toShort;

@AllArgsConstructor
@Getter
public final class DiamondConfigGetter extends DefaultConfigGetter {

    private Minerable minerable;

    @Override
    public String getString(String key, String defaultValue) {
        return nullThen(minerable.getString(key), () -> defaultValue);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return toInt(minerable.getString(key), defaultValue);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return toLong(minerable.getString(key), defaultValue);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        return toShort(minerable.getString(key), defaultValue);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return toFloat(minerable.getString(key), defaultValue);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return toDouble(minerable.getString(key), defaultValue);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        return toByte(minerable.getString(key), defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return nullThen(toBooleanObject(minerable.getString(key)), () -> defaultValue);
    }

    @Override
    public long getDuration(String key, long defaultValue) {
        return minerable.getDuration(key, TimeUnit.MILLISECONDS, defaultValue);
    }
}
