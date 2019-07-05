package com.pdftron.collab.ui.annotlist;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.content.Context;
import android.support.annotation.NonNull;

import com.pdftron.collab.R;
import com.pdftron.collab.db.entity.AnnotationEntity;
import com.pdftron.collab.ui.annotlist.model.list.AnnotationEntityMapper;
import com.pdftron.collab.ui.annotlist.model.list.AnnotationList;
import com.pdftron.collab.ui.annotlist.model.list.GroupedList;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.ui.annotlist.model.list.item.DayDate;
import com.pdftron.collab.ui.annotlist.model.list.item.PageNumber;
import com.pdftron.collab.utils.date.AnnotationListDateFormat;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.dialog.annotlist.AnnotationListSorter;
import com.pdftron.pdf.dialog.annotlist.BaseAnnotationListSorter;
import com.pdftron.pdf.dialog.annotlist.BaseAnnotationSortOrder;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * A {@link ViewModel} that stores and updates the current annotation list sort order. Is
 * responsible for returning the comparator for the current sort order, and also
 */
public class CollabAnnotationListSorter extends BaseAnnotationListSorter<AnnotationListContent> {

    public final static String TAG = CollabAnnotationListSorter.class.getName();

    public CollabAnnotationListSorter(@NonNull BaseAnnotationSortOrder sortOrder) {
        super(sortOrder);
    }

    @NonNull
    @Override
    public Comparator<AnnotationListContent> getComparator() {
        BaseAnnotationSortOrder value = mSortOrder.getValue();
        if (value != null) {
            if (value instanceof CollabAnnotationListSortOrder) {
                switch ((CollabAnnotationListSortOrder) value) {
                    case DATE_DESCENDING:
                        return (o1, o2) -> -1 * AnnotationListSorter.compareDate(o1, o2);
                    case POSITION_ASCENDING:
                        return AnnotationListSorter::compareYPosition;
                    case LAST_ACTIVITY:
                        return CollabAnnotationListSorter::compareLastActivity;
                }
            }
        }
        return AnnotationListSorter::compareDate; // default we sort by descending date
    }

    public AnnotationList getAnnotationList(
            @NonNull Context context,
            @NonNull PDFViewCtrl mPdfViewCtrl,
            @NonNull List<AnnotationEntity> list) {
        BaseAnnotationSortOrder value = mSortOrder.getValue();
        if (value != null) {
            if (value instanceof CollabAnnotationListSortOrder) {
                switch ((CollabAnnotationListSortOrder) value) {
                    case POSITION_ASCENDING: {
                        return new GroupedList<>(
                                new AnnotationEntityMapper<>(list),
                                mPdfViewCtrl,
                                item -> {
                                    String pagePrefix = context.getResources().getString(R.string.page_number_item);
                                    return new PageNumber(item.getPageNum(),
                                            pagePrefix);
                                }) // key will be page
                                .sort(
                                        CollabAnnotationListSorter::compareHeaderPage,
                                        AnnotationListSorter::compareYPosition
                                );
                    }
                    case LAST_ACTIVITY: {
                        AnnotationListDateFormat dateFormat = AnnotationListDateFormat.newInstance(context);
                        return new GroupedList<>(
                                new AnnotationEntityMapper<>(list),
                                mPdfViewCtrl,
                                item -> new DayDate(
                                        item.getLastReplyDate() == null ? item.getCreationDate() : item.getLastReplyDate(),
                                        dateFormat)) // key will be either reply date or creation date
                                .sort(
                                        CollabAnnotationListSorter::compareHeaderDate,
                                        CollabAnnotationListSorter::compareLastActivity
                                );
                    }
                    case DATE_DESCENDING: {
                        AnnotationListDateFormat dateFormat = AnnotationListDateFormat.newInstance(context);
                        return new GroupedList<>(
                                new AnnotationEntityMapper<>(list),
                                mPdfViewCtrl,
                                item -> new DayDate(
                                        item.getCreationDate(),
                                        dateFormat)) // key will be creation date
                                .sort(
                                        CollabAnnotationListSorter::compareHeaderDate,
                                        (o1, o2) -> AnnotationListSorter.compareDate(o1, o2) * -1
                                );
                    }
                }
            }
        }
        AnnotationListDateFormat dateFormat = AnnotationListDateFormat.newInstance(context);
        return new GroupedList<>(
                new AnnotationEntityMapper<>(list),
                mPdfViewCtrl,
                item -> new DayDate(
                        item.getLastReplyDate() == null ? item.getCreationDate() : item.getLastReplyDate(),
                        dateFormat)) // key will be either reply date or creation date
                .sort(
                        CollabAnnotationListSorter::compareHeaderDate,
                        CollabAnnotationListSorter::compareLastActivity
                );
    }

    /**
     * Helper methods to compare two list annotation items
     */
    private static int compareLastActivity(@NonNull AnnotationListContent thisObj,
            @NonNull AnnotationListContent thatObj) {
        Date thisDate = thisObj.getLastReplyDate() == null ? thisObj.getCreationDate() : thisObj.getLastReplyDate();
        Date thatDate = thatObj.getLastReplyDate() == null ? thatObj.getCreationDate() : thatObj.getLastReplyDate();
        return thatDate.compareTo(thisDate); // note ordering
    }


    /**
     * Helper methods to compare two list header items
     */
    private static int compareHeaderDate(@NonNull GroupedList.AnnotationListGroup<DayDate> o1,
            @NonNull GroupedList.AnnotationListGroup<DayDate> o2) {
        return o1.getHeader().getHeaderKey().compareTo(o2.getHeader().getHeaderKey());
    }

    private static int compareHeaderPage(@NonNull GroupedList.AnnotationListGroup<PageNumber> o1,
            @NonNull GroupedList.AnnotationListGroup<PageNumber> o2) {
        return o1.getHeader().getHeaderKey().compareTo(o2.getHeader().getHeaderKey());
    }

    /**
     *  Factory for creating {@link CollabAnnotationListSorter} depending on sort order.
     */
    public static class Factory implements ViewModelProvider.Factory {
        private BaseAnnotationSortOrder mSortOrder;

        public Factory(@NonNull BaseAnnotationSortOrder sortOrder) {
            mSortOrder = sortOrder;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CollabAnnotationListSorter.class)) {
                return (T) new CollabAnnotationListSorter(mSortOrder);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
