//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.webkit.WebView;

import com.pdftron.pdf.controls.ReflowControl;

/**
 * WebView for Reflow.
 */
public class ReflowWebView extends WebView {

    private ScaleGestureDetector mScaleGestureDetector;
    private GestureDetector mGestureDetector;

    // for vertical scrolling
    private int mOrientation;
    private float mFlingThreshSpeed;
    private boolean mPageTop;
    private boolean mPageBottom;
    private boolean mDone;

    /**
     * Callback interface to be invoked when a gesture occurs.
     */
    public interface ReflowWebViewCallback {

        /**
         * Called when a scale gesture begins.
         *
         * @param detector The {@link ScaleGestureDetector}
         * @return True if handled
         */
        boolean onReflowWebViewScaleBegin(WebView webView, ScaleGestureDetector detector);

        /**
         * Called when user scales.
         *
         * @param detector The {@link ScaleGestureDetector}
         * @return True if handled
         */
        boolean onReflowWebViewScale(WebView webView, ScaleGestureDetector detector);

        /**
         * Called when a scale gesture ends.
         *
         * @param detector The {@link ScaleGestureDetector}
         */
        void onReflowWebViewScaleEnd(WebView webView, ScaleGestureDetector detector);

        /**
         * Called when a tap occurs with the up event.
         *
         * @param event The {@link MotionEvent}
         */
        void onReflowWebViewSingleTapUp(WebView webView, MotionEvent event);

        /**
         * Called when the top of this WebView is reached.
         */
        void onPageTop(WebView webView);

        /**
         * Called when the bottom of this WebView is reached.
         */
        void onPageBottom(WebView webView);
    }

    private ReflowWebViewCallback mCallback;

    /**
     * Sets the {@link ReflowWebViewCallback} listener
     *
     * @param listener The listener
     */
    public void setListener(ReflowWebViewCallback listener) {
        mCallback = listener;
    }

    /**
     * Class constructor
     */
    public ReflowWebView(Context context) {
        super(context);
        init(context);
    }

    /**
     * Class constructor
     */
    public ReflowWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
        mGestureDetector = new GestureDetector(getContext(), new TapListener());

        mFlingThreshSpeed = Utils.convDp2Pix(context, 1000);
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
        if (mGestureDetector != null) {
            mGestureDetector.onTouchEvent(ev);
        }
        if (mScaleGestureDetector != null) {
            mScaleGestureDetector.onTouchEvent(ev);
        }

        return true;
    }

    private void detectPageEnds() {
        if (mOrientation != ReflowControl.VERTICAL) {
            return;
        }
        mPageTop = false;
        mPageBottom = false;
        if (this.computeVerticalScrollRange() <= (this.computeVerticalScrollOffset() +
                this.computeVerticalScrollExtent())) {
            mPageBottom = true;
        }
        if (getScrollY() == 0) {
            mPageTop = true;
        }
    }

    private void onPageBottom() {
        if (mDone) {
            return;
        }
        if (mCallback != null) {
            mCallback.onPageBottom(this);
        }
        mDone = true;
    }

    private void onPageTop() {
        if (mDone) {
            return;
        }
        if (mCallback != null) {
            mCallback.onPageTop(this);
        }
        mDone = true;
    }

    private class TapListener implements GestureDetector.OnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent event) {
            if (mCallback != null) {
                mCallback.onReflowWebViewSingleTapUp(ReflowWebView.this, event);
            }
            return true;
        }

        @Override
        public boolean onDown(MotionEvent event) {
            mDone = false;
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {
            if (mOrientation == ReflowControl.VERTICAL) {
                detectPageEnds();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                float velocityX, float velocityY) {
            if (mOrientation == ReflowControl.VERTICAL) {
                if (Math.abs(velocityY) > mFlingThreshSpeed) {
                    if (velocityY < 0) {
                        if (mPageBottom) {
                            onPageBottom();
                        }
                    } else {
                        if (mPageTop) {
                            onPageTop();
                        }
                    }
                }
                detectPageEnds();
            }
            return true;
        }

        @Override
        public void onShowPress(MotionEvent event) {
        }

        @Override
        public void onLongPress(MotionEvent event) {
        }
    }

    private class ScaleListener
            extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return mCallback == null || mCallback.onReflowWebViewScaleBegin(ReflowWebView.this, detector);
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return mCallback == null || mCallback.onReflowWebViewScale(ReflowWebView.this, detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (mCallback != null) {
                mCallback.onReflowWebViewScaleEnd(ReflowWebView.this, detector);
            }
        }
    }
}
