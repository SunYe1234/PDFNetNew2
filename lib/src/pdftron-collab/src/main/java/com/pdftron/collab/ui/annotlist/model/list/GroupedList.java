package com.pdftron.collab.ui.annotlist.model.list;

import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListContent;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListHeader;
import com.pdftron.collab.ui.annotlist.model.list.item.AnnotationListItem;
import com.pdftron.pdf.PDFViewCtrl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Data structure for a grouped list with headers for each grouping.
 *
 * @param <T> Data type for header info.
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class GroupedList<T extends AnnotationListHeader.HeaderData> extends AnnotationList {

    @NonNull
    private final List<AnnotationListGroup<T>> mAnnotGroups;

    public GroupedList(@NonNull AnnotationEntityMapper<T> adapter,
            @NonNull PDFViewCtrl mPdfViewCtrl,
            @NonNull GroupKey<T> groupKey) {
        mAnnotGroups = fromList(adapter.fromEntities(mPdfViewCtrl), groupKey, adapter);
    }

    List<AnnotationListGroup<T>> getGroups() {
        return mAnnotGroups;
    }

    @NonNull
    private List<AnnotationListGroup<T>> fromList(@NonNull List<AnnotationListContent> annotItems,
            @NonNull GroupKey<T> groupKey,
            @NonNull AnnotationEntityMapper<T> adapter) {
        // Group items
        HashMap<T, List<AnnotationListContent>> groupMap = new HashMap<>();
        for (AnnotationListContent item : annotItems) {
            T key = groupKey.getKey(item);
            if (!groupMap.containsKey(key)) {
                List<AnnotationListContent> group = new ArrayList<>();
                group.add(item);
                groupMap.put(key, group);
            } else {
                List<AnnotationListContent> group = groupMap.get(key);
                if (group != null) {
                    group.add(item);
                }
            }
        }

        // Create list of AnnotationListGroup from grouped items
        Collection<List<AnnotationListContent>> groupAnnots = groupMap.values();
        List<AnnotationListGroup<T>> result = new ArrayList<>();
        for (List<AnnotationListContent> group : groupAnnots) {
            if (group.size() != 0) {
                result.add(
                        new AnnotationListGroup<>(
                                adapter.getHeader(groupKey.getKey(group.get(0))),
                                group)
                );
            }
        }

        return result;
    }

    @Override
    public AnnotationListItem get(int position) {
        int runningIdx = 0;
        for (AnnotationListGroup group : mAnnotGroups) {
            if ((runningIdx + group.size() + 1) > position) {
                int relativePos = position - runningIdx - 1;
                if (relativePos < 0) {
                    return group.getHeader();
                } else {
                    return group.getItem(relativePos);
                }
            } else {
                runningIdx += group.size() + 1;
            }
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public int size() {
        int size = 0;
        for (AnnotationListGroup group : mAnnotGroups) {
            size += group.size();
            size++;
        }
        return size;
    }

    public GroupedList sort(@NonNull Comparator<AnnotationListGroup<T>> groupComparator,
            @NonNull Comparator<AnnotationListContent> itemComparator) {
        // Sort groups
        Collections.sort(mAnnotGroups, groupComparator);

        // Sort by element in each group
        for (AnnotationListGroup<T> group : mAnnotGroups) {
            Collections.sort(group.mAnnotItems, itemComparator);
        }
        return this;
    }

    public static class AnnotationListGroup<K extends AnnotationListHeader.HeaderData> {
        private final AnnotationListHeader<K> mKey;
        private final List<AnnotationListContent> mAnnotItems;

        AnnotationListGroup(AnnotationListHeader<K> header, @NonNull List<AnnotationListContent> annotItems) {
            mKey = header;
            mAnnotItems = annotItems;
        }

        AnnotationListContent getItem(int position) {
            return mAnnotItems.get(position);
        }

        int size() {
            return mAnnotItems.size();
        }

        public AnnotationListHeader<K> getHeader() {
            return mKey;
        }
    }

    public interface GroupKey<T> {
        T getKey(@NonNull AnnotationListContent item);
    }
}
