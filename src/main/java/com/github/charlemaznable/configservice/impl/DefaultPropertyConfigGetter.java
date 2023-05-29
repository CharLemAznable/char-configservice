package com.github.charlemaznable.configservice.impl;

import org.apache.commons.lang3.BooleanUtils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.github.charlemaznable.core.lang.Condition.checkBlank;

public abstract class DefaultPropertyConfigGetter extends DefaultConfigGetter {

    public abstract String getPropertyValue(String keyset, String key);

    @Override
    public int getInt(String key, int defaultValue) {
        return getValue(key, defaultValue, Functions.TO_INT_FUNCTION);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return getValue(key, defaultValue, Functions.TO_LONG_FUNCTION);
    }

    @Override
    public short getShort(String key, short defaultValue) {
        return getValue(key, defaultValue, Functions.TO_SHORT_FUNCTION);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return getValue(key, defaultValue, Functions.TO_FLOAT_FUNCTION);
    }

    @Override
    public double getDouble(String key, double defaultValue) {
        return getValue(key, defaultValue, Functions.TO_DOUBLE_FUNCTION);
    }

    @Override
    public byte getByte(String key, byte defaultValue) {
        return getValue(key, defaultValue, Functions.TO_BYTE_FUNCTION);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return getValue(key, defaultValue, BooleanUtils::toBoolean);
    }

    @Override
    public long getDuration(String key, long defaultValue) {
        return getValue(key, defaultValue, Functions.TO_DURATION_FUNCTION);
    }

    private <T> T getValue(String key, T defaultValue, Function<String, T> parser) {
        return checkBlank(getString(key), () -> defaultValue, value -> {
            try {
                return parser.apply(value);
            } catch (Exception e) {
                return defaultValue;
            }
        });
    }

    interface Functions {

        Function<String, String> TO_STR_FUNCTION = Function.identity();
        Function<String, Integer> TO_INT_FUNCTION = Integer::parseInt;
        Function<String, Long> TO_LONG_FUNCTION = Long::parseLong;
        Function<String, Short> TO_SHORT_FUNCTION = Short::parseShort;
        Function<String, Float> TO_FLOAT_FUNCTION = Float::parseFloat;
        Function<String, Double> TO_DOUBLE_FUNCTION = Double::parseDouble;
        Function<String, Byte> TO_BYTE_FUNCTION = Byte::parseByte;
        Function<String, Boolean> TO_BOOLEAN_FUNCTION = BooleanUtils::toBoolean;
        Function<String, Long> TO_DURATION_FUNCTION = DurationParser.INSTANCE::parseToMillis;

        enum DurationParser {

            INSTANCE;

            private static final Pattern PATTERN =
                    Pattern.compile("(?:([0-9]+)D)?(?:([0-9]+)H)?(?:([0-9]+)M)?(?:([0-9]+)S)?(?:([0-9]+)(?:MS)?)?",
                            Pattern.CASE_INSENSITIVE);

            private static final int HOURS_PER_DAY = 24;
            private static final int MINUTES_PER_HOUR = 60;
            private static final int SECONDS_PER_MINUTE = 60;
            private static final int MILLIS_PER_SECOND = 1000;
            private static final int MILLIS_PER_MINUTE = MILLIS_PER_SECOND * SECONDS_PER_MINUTE;
            private static final int MILLIS_PER_HOUR = MILLIS_PER_MINUTE * MINUTES_PER_HOUR;
            private static final int MILLIS_PER_DAY = MILLIS_PER_HOUR * HOURS_PER_DAY;

            public long parseToMillis(String text) {
                Matcher matcher = PATTERN.matcher(text);
                if (matcher.matches()) {
                    String dayMatch = matcher.group(1);
                    String hourMatch = matcher.group(2);
                    String minuteMatch = matcher.group(3);
                    String secondMatch = matcher.group(4);
                    String fractionMatch = matcher.group(5);
                    if (dayMatch != null || hourMatch != null || minuteMatch != null || secondMatch != null || fractionMatch != null) {
                        int daysAsMilliSecs = parseNumber(dayMatch, MILLIS_PER_DAY);
                        int hoursAsMilliSecs = parseNumber(hourMatch, MILLIS_PER_HOUR);
                        int minutesAsMilliSecs = parseNumber(minuteMatch, MILLIS_PER_MINUTE);
                        int secondsAsMilliSecs = parseNumber(secondMatch, MILLIS_PER_SECOND);
                        int milliseconds = parseNumber(fractionMatch, 1);

                        return daysAsMilliSecs + hoursAsMilliSecs + minutesAsMilliSecs + secondsAsMilliSecs + milliseconds;
                    }
                }
                throw new IllegalArgumentException(String.format("Text %s cannot be parsed to duration)", text));
            }


            private static int parseNumber(String parsed, int multiplier) {
                // regex limits to [0-9]+
                if (parsed == null || parsed.trim().isEmpty()) {
                    return 0;
                }
                return Integer.parseInt(parsed) * multiplier;
            }
        }
    }
}