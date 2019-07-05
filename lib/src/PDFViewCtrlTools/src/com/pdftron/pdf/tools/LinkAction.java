//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.tools;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.view.MotionEvent;

import com.pdftron.pdf.Action;
import com.pdftron.pdf.ActionParameter;
import com.pdftron.pdf.Annot;
import com.pdftron.pdf.PDFViewCtrl;
import com.pdftron.pdf.QuadPoint;
import com.pdftron.pdf.annots.Link;
import com.pdftron.pdf.tools.ToolManager.ToolMode;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.AnnotUtils;

/**
 * A tool for handling single tap on {@link Link} annotation
 */
@Keep
public class LinkAction extends Tool {

    private Link mLink;
    private Paint mPaint;

    /**
     * Class constructor
     */
    public LinkAction(@NonNull PDFViewCtrl ctrl) {
        super(ctrl);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    /**
     * The overload implementation of {@link Tool#getToolMode()}.
     */
    @Override
    public ToolManager.ToolModeBase getToolMode() {
        return ToolMode.LINK_ACTION;
    }

    @Override
    public int getCreateAnnotType() {
        return Annot.e_Unknown;
    }

    /**
     * The overload implementation of {@link Tool#onSingleTapConfirmed(MotionEvent)}.
     */
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (mAnnot != null) {
            mNextToolMode = ToolMode.LINK_ACTION;
            boolean shouldUnlockRead = false;
            try {
                mPdfViewCtrl.docLockRead();
                shouldUnlockRead = true;
                mLink = new Link(mAnnot);
                mPdfViewCtrl.invalidate();
            } catch (Exception ex) {
                AnalyticsHandlerAdapter.getInstance().sendException(ex);
            } finally {
                if (shouldUnlockRead) {
                    mPdfViewCtrl.docUnlockRead();
                }
            }
        } else {
            mNextToolMode = ToolMode.PAN;
        }
        return false;
    }

    private boolean handleLink() {
        if (onInterceptAnnotationHandling(mAnnot)) {
            return true;
        }
        if (mLink == null) {
            return false;
        }

        mNextToolMode = mCurrentDefaultToolMode;

        Action a = null;
        boolean shouldUnlockRead = false;
        try {
            mPdfViewCtrl.docLockRead();
            shouldUnlockRead = true;
            a = mLink.getAction();
        } catch (Exception e) {
            AnalyticsHandlerAdapter.getInstance().sendException(e);
        } finally {
            if (shouldUnlockRead) {
                mPdfViewCtrl.docUnlockRead();
            }
        }
        if (a != null) {
            shouldUnlockRead = false;
            boolean shouldUnlock = false;
            boolean hasChanges = false;
            try {
                if (a.needsWriteLock()) {
                    mPdfViewCtrl.docLock(true);
                    shouldUnlock = true;
                } else {
                    mPdfViewCtrl.docLockRead();
                    shouldUnlockRead = true;
                }
                ActionParameter action_param = new ActionParameter(a);
                executeAction(action_param);
                mPdfViewCtrl.invalidate();  // Draw away the highlight.
                hasChanges = mPdfViewCtrl.getDoc().hasChangesSinceSnapshot();
            } catch (Exception ex) {
                AnalyticsHandlerAdapter.getInstance().sendException(ex);
            } finally {
                if (shouldUnlock) {
                    mPdfViewCtrl.docUnlock();
                }
                if (shouldUnlockRead) {
                    mPdfViewCtrl.docUnlockRead();
                }
                if (hasChanges) {
                    raiseAnnotationActionEvent();
                }
            }
        }
        return true;
    }

    /**
     * The overload implementation of {@link Tool#onPostSingleTapConfirmed()}.
     */
    @Override
    public void onPostSingleTapConfirmed() {
        handleLink();
    }

    /**
     * The overload implementation of {@link Tool#onLongPress(MotionEvent)}.
     */
    @Override
    public boolean onLongPress(MotionEvent e) {
        if (mAnnot != null) {
            mNextToolMode = ToolMode.LINK_ACTION;
            boolean shouldUnlockRead = false;
            try {
                mPdfViewCtrl.docLockRead();
                shouldUnlockRead = true;
                mLink = new Link(mAnnot);
                mPdfViewCtrl.invalidate();
            } catch (Exception ex) {
                AnalyticsHandlerAdapter.getInstance().sendException(ex);
            } finally {
                if (shouldUnlockRead) {
                    mPdfViewCtrl.docUnlockRead();
                }
            }
        } else {
            mNextToolMode = ToolMode.PAN;
        }

        mAvoidLongPressAttempt = true;

        handleLink();

        return false;
    }

    /**
     * The overload implementation of {@link Tool#onDraw(Canvas, Matrix)}.
     */
    @Override
    public void onDraw(Canvas canvas, Matrix tfm) {
        try {
            int qn = mLink.getQuadPointCount();
            float sx = mPdfViewCtrl.getScrollX();
            float sy = mPdfViewCtrl.getScrollY();
            for (int i = 0; i < qn; ++i) {
                QuadPoint qp = mLink.getQuadPoint(i);
                com.pdftron.pdf.Rect quadRect = AnnotUtils.quadToRect(qp);
                double[] pts1 = mPdfViewCtrl.convPagePtToScreenPt(quadRect.getX1(), quadRect.getY1(), mAnnotPageNum);
                double[] pts2 = mPdfViewCtrl.convPagePtToScreenPt(quadRect.getX2(), quadRect.getY2(), mAnnotPageNum);
                double minX = Math.min(pts1[0], pts2[0]);
                double minY = Math.min(pts1[1], pts2[1]);
                double maxX = Math.max(pts1[0], pts2[0]);
                double maxY = Math.max(pts1[1], pts2[1]);

                float left = (float) minX + sx;
                float top = (float) minY + sy;
                float right = (float) maxX + sx;
                float bottom = (float) maxY + sy;

                mPaint.setStyle(Paint.Style.FILL);
                mPaint.setColor(mPdfViewCtrl.getResources().getColor(R.color.tools_link_fill));
                canvas.drawRect(left, top, right, bottom, mPaint);

                float len = Math.min(right - left, bottom - top);
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeWidth(Math.max(len / 15, 2));
                mPaint.setColor(mPdfViewCtrl.getResources().getColor(R.color.tools_link_stroke));
                canvas.drawRect(left, top, right, bottom, mPaint);
            }
        } catch (Exception ignored) {

        }
    }
}
