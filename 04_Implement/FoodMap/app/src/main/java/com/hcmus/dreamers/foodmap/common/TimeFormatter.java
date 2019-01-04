package com.hcmus.dreamers.foodmap.common;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormatter {
    public static final String FULL_TIME = "HH:mm:ss";
    public static final String SHORT_TIME = "HH:mm";
    public static final String FULL_DATE = "yyyy-MM-dd HH:mm:ss";

    private static SimpleDateFormat timeFormatter = null;

    public static String format(Date date, boolean fullTime) {
        if (fullTime)
            timeFormatter = new SimpleDateFormat(FULL_TIME);
        else
            timeFormatter = new SimpleDateFormat(SHORT_TIME);
        return timeFormatter.format(date);
    }

    public static String format(int hourOfDay, int minute) {
        return String.format("%02d:%02d", hourOfDay, minute);
    }

    public static Date parse(String time) {
        timeFormatter = new SimpleDateFormat(SHORT_TIME);
        Date date = null;
        try {
            date = timeFormatter.parse(time);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String format(Date date){
        timeFormatter = new SimpleDateFormat(FULL_DATE);
        return timeFormatter.format(date);
    }
}
