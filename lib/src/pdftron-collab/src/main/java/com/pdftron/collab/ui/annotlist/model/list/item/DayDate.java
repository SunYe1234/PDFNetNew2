package com.pdftron.collab.ui.annotlist.model.list.item;

import android.support.annotation.NonNull;

import com.pdftron.collab.utils.date.AnnotationListDateFormat;
import com.pdftron.collab.utils.date.BaseDateFormat;

import org.apache.commons.lang3.time.DateUtils;

import java.util.Calendar;
import java.util.Date;

/**
 * Date header data, that is held by a {@link AnnotationListHeader}. Will display the date if the
 * date is not today, otherwise shows "Today"
 */
public class DayDate implements Comparable<DayDate>, AnnotationListHeader.HeaderData {
    private final Date date;
    private final BaseDateFormat dateFormat;

    public DayDate(@NonNull Date date, @NonNull AnnotationListDateFormat dateFormat) {
        this.date = date;
        this.dateFormat = dateFormat;
    }

    @Override
    public String getHeaderTitle() {
        return dateFormat.getDateString(date);
    }

    @Override
    public int compareTo(DayDate that) {
        return that.date.compareTo(this.date); // note reversed order
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DayDate dayDate = (DayDate) o;
        return DateUtils.isSameDay(this.date, dayDate.date);
    }

    @Override
    public int hashCode() {
        final Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date);
        int val = cal1.get(Calendar.YEAR) * 13 + cal1.get(Calendar.DAY_OF_YEAR) + cal1.get(Calendar.ERA);
        return val ^ (val >> 16);
    }

    @NonNull
    @Override
    public String toString() {
        return this.date.toString();
    }

}
