package com.pdftron.collab.utils.date;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.pdftron.pdf.utils.Utils;

import org.apache.commons.lang3.time.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class ReplyDateFormat extends BaseDateFormat {

    public ReplyDateFormat(@NonNull Date today, @NonNull Locale locale) {
        super(today, locale);
    }

    public static ReplyDateFormat newInstance(@NonNull Context context) {
        Date today = Calendar.getInstance().getTime();
        Locale locale = getLocale(context);

        return new ReplyDateFormat(today, locale);
    }

    @Override
    public String getDateString(@NonNull Date date) {
        if (DateUtils.isSameDay(date, mToday)) {
            return getShortTimeString(date, mLocale);
        } else if (isSameYear(date, mToday)) {
            return getShortDateTimeString(date, mLocale);
        } else {
            return getLongDateTimeString(date, mLocale);
        }
    }

    protected static String getShortDateTimeString(@NonNull Date date, @NonNull Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, h:mm a", locale);
        return dateFormat.format(date);
    }

    protected static String getLongDateTimeString(@NonNull Date date, @NonNull Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm a", locale);
        return dateFormat.format(date);
    }

    protected static String getShortTimeString(@NonNull Date date, @NonNull Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat("h:mm a", locale);
        return dateFormat.format(date);
    }

    public static boolean isSameYear(@NonNull final Date date1, @NonNull final Date date2) {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        final Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }

    /**
     * Returns a {@link DateFormat} formatted as yyyy-MM-dd.
     *
     * @param context to get device locale
     * @return DateFormat formatted as yyyy-MM-dd.
     */
    public static DateFormat getSimpleDateFormat(@NonNull Context context) {
        DateFormat dateFormat;
        if (Utils.isNougat()) {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", context.getResources().getConfiguration().getLocales().get(0));
        } else {
            dateFormat = new SimpleDateFormat("yyyy-MM-dd", context.getResources().getConfiguration().locale);
        }
        return dateFormat;
    }
}
