package com.gym.fitconnectpro.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date and time operations
 */
public class DateUtil {

    private static final String TAG = "DateUtil";

    // Date format patterns
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_TIME_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String SQL_DATE_FORMAT = "yyyy-MM-dd";
    public static final String SQL_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * Format a date to "dd/MM/yyyy" format
     * @param date Date to format
     * @return Formatted date string
     */
    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date", e);
            return "";
        }
    }

    /**
     * Format a date to "dd/MM/yyyy HH:mm" format
     * @param date Date to format
     * @return Formatted date-time string
     */
    public static String formatDateTime(Date date) {
        if (date == null) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting date-time", e);
            return "";
        }
    }

    /**
     * Format a date to SQL format "yyyy-MM-dd"
     * @param date Date to format
     * @return Formatted SQL date string
     */
    public static String formatSqlDate(Date date) {
        if (date == null) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATE_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting SQL date", e);
            return "";
        }
    }

    /**
     * Format a date to SQL date-time format "yyyy-MM-dd HH:mm:ss"
     * @param date Date to format
     * @return Formatted SQL date-time string
     */
    public static String formatSqlDateTime(Date date) {
        if (date == null) {
            return "";
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(SQL_DATE_TIME_FORMAT, Locale.getDefault());
            return sdf.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Error formatting SQL date-time", e);
            return "";
        }
    }

    /**
     * Get current date
     * @return Current date
     */
    public static Date getCurrentDate() {
        return new Date();
    }

    /**
     * Get current date-time
     * @return Current date-time
     */
    public static Date getCurrentDateTime() {
        return new Date();
    }

    /**
     * Get current date formatted as "dd/MM/yyyy"
     * @return Formatted current date string
     */
    public static String getCurrentDateFormatted() {
        return formatDate(getCurrentDate());
    }

    /**
     * Get current date-time formatted as "dd/MM/yyyy HH:mm"
     * @return Formatted current date-time string
     */
    public static String getCurrentDateTimeFormatted() {
        return formatDateTime(getCurrentDateTime());
    }

    /**
     * Parse a date string in "dd/MM/yyyy" format
     * @param dateString Date string to parse
     * @return Parsed Date object, or null if parsing fails
     */
    public static Date parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
            return sdf.parse(dateString);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date: " + dateString, e);
            return null;
        }
    }

    /**
     * Parse a date-time string in "dd/MM/yyyy HH:mm" format
     * @param dateTimeString Date-time string to parse
     * @return Parsed Date object, or null if parsing fails
     */
    public static Date parseDateTime(String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
            return sdf.parse(dateTimeString);
        } catch (ParseException e) {
            Log.e(TAG, "Error parsing date-time: " + dateTimeString, e);
            return null;
        }
    }

    /**
     * Calculate days between two dates
     * @param startDate Start date
     * @param endDate End date
     * @return Number of days between dates
     */
    public static long daysBetween(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return 0;
        }

        try {
            long diffInMillis = endDate.getTime() - startDate.getTime();
            return TimeUnit.DAYS.convert(diffInMillis, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            Log.e(TAG, "Error calculating days between dates", e);
            return 0;
        }
    }

    /**
     * Add days to a date
     * @param date Original date
     * @param days Number of days to add
     * @return New date with added days
     */
    public static Date addDays(Date date, int days) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, days);
        return calendar.getTime();
    }

    /**
     * Add months to a date
     * @param date Original date
     * @param months Number of months to add
     * @return New date with added months
     */
    public static Date addMonths(Date date, int months) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    /**
     * Add years to a date
     * @param date Original date
     * @param years Number of years to add
     * @return New date with added years
     */
    public static Date addYears(Date date, int years) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    /**
     * Check if a date is in the past
     * @param date Date to check
     * @return true if date is in the past
     */
    public static boolean isPast(Date date) {
        if (date == null) {
            return false;
        }
        return date.before(new Date());
    }

    /**
     * Check if a date is in the future
     * @param date Date to check
     * @return true if date is in the future
     */
    public static boolean isFuture(Date date) {
        if (date == null) {
            return false;
        }
        return date.after(new Date());
    }

    /**
     * Check if a date is today
     * @param date Date to check
     * @return true if date is today
     */
    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }

        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);

        Calendar cal2 = Calendar.getInstance();

        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    /**
     * Get age from date of birth
     * @param dateOfBirth Date of birth
     * @return Age in years
     */
    public static int getAge(Date dateOfBirth) {
        if (dateOfBirth == null) {
            return 0;
        }

        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}
