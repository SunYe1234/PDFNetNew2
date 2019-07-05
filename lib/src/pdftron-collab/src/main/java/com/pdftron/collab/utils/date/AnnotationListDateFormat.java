package com.pdftron.collab.utils.date;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AnnotationListDateFormat extends BaseDateFormat {

    private AnnotationListDateFormat(@NonNull Date today, @NonNull Locale locale) {
        super(today, locale);
    }

    public static AnnotationListDateFormat newInstance(@NonNull Context context) {
        Date today = Calendar.getInstance().getTime();
        Locale locale = getLocale(context);

        return new AnnotationListDateFormat(today, locale);
    }

    private static String getShortDateString(@NonNull Date date, @NonNull Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", locale);
        return dateFormat.format(date);
    }

    private static boolean isSameDay(@NonNull final Date date1, @NonNull final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private static boolean isYesterday(@NonNull final Date date, @NonNull final Date today) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        final Calendar calToday = Calendar.getInstance();
        calToday.setTime(today);
        calToday.add(Calendar.DAY_OF_YEAR, -1);
        return cal1.get(Calendar.ERA) == calToday.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == calToday.get(Calendar.DAY_OF_YEAR);
    }

    @Override
    public String getDateString(@NonNull Date date) {
        if (isSameDay(date, mToday)) {
            return "Today";
        } else if (isYesterday(date, mToday)) {
            return "Yesterday";
        } else {
            return getShortDateString(date, mLocale);
        }
    }
}
