package com.climaxion.drivedock.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DateUtils {

    public static final String DATE_TIME_FORMAT = Constants.DATE_TIME_FORMAT;

    private static final SimpleDateFormat apiFormat =
            new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
    private static final SimpleDateFormat displayFormat =
            new SimpleDateFormat(Constants.DISPLAY_DATE_FORMAT, Locale.getDefault());

    public static String formatForDisplay(String dateTimeStr) {
        try {
            Date date = apiFormat.parse(dateTimeStr);
            return displayFormat.format(date);
        } catch (ParseException e) {
            return dateTimeStr;
        }
    }

    public static long getDurationInHours(String startStr, String endStr) {
        try {
            Date start = apiFormat.parse(startStr);
            Date end = apiFormat.parse(endStr);
            long diffInMillis = end.getTime() - start.getTime();
            return TimeUnit.MILLISECONDS.toHours(diffInMillis);
        } catch (ParseException e) {
            return 0;
        }
    }

    public static double calculateTotalPrice(double pricePerHour, String startStr, String endStr) {
        long hours = getDurationInHours(startStr, endStr);
        return pricePerHour * hours;
    }

    public static String getCurrentDateTime() {
        return apiFormat.format(new Date());
    }
}
