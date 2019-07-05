package com.pdftron.collab.ui.reply.bottomsheet;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.support.annotation.NonNull;

import com.pdftron.pdf.interfaces.builder.SkeletalFragmentBuilder;

/**
 * Builder to create a {@link BottomSheetReplyFragment}.
 */
public class BottomSheetReplyFragmentBuilder extends SkeletalFragmentBuilder<BottomSheetReplyFragment> {

    private final ReplyFragmentBuilder mReplyFragmentBuilder;

    BottomSheetReplyFragmentBuilder(@NonNull ReplyFragmentBuilder replyFragmentBuilder) {
        mReplyFragmentBuilder = replyFragmentBuilder;
    }

    @Override
    public BottomSheetReplyFragment build(@NonNull Context context) {
        return build(context, BottomSheetReplyFragment.class);
    }

    @Override
    public Bundle createBundle(@NonNull Context context) {
        Bundle args = new Bundle();
        args.putString(ReplyFragment.BUNDLE_ANNOTATION_ID_KEY, mReplyFragmentBuilder.getAnnotationId());
        args.putString(ReplyFragment.BUNDLE_DOCUMENT_ID_KEY, mReplyFragmentBuilder.getDocumentId());
        args.putString(ReplyFragment.BUNDLE_USER_ID_KEY, mReplyFragmentBuilder.getUserId());
        args.putInt(ReplyFragment.BUNDLE_THEME_ID_KEY, mReplyFragmentBuilder.getReplyTheme());
        args.putParcelable(ReplyFragment.BUNDLE_AVATAR_ADAPTER_KEY, mReplyFragmentBuilder.getAvatarAdapter());
        return args;
    }

    @Override
    public void checkArgs(@NonNull Context context) {
        mReplyFragmentBuilder.checkArgs(context);
    }

    // Parcelable methods

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mReplyFragmentBuilder, flags);
    }

    /**
     * {@hide}
     */
    @SuppressWarnings("WeakerAccess")
    protected BottomSheetReplyFragmentBuilder(Parcel in) {
        this.mReplyFragmentBuilder = in.readParcelable(ReplyFragmentBuilder.class.getClassLoader());
    }

    public static final Creator<BottomSheetReplyFragmentBuilder> CREATOR = new Creator<BottomSheetReplyFragmentBuilder>() {

        @Override
        public BottomSheetReplyFragmentBuilder createFromParcel(Parcel source) {
            return new BottomSheetReplyFragmentBuilder(source);
        }

        @Override
        public BottomSheetReplyFragmentBuilder[] newArray(int size) {
            return new BottomSheetReplyFragmentBuilder[size];
        }
    };
}
