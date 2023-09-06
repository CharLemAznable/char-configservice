package com.github.charlemaznable.configservice.impl;

import org.apache.commons.lang3.BooleanUtils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Functions {

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
                    long daysAsMilliSecs = parseNumber(dayMatch, MILLIS_PER_DAY);
                    long hoursAsMilliSecs = parseNumber(hourMatch, MILLIS_PER_HOUR);
                    long minutesAsMilliSecs = parseNumber(minuteMatch, MILLIS_PER_MINUTE);
                    long secondsAsMilliSecs = parseNumber(secondMatch, MILLIS_PER_SECOND);
                    long milliseconds = parseNumber(fractionMatch, 1);

                    return daysAsMilliSecs + hoursAsMilliSecs + minutesAsMilliSecs + secondsAsMilliSecs + milliseconds;
                }
            }
            throw new IllegalArgumentException(String.format("Text %s cannot be parsed to duration)", text));
        }


        private static long parseNumber(String parsed, int multiplier) {
            // regex limits to [0-9]+
            if (parsed == null || parsed.trim().isEmpty()) {
                return 0L;
            }
            return Long.parseLong(parsed) * multiplier;
        }
    }
}
