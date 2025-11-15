package ru.practicum.ewm.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class DateTimeUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static LocalDateTime parse(String s) {
        return LocalDateTime.parse(s, FORMATTER);
    }

    public static LocalDateTime parseNullable(String s) {
        return (null == s || s.isBlank()) ? null : parse(s);
    }

    public static String format(LocalDateTime dt) {
        return dt.format(FORMATTER);
    }
}