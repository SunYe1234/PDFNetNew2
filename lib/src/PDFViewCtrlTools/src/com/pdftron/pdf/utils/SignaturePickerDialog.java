//---------------------------------------------------------------------------------------
// Copyright (c) 2001-2019 by PDFTron Systems Inc. All Rights Reserved.
// Consult legal.txt regarding legal and license information.
//---------------------------------------------------------------------------------------

package com.pdftron.pdf.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.v4.app.FragmentActivity;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.pdftron.pdf.config.ToolStyleConfig;
import com.pdftron.pdf.controls.AnnotStyleDialogFragment;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.FontResource;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.widget.SignatureView;

import java.util.LinkedList;

@Deprecated
public class SignaturePickerDialog extends AlertDialog {

    /**
     * Callback interface to be invoked when an interaction is needed.
     */
    public interface SignaturePickerDialogListener extends AnnotStyle.OnAnnotStyleChangeListener {
        /**
         * Called when signature has been done.
         *
         * @param addSignature True if should sign
         */
        void signatureDone(boolean addSignature);

        /**
         * Called when the popup window has been dismissed.
         *
         * @param popupWindow The popup window
         */
        void popupDismissed(AnnotStyleDialogFragment popupWindow);
    }

    private SignaturePickerDialogListener mListener;

    private Button mAddSignatureButton;
    private Button mClearButton;
    private ImageButton mStyleSignatureButton;
    private CheckBox mMakeDefaultCheckbox;

    private LinkedList<LinkedList<PointF>> mPathPoints;
    private RectF mBoundingBox;

    private SignatureView mSignatureView;

    public SignaturePickerDialog(final Context context, int color, float thickness) {
        super(context);
        init(context, color, thickness);
    }

    public SignaturePickerDialog(final Context context, int theme, int color, float thickness) {
        super(context, theme);
        init(context, color, thickness);
    }

    private void init(final Context context, int color, float thickness) {
        // Get size of the screen
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return;
        }
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        int viewWidth = (int) (point.x * 0.95f);
        int viewHeight = (int) (point.y * 0.95f);

        final View view = LayoutInflater.from(context).inflate(R.layout.tools_dialog_floating_signature, null);
        setView(view);

        final RelativeLayout mSignatureContainer = view.findViewById(R.id.tools_dialog_floating_sig_signature_view);
        mSignatureView = new SignatureView(context);
        mSignatureView.setup(color, thickness);
        mSignatureView.setSignatureViewListener(new SignatureView.SignatureViewListener() {
            @Override
            public void onTouchStart(float x, float y) {
                // Enable/disable buttons
                mAddSignatureButton.setEnabled(true);
                mClearButton.setEnabled(true);
            }

            @Override
            public void onError() {
                processSignature(false);
            }
        });
        mSignatureContainer.addView(mSignatureView);

        // "Make default" checkbox
        mMakeDefaultCheckbox = view.findViewById(R.id.tools_dialog_floating_sig_checkbox_default);

