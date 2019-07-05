package com.pdftron.collab.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.RestrictTo;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;

/**
 * Custom Image Button that overlays a notification icon on top of the button if enabled.
 * {@hide}
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public class NotificationImageButton extends AppCompatImageButton {

    private float mDiameter;
    private float mX;
    private float mY;
    private Paint mCirclePaint;

    private boolean mIsNotificationVisible = false;

    public NotificationImageButton(Context context) {
        super(context);
        init();
    }

    public NotificationImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NotificationImageButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setColor(Color.RED); // default notification icon is red
        mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Account for padding
        float xpad = (float) (getPaddingLeft() + getPaddingRight());
        float ypad = (float) (getPaddingTop() + getPaddingBottom());

        float ww = (float) w - xpad;
        float hh = (float) h - ypad;

        mX = ww * 0.7f + getPaddingLeft();
        mY = hh * 0.3f + getPaddingTop();

        // Figure out how big we can make the pie.
        mDiameter = Math.min(ww, hh) / 5.0f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Only draw the notification icon if enabled
        if (mIsNotificationVisible) {
            canvas.drawCircle(mX, mY, mDiameter, mCirclePaint);
        }
    }

    public void setNotificationVisibility(boolean visible) {
        mIsNotificationVisible = visible;
        invalidate();
        requestLayout();
    }

    public void setNotificationColor(@ColorInt int color) {
        mCirclePaint.setColor(color);
        invalidate();
        requestLayout();
    }
}
