package com.pdftron.pdf.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.pdftron.pdf.Page;
import com.pdftron.pdf.config.ToolStyleConfig;
import com.pdftron.pdf.controls.AnnotStyleDialogFragment;
import com.pdftron.pdf.interfaces.OnCreateSignatureListener;
import com.pdftron.pdf.model.AnnotStyle;
import com.pdftron.pdf.model.FontResource;
import com.pdftron.pdf.model.RulerItem;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.AnalyticsHandlerAdapter;
import com.pdftron.pdf.utils.StampManager;
import com.pdftron.pdf.widget.signature.VariableWidthSignatureView;

import java.util.List;

public class CreateSignatureFragment extends Fragment {

    private final static String BUNDLE_COLOR = "bundle_color";
    private final static String BUNDLE_STROKE_WIDTH = "bundle_stroke_width";
    private final static String BUNDLE_SHOW_SIGNATURE_FROM_IMAGE = "bundle_signature_from_image";
    private final static String BUNDLE_PRESSURE_SENSITIVE = "bundle_pressure_sensitive";

    private OnCreateSignatureListener mOnCreateSignatureListener;
    private Toolbar mToolbar;
    private Button mClearButton;
    private ImageButton mStyleSignatureButton;

    private int mColor;
    private float mStrokeWidth;
    private boolean mShowSignatureFromImage;
    private boolean mIsPressureSensitive = true; // by default pressure sensitivity is enabled

    private VariableWidthSignatureView mSignatureView;

    @Deprecated
    public static CreateSignatureFragment newInstance(int color, float strokeWidth, boolean showSignatureFromImage) {
        return newInstance(color, strokeWidth, showSignatureFromImage, true);
    }

    public static CreateSignatureFragment newInstance(int color, float strokeWidth, boolean showSignatureFromImage,
            boolean isPressureSensitive) {
        CreateSignatureFragment fragment = new CreateSignatureFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(BUNDLE_COLOR, color);
        bundle.putFloat(BUNDLE_STROKE_WIDTH, strokeWidth);
        bundle.putBoolean(BUNDLE_SHOW_SIGNATURE_FROM_IMAGE, showSignatureFromImage);
        bundle.putBoolean(BUNDLE_PRESSURE_SENSITIVE, isPressureSensitive);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arg = getArguments();
        if (arg != null) {
            mColor = arg.getInt(BUNDLE_COLOR);
            mStrokeWidth = arg.getFloat(BUNDLE_STROKE_WIDTH);
            mShowSignatureFromImage = arg.getBoolean(BUNDLE_SHOW_SIGNATURE_FROM_IMAGE, true);
            mIsPressureSensitive = arg.getBoolean(BUNDLE_PRESSURE_SENSITIVE, mIsPressureSensitive);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tools_dialog_create_signature, container, false);
    }

    @Override
    public void onViewCreated(@NonNull final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RelativeLayout mSignatureContainer = view.findViewById(R.id.tools_dialog_floating_sig_signature_view);
        mSignatureView = new VariableWidthSignatureView(view.getContext(), null);
        mSignatureView.setPressureSensitivity(mIsPressureSensitive);
        mSignatureView.setColor(mColor);
        mSignatureView.setStrokeWidth(mStrokeWidth);
        mSignatureView.addListener(new VariableWidthSignatureView.InkListener() {
            @Override
            public void onInkStarted() {
                // Enable/disable buttons
                setClearButtonEnabled(true);
            }
        });
        mSignatureContainer.addView(mSignatureView);

        // Clear button
        mClearButton = view.findViewById(R.id.tools_dialog_floating_sig_button_clear);
        setClearButtonEnabled(false);
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignatureView.clear();
                setClearButtonEnabled(false);
            }
        });

        // image button
        ImageButton imageButton = view.findViewById(R.id.tools_dialog_floating_sig_button_image);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnCreateSignatureListener != null) {
                    mOnCreateSignatureListener.onSignatureFromImage(null, -1, null); // will be set in hosting signature fragment
                }
            }
        });
        if (mShowSignatureFromImage) {
            imageButton.setVisibility(View.VISIBLE);
        } else {
            imageButton.setVisibility(View.GONE);
        }

        // style button
        mStyleSignatureButton = view.findViewById(R.id.tools_dialog_floating_sig_button_style);

        mStyleSignatureButton.getDrawable().mutate().setColorFilter(mColor, PorterDuff.Mode.SRC_IN);
        mStyleSignatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // keep the background of the button blue while the popup is visible
                mStyleSignatureButton.setSelected(true);

                // create style popup window
                AnnotStyle annotStyle = ToolStyleConfig.getInstance().getCustomAnnotStyle(v.getContext(), AnnotStyle.CUSTOM_ANNOT_TYPE_SIGNATURE, "");
                // get current signature button on screen
                int[] pos = new int[2];
                mStyleSignatureButton.getLocationOnScreen(pos);

                Rect anchor = new Rect(pos[0], pos[1], pos[0] + mStyleSignatureButton.getWidth(), pos[1] + mStyleSignatureButton.getHeight());
                final AnnotStyleDialogFragment popupWindow =
                        new AnnotStyleDialogFragment.Builder(annotStyle)
                                .setAnchorInScreen(anchor)
                                .setShowPressureSensitivePreview(mIsPressureSensitive)
                                .build();

                try {
                    FragmentActivity activity = getActivity();
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
                        mSignatureView.setStrokeWidth(mStrokeWidth);
                        mStrokeWidth = thickness;
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
                        mSignatureView.setColor(color);
                        mColor = color;
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
                popupWindow.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        if (mOnCreateSignatureListener != null) {
                            mOnCreateSignatureListener.onAnnotStyleDialogFragmentDismissed(popupWindow);
                        }
                        mStyleSignatureButton.setSelected(false);
                    }
                });
            }
        });
    }

    private void setClearButtonEnabled(boolean enabled) {
        if (enabled) {
            mClearButton.setTextColor(mClearButton.getContext().getResources().getColor(R.color.tools_colors_white));
        } else {
            mClearButton.setTextColor(mClearButton.getContext().getResources().getColor(R.color.tab_unselected));
        }
    }

    /**
     * Sets the listener to {@link OnCreateSignatureListener}.
     *
     * @param listener The listener
     */
    public void setOnCreateSignatureListener(OnCreateSignatureListener listener) {
        mOnCreateSignatureListener = listener;
    }

    /**
     * Sets the main and cab toolbars.
     *
     * @param toolbar The toolbar with one action called Add
     */
    public void setToolbar(@NonNull Toolbar toolbar) {
        mToolbar = toolbar;
    }

    public void resetToolbar(final Context context) {
        if (mToolbar != null) {
            mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (mToolbar == null) {
                        return false;
                    }
                    if (item.getItemId() == R.id.controls_action_edit) {
                        createSignature(context);
                    }
                    return false;
                }
            });
        }
    }

    private void createSignature(Context context) {
        if (context == null) {
            return;
        }
        List<double[]> strokes = mSignatureView.getStrokes();
        if (strokes.isEmpty()) {
            return;
        }
        String signatureFilePath = StampManager.getInstance().getSignatureFilePath(context);
        Page page = StampManager.getInstance().createVariableThicknessSignature(signatureFilePath,
                mSignatureView.getBoundingBox(),
                strokes,
                mColor, mStrokeWidth * 2.0f); // use stroke x 2 just in case
        if (mOnCreateSignatureListener != null) {
            mOnCreateSignatureListener.onSignatureCreated(page != null ? signatureFilePath : null);
        }
    }
}
