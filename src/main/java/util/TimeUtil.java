package main.java.util;

import java.util.Calendar;

/**
 * Util class for manipulation with time.
 */
public class TimeUtil {
    private TimeUtil() {
        // prevent instantiation
    }

    /**
     * Get current time in string representation. Format is: hh:mm:ss
     * @return String which represent current time.
     */
    public static String getCurrentTimeString() {
        Calendar rightNowCalendar = Calendar.getInstance();
        int hours = rightNowCalendar.get(Calendar.HOUR_OF_DAY);
        int minutes = rightNowCalendar.get(Calendar.MINUTE);
        int seconds = rightNowCalendar.get(Calendar.SECOND);
        String currentTime = ((hours < 10) ? ("0" + hours) : hours) + ":"
                + ((minutes < 10) ? ("0" + minutes) : minutes) + ":"
                + ((seconds < 10) ? ("0" + seconds) : seconds) + " - ";

        return currentTime;
    }
}
