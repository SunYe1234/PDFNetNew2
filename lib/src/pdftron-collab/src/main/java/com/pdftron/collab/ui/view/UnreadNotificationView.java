package com.pdftron.collab.ui.view;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pdftron.collab.R;

/**
 * A circular notification icon that also shows the number of unread messages.
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class UnreadNotificationView extends FrameLayout {

    private TextView mTextView;
    private ImageView mImageView;

    public UnreadNotificationView(@NonNull Context context) {
        super(context);
        init();
    }

    public UnreadNotificationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UnreadNotificationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public UnreadNotificationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_unread_notification, this);

        mTextView = findViewById(R.id.unread_text);
        mImageView = findViewById(R.id.background_circle);
    }

    /**
     * Set the text for the notification view. A number is usually set.
     *
     * @param text to set in this view
     */
    public void setText(@Nullable String text) {
        mTextView.setText(text);
    }

    /**
     * Color of the circular notification icon.
     *
     * @param color of the icon
     */
    public void setNotificationColor(@ColorInt int color) {
        mImageView.setColorFilter(color);
    }

    /**
     * Color of the notification text in the view.
     *
     * @param color of the text
     */
    public void setTextColor(@ColorInt int color) {
        mTextView.setTextColor(color);
    }
}
