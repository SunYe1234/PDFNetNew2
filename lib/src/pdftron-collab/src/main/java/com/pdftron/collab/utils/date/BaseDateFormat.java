package com.pdftron.collab.utils.date;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.pdftron.pdf.utils.Utils;

import java.util.Date;
import java.util.Locale;

/**
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class BaseDateFormat {

    final Date mToday;
    final Locale mLocale;

    BaseDateFormat(@NonNull Date today, @NonNull Locale locale) {
        mToday = today;
        mLocale = locale;
    }

    public abstract String getDateString(@NonNull Date date);

    static Locale getLocale(@NonNull Context context) {
        return Utils.isNougat() ?
                context.getResources().getConfiguration().getLocales().get(0) :
                context.getResources().getConfiguration().locale;
    }
}
