package com.pdftron.collab.ui.reply.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;

import com.pdftron.collab.R;
import com.pdftron.collab.ui.reply.bottomsheet.view.AvatarAdapter;
import com.pdftron.collab.ui.reply.bottomsheet.view.InitialsAvatarAdapter;
import com.pdftron.pdf.interfaces.builder.SkeletalFragmentBuilder;

/**
 * Builder to create a {@link ReplyFragment}.
 */
public class ReplyFragmentBuilder extends SkeletalFragmentBuilder<ReplyFragment> {
    private String mDocumentId;
    private String mAnnotationId;
    private String mUserId;
    @StyleRes
    private int mReplyTheme;
    private AvatarAdapter mAvatarAdapter;

    private ReplyFragmentBuilder() {
    }

    /**
     * Required method called to set {@link ReplyFragmentBuilder} with a specified {@link AvatarAdapter}.
     *
     * @param docId   that will be used to instantiate the {@link ReplyFragment}
     * @param annotId that will be used to instantiate the {@link ReplyFragment}
     * @param userId  that will be used to interact with the document
     * @return this builder with the specified ids.
     */
    public static ReplyFragmentBuilder withAnnot(@NonNull String docId, @NonNull String annotId,
            @NonNull String userId) {
        ReplyFragmentBuilder builder = new ReplyFragmentBuilder();
        builder.mDocumentId = docId;
        builder.mAnnotationId = annotId;
        builder.mUserId = userId;
        return builder;
    }

    /**
     * Defines the theme that will be used by the {@link ReplyFragment}. The theme
     * must extend com.pdftron.collab.R.style.ReplyBaseTheme.DayNight.
     *
     * @param replyTheme style resource that extends ReplyBaseStyle
     * @return this builder with the specified theme to create {@link ReplyFragment}.
     */
    public ReplyFragmentBuilder usingTheme(@StyleRes int replyTheme) {
        mReplyTheme = replyTheme;
        return this;
    }

    /**
     * Optional method used to set {@link ReplyFragmentBuilder} with a specified {@link AvatarAdapter}.
     *
     * @param adapter for inflating and binding the reply message avatar
     * @return this builder with the specified AvatarAdapter to create {@link ReplyFragment}.
     */
    public ReplyFragmentBuilder usingAdapter(@Nullable AvatarAdapter adapter) {
        mAvatarAdapter = adapter;
        return this;
    }

    /**
     * Transform this builder into a {@link BottomSheetReplyFragmentBuilder}.
     *
     * @return a {@link BottomSheetReplyFragmentBuilder} with same settings as this builder.
     */
    public BottomSheetReplyFragmentBuilder asBottomSheet() {
        return new BottomSheetReplyFragmentBuilder(this);
    }

    @Override
    public ReplyFragment build(@NonNull Context context) {
        return build(context, ReplyFragment.class);
    }

    @Override
    public Bundle createBundle(@NonNull Context context) {
        Bundle args = new Bundle();
        args.putString(ReplyFragment.BUNDLE_ANNOTATION_ID_KEY, mAnnotationId);
        args.putString(ReplyFragment.BUNDLE_DOCUMENT_ID_KEY, mDocumentId);
        args.putString(ReplyFragment.BUNDLE_USER_ID_KEY, mUserId);
        args.putInt(ReplyFragment.BUNDLE_THEME_ID_KEY, mReplyTheme);
        args.putParcelable(ReplyFragment.BUNDLE_AVATAR_ADAPTER_KEY, mAvatarAdapter);
        return args;
    }

    @Override
    public void checkArgs(@NonNull Context context) {
        // These are required, should not be able to create the fragment without these.
        if (mAnnotationId == null || mDocumentId == null || mUserId == null) {
            throw new IllegalStateException("Must specify a valid document id, annot id, and user id");
        }
        // Use the default if not set.
        mAvatarAdapter = mAvatarAdapter == null ? new InitialsAvatarAdapter() : mAvatarAdapter;
        mReplyTheme = mReplyTheme == 0 ? R.style.ReplyBaseTheme_DayNight : mReplyTheme;
    }

    String getDocumentId() {
        return mDocumentId;
    }

    String getAnnotationId() {
        return mAnnotationId;
    }

    String getUserId() {
        return mUserId;
    }

    int getReplyTheme() {
        return mReplyTheme;
    }

    AvatarAdapter getAvatarAdapter() {
        return mAvatarAdapter;
    }

    //Parcelable methods

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mDocumentId);
        dest.writeString(this.mAnnotationId);
        dest.writeString(this.mUserId);
        dest.writeInt(this.mReplyTheme);
        dest.writeParcelable(this.mAvatarAdapter, flags);
    }

    /**
     * {@hide}
     */
    @SuppressWarnings("WeakerAccess")
    protected ReplyFragmentBuilder(Parcel in) {
        this.mDocumentId = in.readString();
        this.mAnnotationId = in.readString();
        this.mUserId = in.readString();
        this.mReplyTheme = in.readInt();
        this.mAvatarAdapter = in.readParcelable(AvatarAdapter.class.getClassLoader());
    }

    public static final Creator<ReplyFragmentBuilder> CREATOR = new Creator<ReplyFragmentBuilder>() {

        @Override
        public ReplyFragmentBuilder createFromParcel(Parcel source) {
            return new ReplyFragmentBuilder(source);
        }

        @Override
        public ReplyFragmentBuilder[] newArray(int size) {
            return new ReplyFragmentBuilder[size];
        }
    };
}
