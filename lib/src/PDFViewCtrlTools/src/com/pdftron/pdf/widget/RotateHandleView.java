package com.pdftron.pdf.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.DrawingUtils;
import com.pdftron.pdf.utils.Utils;

public class RotateHandleView extends FloatingActionButton {

    public interface RotateHandleViewListener {
        void onDown(float rawX, float rawY);

        void onMove(float rawX, float rawY);

        void onUp(float rawX, float rawY, float x, float y);
    }

    private RotateHandleViewListener mListener;

    float mDX, mDY;

    public RotateHandleView(Context context) {
        this(context, null);
    }

    public RotateHandleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateHandleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        setColorFilter(ContextCompat.getColor(getContext(), R.color.tools_selection_control_point));
        ColorStateList tintList = AppCompatResources.getColorStateList(getContext(), R.color.tools_colors_white);
        setBackgroundTintList(tintList);
        setCustomSize((int)Utils.convDp2Pix(getContext(), DrawingUtils.sRotateHandleSize));
        setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.ic_rotate_left_black_24dp));
    }

    public void setListener(RotateHandleViewListener listener) {
        mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDX = getX() - event.getRawX();
                mDY = getY() - event.getRawY();

                if (mListener != null) {
                    mListener.onDown(event.getRawX(), event.getRawY());
                }
                break;
            case MotionEvent.ACTION_MOVE:
                animate()
                        .x(event.getRawX() + mDX)
                        .y(event.getRawY() + mDY)
                        .setDuration(0)
                        .start();

                if (mListener != null) {
                    mListener.onMove(event.getRawX(), event.getRawY());
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mListener != null) {
                    mListener.onUp(event.getRawX(), event.getRawY(), event.getX(), event.getY());
                }
                break;
            default:
                return false;
        }
        return true;
    }
}
