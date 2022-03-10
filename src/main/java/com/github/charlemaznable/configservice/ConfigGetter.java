package com.github.charlemaznable.configservice;

public interface ConfigGetter {

    String getString(String key);
    String getString(String key, String defaultValue);

    int getInt(String key);
    int getInt(String key, int defaultValue);

    long getLong(String key);
    long getLong(String key, long defaultValue);

    short getShort(String key);
    short getShort(String key, short defaultValue);

    float getFloat(String key);
    float getFloat(String key, float defaultValue);

    double getDouble(String key);
    double getDouble(String key, double defaultValue);

    byte getByte(String key);
    byte getByte(String key, byte defaultValue);

    boolean getBoolean(String key);
    boolean getBoolean(String key, boolean defaultValue);

    long getDuration(String key);
    long getDuration(String key, long defaultValue);
}
