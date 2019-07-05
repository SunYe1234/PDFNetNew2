package com.pdftron.collab.ui.annotlist;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;

import com.pdftron.pdf.interfaces.builder.SkeletalFragmentBuilder;
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager;

/**
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class AnnotationListFragmentBuilder extends SkeletalFragmentBuilder<CollabAnnotationListFragment> {

    private String mDocId;
    private boolean mIsReadOnly = true;
    private boolean mIsRtl = false;
    private int mInitialSortOrder = -1;  // default sort order is CollabAnnotationListSortOrder.LAST_ACTIVITY

    private AnnotationListFragmentBuilder() {
        super();
    }

    /**
     * Create a {@link AnnotationListFragmentBuilder} with the specified document ID.
     *
     * @param documentId to specify the document referenced by the builder
     * @return builder with the specified document ID
     */
    public static AnnotationListFragmentBuilder withDoc(@NonNull String documentId) {
        AnnotationListFragmentBuilder builder = new AnnotationListFragmentBuilder();
        builder.mDocId = documentId;
        return builder;
    }

    @Override
    public CollabAnnotationListFragment build(@NonNull Context context) {
        return build(context, CollabAnnotationListFragment.class);
    }

    @Override
    public Bundle createBundle(@NonNull Context context) {
        Bundle args = CollabAnnotationListFragment.newBundle(mDocId);
        args.putBoolean(CollabAnnotationListFragment.BUNDLE_IS_READ_ONLY, mIsReadOnly);
        args.putBoolean(CollabAnnotationListFragment.BUNDLE_IS_RTL, mIsRtl);
        args.putInt(CollabAnnotationListFragment.BUNDLE_KEY_SORT_MODE, mInitialSortOrder);
        return null;
    }

    @Override
    public void checkArgs(@NonNull Context context) {
        if (mDocId == null) {
            throw new IllegalStateException("Must specify a valid document id, annot id, and user id");
        }
        if (mInitialSortOrder == -1) {
            mInitialSortOrder = PdfViewCtrlSettingsManager.getAnnotListSortOrder(context,
                    CollabAnnotationListSortOrder.LAST_ACTIVITY);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDocId);
        dest.writeByte(this.mIsReadOnly ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mIsRtl ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mInitialSortOrder);
    }

    /**
     * {@hide}
     */
    @SuppressWarnings("WeakerAccess")
    protected AnnotationListFragmentBuilder(Parcel in) {
        this.mDocId = in.readString();
        this.mIsReadOnly = in.readByte() != 0;
        this.mIsRtl = in.readByte() != 0;
        this.mInitialSortOrder = in.readInt();
    }

    public static final Creator<AnnotationListFragmentBuilder> CREATOR = new Creator<AnnotationListFragmentBuilder>() {
        @Override
        public AnnotationListFragmentBuilder createFromParcel(Parcel source) {
            return new AnnotationListFragmentBuilder(source);
        }

        @Override
        public AnnotationListFragmentBuilder[] newArray(int size) {
            return new AnnotationListFragmentBuilder[size];
        }
    };
}
