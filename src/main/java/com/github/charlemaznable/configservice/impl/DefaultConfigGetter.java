package com.github.charlemaznable.configservice.impl;

import com.github.charlemaznable.configservice.ConfigGetter;

public abstract class DefaultConfigGetter implements ConfigGetter {

    @Override
    public String getString(String key) {
        return getString(key, null);
    }

    @Override
    public int getInt(String key) {
        return getInt(key, 0);
    }

    @Override
    public long getLong(String key) {
        return getLong(key, 0);
    }

    @Override
    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    @Override
    public float getFloat(String key) {
        return getFloat(key, 0);
    }

    @Override
    public double getDouble(String key) {
        return getDouble(key, 0);
    }

    @Override
    public byte getByte(String key) {
        return getByte(key, (byte) 0);
    }

    @Override
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    @Override
    public long getDuration(String key) {
        return getDuration(key, 0);
    }
}
