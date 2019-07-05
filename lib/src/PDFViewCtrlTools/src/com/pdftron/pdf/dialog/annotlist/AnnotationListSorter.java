package com.pdftron.pdf.dialog.annotlist;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.pdftron.common.PDFNetException;
import com.pdftron.pdf.controls.AnnotationDialogFragment;
import com.pdftron.pdf.utils.AnnotUtils;

import java.util.Comparator;
import java.util.Date;

/**
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AnnotationListSorter extends BaseAnnotationListSorter<AnnotationDialogFragment.AnnotationInfo> {

    protected final Comparator<AnnotationDialogFragment.AnnotationInfo> mTopToBottomComparator =
            new Comparator<AnnotationDialogFragment.AnnotationInfo>() {
                @Override
                public int compare(AnnotationDialogFragment.AnnotationInfo thisObj, AnnotationDialogFragment.AnnotationInfo thatObj) {
                    return compareYPosition(thisObj, thatObj);
                }
            };

    protected final Comparator<AnnotationDialogFragment.AnnotationInfo> mDateComparator =
            new Comparator<AnnotationDialogFragment.AnnotationInfo>() {
                @Override
                public int compare(AnnotationDialogFragment.AnnotationInfo thisObj, AnnotationDialogFragment.AnnotationInfo thatObj) {
                    return compareDate(thisObj, thatObj);
                }
            };

    public AnnotationListSorter(@NonNull BaseAnnotationSortOrder sortOrder) {
        super(sortOrder);
    }

    @NonNull
    @Override
    public Comparator<AnnotationDialogFragment.AnnotationInfo> getComparator() {
        BaseAnnotationSortOrder value = mSortOrder.getValue();
        if (value != null) {
            if (value instanceof AnnotationListSortOrder) {
                switch ((AnnotationListSortOrder) value) {
                    case DATE_ASCENDING:
                        return mDateComparator;
                    case POSITION_ASCENDING:
                        return mTopToBottomComparator;
                }
            }
        }
        return mDateComparator; // default we sort by descending date
    }

    public static int compareDate(AnnotationDialogFragment.AnnotationInfo thisObj,
            AnnotationDialogFragment.AnnotationInfo thatObj) {

        if (thisObj.getAnnotation() == null || thatObj.getAnnotation() == null) {
            return 0;
        }
        try {
            Date thisDate = AnnotUtils.getAnnotLocalDate(thisObj.getAnnotation());
            Date thatDate = AnnotUtils.getAnnotLocalDate(thatObj.getAnnotation());
            return thisDate.compareTo(thatDate);
        } catch (PDFNetException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static int compareYPosition(AnnotationDialogFragment.AnnotationInfo thisObj,
            AnnotationDialogFragment.AnnotationInfo thatObj) {
        double thisY2 = thisObj.getY2();
        double thatY2 = thatObj.getY2();
        return Double.compare(thatY2, thisY2); // note reversed o1 and o2
    }

    public static class Factory implements ViewModelProvider.Factory {
        private BaseAnnotationSortOrder mSortOrder;

        public Factory(BaseAnnotationSortOrder sortOrder) {
            mSortOrder = sortOrder;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AnnotationListSorter.class)) {
                return (T) new AnnotationListSorter(mSortOrder);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
