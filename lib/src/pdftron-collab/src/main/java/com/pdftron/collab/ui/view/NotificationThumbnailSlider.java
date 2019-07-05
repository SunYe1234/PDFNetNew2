package com.pdftron.collab.ui.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.pdftron.collab.R;
import com.pdftron.pdf.controls.ThumbnailSlider;

/**
 * A {@link ThumbnailSlider} that has a custom notification icon for the annotation list.
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public final class NotificationThumbnailSlider extends ThumbnailSlider {

    private NotificationImageButton mRightItemImageBtn;

    public NotificationThumbnailSlider(Context context) {
        super(context);
        init();
    }

    public NotificationThumbnailSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NotificationThumbnailSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public NotificationThumbnailSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    protected void inflateLayout(@NonNull Context context) {
        LayoutInflater.from(context).inflate(R.layout.controls_notification_thumbnail_slider, this);
    }

    private void init() {
        mRightItemImageBtn = findViewById(R.id.controls_thumbnail_slider_right_menu_button);
    }

    /**
     * Whether or not to show the notification dot.
     *
     * @param isShown is true if dot should be is visible, false otherwise.
     */
    public void setIsNotificationShown(boolean isShown) {
        mRightItemImageBtn.setNotificationVisibility(isShown);
    }
}
