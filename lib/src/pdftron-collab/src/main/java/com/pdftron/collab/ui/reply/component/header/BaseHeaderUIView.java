package com.pdftron.collab.ui.reply.component.header;

import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.view.ViewGroup;

import com.pdftron.collab.ui.base.component.BaseUIView;
import com.pdftron.collab.ui.reply.model.ReplyHeader;

/**
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public abstract class BaseHeaderUIView extends BaseUIView<HeaderEvent> {

    public BaseHeaderUIView(@NonNull ViewGroup parent) {
        super(parent);
    }

    protected void onCloseClicked(@Nullable ReplyHeader data) {
        if (mEventObservable != null) {
            mEventObservable.onNext(new HeaderEvent(HeaderEvent.Type.CLOSE_CLICKED, data));
        }
    }

    public void onListClicked(@Nullable ReplyHeader data) {
        if (mEventObservable != null) {
            mEventObservable.onNext(new HeaderEvent(HeaderEvent.Type.LIST_CLICKED, data));
        }
    }

    public abstract void setHeaderTitle(@NonNull String title);

    public abstract void showPreviewHeader();

    public abstract void hidePreviewHeader();

    public abstract void setAnnotationPreviewIcon(@DrawableRes int icon, @ColorInt int color, float opacity);

    public abstract void setAnnotationPreviewText(@NonNull String content);

    public abstract void setNotificationIcon(boolean hasUnreadReplies);
}
