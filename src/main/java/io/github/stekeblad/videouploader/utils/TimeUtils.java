package io.github.stekeblad.videouploader.utils;

import java.time.*;

/**
 * Calculations and conversation related to time and timezones
 */
public class TimeUtils {
    /**
     * @return the time in the user's timezone that is the same as 00:00 PT,
     * using the 24-hours format and with always two digits for hours and minutes
     */
    public static String fromMidnightPacificToUserTimeZone() {
        ZoneOffset userUtcOffset = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault()).getOffset();
        ZoneOffset pacificOffset = ZoneId.of("UTC-8").getRules().getOffset(Instant.now());
        Duration timeDiff = Duration.ofSeconds(pacificOffset.compareTo(userUtcOffset));
        if (timeDiff.isNegative())
            timeDiff = timeDiff.plusHours(24);
        long minutes = timeDiff.minusHours(timeDiff.toHours()).toMinutes(); // example: 00:30 = 06:30 - 06:00
        return String.format("%02d", timeDiff.toHours()) + ":" + String.format("%02d", minutes);
    }

    public static String currentTimeString() {
        return LocalDateTime.now().toString();
    }
}