        // Clear button
        mClearButton = view.findViewById(R.id.tools_dialog_floating_sig_button_clear);
        mClearButton.setEnabled(false);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignatureView.eraseSignature();
                mAddSignatureButton.setEnabled(false);
                mClearButton.setEnabled(false);
            }
        });

        // Cancel button
        Button cancelButton = view.findViewById(R.id.tools_dialog_floating_sig_button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processSignature(false);
            }
        });

        // Add signature button
        mAddSignatureButton = view.findViewById(R.id.tools_dialog_floating_sig_button_add);
        mAddSignatureButton.setEnabled(false);
        mAddSignatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPathPoints = mSignatureView.getSignaturePaths();
                mBoundingBox = mSignatureView.getBoundingBox();
                processSignature(true);
            }
        });

        // Add style button
        mStyleSignatureButton = view.findViewById(R.id.tools_dialog_floating_sig_button_style);

        mStyleSignatureButton.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        mStyleSignatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // keep the background of the button blue while the popup is visible
                mStyleSignatureButton.setSelected(true);

                // create style popup window
                AnnotStyle annotStyle = ToolStyleConfig.getInstance().getCustomAnnotStyle(getContext(), AnnotStyle.CUSTOM_ANNOT_TYPE_SIGNATURE, "");
                // get current signature button on screen
                int[] pos = new int[2];
                mStyleSignatureButton.getLocationOnScreen(pos);

                Rect anchor = new Rect(pos[0], 0, pos[0] + mStyleSignatureButton.getWidth(), pos[1] + mStyleSignatureButton.getHeight());
                final AnnotStyleDialogFragment popupWindow = new AnnotStyleDialogFragment.Builder(annotStyle).setAnchorInScreen(anchor).build();

                // get location values to show the popup window in the correct position
                int[] loc_int_button_style = new int[2];
                int[] loc_int_button_clear = new int[2];
                int[] loc_int_dialog = new int[2];
                try {
                    mStyleSignatureButton.getLocationInWindow(loc_int_button_style);
                    mClearButton.getLocationInWindow(loc_int_button_clear);
                    view.getLocationInWindow(loc_int_dialog);
                } catch (Exception e) {
                    AnalyticsHandlerAdapter.getInstance().sendException(e);
                }

                try {
                    FragmentActivity activity = null;
                    if (getContext() instanceof FragmentActivity) {
                        activity = (FragmentActivity) getContext();
                    } else if (getOwnerActivity() instanceof FragmentActivity){
                        activity = (FragmentActivity) getOwnerActivity();
                    }
                    if (activity == null) {
                        AnalyticsHandlerAdapter.getInstance().sendException(new Exception("SignaturePickerDialog is not attached with an Activity"));
                        return;
                    }
                    popupWindow.show(activity.getSupportFragmentManager(),
                        AnalyticsHandlerAdapter.STYLE_PICKER_LOC_SIGNATURE,
                        AnalyticsHandlerAdapter.getInstance().getAnnotationTool(AnalyticsHandlerAdapter.ANNOTATION_TOOL_SIGNATURE));
                } catch (Exception ex) {
                    AnalyticsHandlerAdapter.getInstance().sendException(ex);
                }

                popupWindow.setOnAnnotStyleChangeListener(new AnnotStyle.OnAnnotStyleChangeListener() {
                    @Override
                    public void onChangeAnnotThickness(float thickness, boolean done) {
                        mSignatureView.updateStrokeThickness(thickness);
                    }

                    @Override
                    public void onChangeAnnotTextSize(float textSize, boolean done) {

                    }

                    @Override
                    public void onChangeAnnotTextColor(int textColor) {

                    }

                    @Override
                    public void onChangeAnnotOpacity(float opacity, boolean done) {

                    }

                    @Override
                    public void onChangeAnnotStrokeColor(int color) {
                        mStyleSignatureButton.getDrawable().mutate().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                        mSignatureView.updateStrokeColor(color);
                    }

                    @Override
                    public void onChangeAnnotFillColor(int color) {

                    }

                    @Override
                    public void onChangeAnnotIcon(String icon) {

                    }

                    @Override
                    public void onChangeAnnotFont(FontResource font) {

                    }

                    @Override
                    public void onChangeRulerProperty(RulerItem rulerItem) {

                    }

                    @Override
                    public void onChangeOverlayText(String overlayText) {

                    }

                    @Override
                    public void onChangeSnapping(boolean snap) {

                    }
                });
                popupWindow.setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        popupDismissed(popupWindow);
                        mStyleSignatureButton.setSelected(false);
                    }
                });
            }
        });

        // removes the border that surrounds the dialog on KitKat
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
    }

    public LinkedList<LinkedList<PointF>> getPaths() {
        return mPathPoints;
    }

    public RectF getSignatureBoundingBox() {
        return mBoundingBox;
    }

    public boolean shouldOverwriteOldSignature() {
        return mMakeDefaultCheckbox != null && mMakeDefaultCheckbox.isChecked();
    }

    public void setMakeDefaultCheckboxVisibility(int visibility) {
        if (mMakeDefaultCheckbox != null) {
            mMakeDefaultCheckbox.setVisibility(visibility);
        }
    }

    private void processSignature(boolean shouldSign) {
        if (mListener != null) {
            mListener.signatureDone(shouldSign);
        }

        dismiss();
    }

    private void popupDismissed(AnnotStyleDialogFragment popupWindow) {
        if (mListener != null) {
            mListener.popupDismissed(popupWindow);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateLayout();
    }

    @Override
    protected void onStop() {
        if (mSignatureView != null) {
            mSignatureView.clearResources();
        }
        super.onStop();
    }

    public void updateLayout() {
        Context context = getContext();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (wm != null) {
            Display display = wm.getDefaultDisplay();

            int navBarHeight = 0;
            int resId = context.getResources().getIdentifier("navigation_bar_height_landscape", "dimen", "android");
            if (resId > 0) {
                navBarHeight = context.getResources().getDimensionPixelOffset(resId);
            }
            Point point = new Point();
            display.getSize(point);
            int minSize = Math.min(point.x, point.y);
            int width = (int) (minSize * 0.90f);
            int height = (int) (minSize * 0.90f);
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);

            //noinspection StatementWithEmptyBody
            if (hasBackKey && hasHomeKey) {
                // no navigation bar, unless it is enabled in the settings, i.e. Samsung
            } else {
                // has navigation bar, i.e. Nexus
                height = height - navBarHeight;
                width = width - navBarHeight;
            }

            if (!Utils.isTablet(context) && Utils.isLandscape(context)) {
                width = (int) (point.x * 0.90f);
                height = (int) (point.y * 0.90f);
            }

            Window window = getWindow();
            if (window != null) {
                lp.copyFrom(window.getAttributes());
                lp.x = 0;
                lp.y = 0;
                lp.width = width;
                if (Utils.isTablet(context)) {
                    lp.height = Math.min((int) (width * 0.75f), height);
                } else {
                    lp.height = height;
                }
                window.setAttributes(lp);
            }
        }
    }

    public void setDialogListener(SignaturePickerDialogListener listener) {
        mListener = listener;
    }

}
