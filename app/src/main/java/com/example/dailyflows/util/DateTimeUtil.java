package com.example.dailyflows.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtil {

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("dd MMMM yyyy", new Locale("ru"));
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("HH:mm", new Locale("ru"));

    public static String formatDate(long millis) {
        return DATE_FMT.format(new Date(millis));
    }

    public static String formatTime(long millis) {
        return TIME_FMT.format(new Date(millis));
    }

    public static long atStartOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long atEndOfDay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        c.set(Calendar.MILLISECOND, 999);
        return c.getTimeInMillis();
    }
}
