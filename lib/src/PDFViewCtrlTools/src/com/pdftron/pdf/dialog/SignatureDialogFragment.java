package com.pdftron.pdf.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pdftron.pdf.adapter.SignatureFragmentAdapter;
import com.pdftron.pdf.controls.AnnotStyleDialogFragment;
import com.pdftron.pdf.controls.CustomSizeDialogFragment;
import com.pdftron.pdf.interfaces.OnCreateSignatureListener;
import com.pdftron.pdf.interfaces.OnDialogDismissListener;
import com.pdftron.pdf.interfaces.OnSavedSignatureListener;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.tools.Tool;
import com.pdftron.pdf.widget.CustomViewPager;

public class SignatureDialogFragment extends CustomSizeDialogFragment implements
    OnCreateSignatureListener,
    OnSavedSignatureListener {
    public final static String TAG = SignatureDialogFragment.class.getName();

    private final static String PREF_LAST_SELECTED_TAB_IN_SIGNATURE_DIALOG = "last_selected_tab_in_signature_dialog";
    private final static String BUNDLE_TARGET_POINT_X = "target_point_x";
    private final static String BUNDLE_TARGET_POINT_Y = "target_point_y";
    private final static String BUNDLE_TARGET_WIDGET = "target_widget";
    private final static String BUNDLE_TARGET_PAGE = "target_page";
    private final static String BUNDLE_COLOR = "bundle_color";
    private final static String BUNDLE_STROKE_WIDTH = "bundle_stroke_width";
    private final static String BUNDLE_SHOW_SAVED_SIGNATURES = "bundle_show_saved_signatures";
    private final static String BUNDLE_SHOW_SIGNATURE_FROM_IMAGE = "bundle_signature_from_image";
    private final static String BUNDLE_CONFIRM_BUTTON_STRING_RES = "bundle_confirm_button_string_res";
    private final static String BUNDLE_PRESSURE_SENSITIVE = "bundle_pressure_sensitive";

    private OnCreateSignatureListener mOnCreateSignatureListener;

    private PointF mTargetPointPage; // keep this in the fragment so that can retrieve it when the fragment is re-created
    private int mTargetPage;
    private Long mTargetWidget;
    private int mColor;
    private float mStrokeWidth;

    private CustomViewPager mViewPager;

    private boolean mShowSavedSignatures;
    private boolean mShowSignatureFromImage;
    private boolean mPressureSensitive = true;

    private int mConfirmBtnStrRes;

    private OnDialogDismissListener mOnDialogDismissListener;

    @Deprecated
    public static SignatureDialogFragment newInstance(@Nullable PointF targetPoint, int targetPage,
                                                      @Nullable Long targetWidget,
                                                      int color, float thickness) {
        return newInstance(targetPoint, targetPage, targetWidget, color, thickness,
            true, true, 0);
    }

    public static SignatureDialogFragment newInstance(@Nullable PointF targetPoint, int targetPage,
            @Nullable Long targetWidget,
            int color, float thickness,
            boolean showSavedSignatures, boolean showSignatureFromImage,
            int confirmBtnStrRes) {
        return newInstance(targetPoint, targetPage,
                targetWidget,
                color, thickness,
                showSavedSignatures, showSignatureFromImage,
                confirmBtnStrRes, true);
    }

    public static SignatureDialogFragment newInstance(@Nullable PointF targetPoint, int targetPage,
            @Nullable Long targetWidget,
            int color, float thickness,
            boolean showSavedSignatures, boolean showSignatureFromImage,
            int confirmBtnStrRes, boolean pressureSensitive) {
        SignatureDialogFragment fragment = new SignatureDialogFragment();
        Bundle bundle = new Bundle();
        if (targetPoint != null) {
            bundle.putFloat(BUNDLE_TARGET_POINT_X, targetPoint.x);
            bundle.putFloat(BUNDLE_TARGET_POINT_Y, targetPoint.y);
        }
        bundle.putInt(BUNDLE_TARGET_PAGE, targetPage);
        if (targetWidget != null) {
            bundle.putLong(BUNDLE_TARGET_WIDGET, targetWidget);
        }
        bundle.putInt(BUNDLE_COLOR, color);
        bundle.putFloat(BUNDLE_STROKE_WIDTH, thickness);
        bundle.putBoolean(BUNDLE_SHOW_SAVED_SIGNATURES, showSavedSignatures);
        bundle.putBoolean(BUNDLE_SHOW_SIGNATURE_FROM_IMAGE, showSignatureFromImage);
        bundle.putBoolean(BUNDLE_PRESSURE_SENSITIVE, pressureSensitive);
        if (confirmBtnStrRes != 0) {
            bundle.putInt(BUNDLE_CONFIRM_BUTTON_STRING_RES, confirmBtnStrRes);
        }
        fragment.setArguments(bundle);
        return fragment;
    }

    public SignatureDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            float x = args.getFloat(BUNDLE_TARGET_POINT_X, -1);
            float y = args.getFloat(BUNDLE_TARGET_POINT_Y, -1);
            if (x > 0 && y > 0) {
                mTargetPointPage = new PointF(x, y);
            }
            mTargetPage = args.getInt(BUNDLE_TARGET_PAGE, -1);
            mTargetWidget = args.getLong(BUNDLE_TARGET_WIDGET);
            mColor = args.getInt(BUNDLE_COLOR);
            mStrokeWidth = args.getFloat(BUNDLE_STROKE_WIDTH);
            mShowSavedSignatures = args.getBoolean(BUNDLE_SHOW_SAVED_SIGNATURES, true);
            mShowSignatureFromImage = args.getBoolean(BUNDLE_SHOW_SIGNATURE_FROM_IMAGE, true);
            mConfirmBtnStrRes = args.getInt(BUNDLE_CONFIRM_BUTTON_STRING_RES, R.string.add);
            mPressureSensitive = args.getBoolean(BUNDLE_PRESSURE_SENSITIVE, true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rubber_stamp_dialog, container);

        Toolbar toolbar = view.findViewById(R.id.stamp_dialog_toolbar);
        toolbar.setTitle(R.string.annot_signature_plural);
        toolbar.inflateMenu(R.menu.controls_fragment_edit_toolbar);
        Toolbar cabToolbar = view.findViewById(R.id.stamp_dialog_toolbar_cab);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mViewPager = view.findViewById(R.id.stamp_dialog_view_pager);
        SignatureFragmentAdapter viewPagerAdapter = new SignatureFragmentAdapter(getChildFragmentManager(),
                getString(R.string.saved), getString(R.string.create),
                toolbar, cabToolbar,
                mColor, mStrokeWidth,
                mShowSavedSignatures,
                mShowSignatureFromImage,
                mConfirmBtnStrRes,
                this,
                this,
                mPressureSensitive);
        mViewPager.setAdapter(viewPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.stamp_dialog_tab_layout);
        tabLayout.setupWithViewPager(mViewPager);

        if (mShowSavedSignatures) {
            SharedPreferences settings = Tool.getToolPreferences(view.getContext());
            int lastSelectedTab = settings.getInt(PREF_LAST_SELECTED_TAB_IN_SIGNATURE_DIALOG, 0);
            mViewPager.setCurrentItem(lastSelectedTab);
            updateViewPager(lastSelectedTab);
        } else {
            tabLayout.setVisibility(View.GONE);
            mViewPager.setSwippingEnabled(false);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Context context = getContext();
                if (context == null) {
                    return;
                }
                SharedPreferences settings = Tool.getToolPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putInt(PREF_LAST_SELECTED_TAB_IN_SIGNATURE_DIALOG, tab.getPosition());
                editor.apply();

                updateViewPager(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        return view;
    }

    private void updateViewPager(int selectedTab) {
        if (selectedTab == 1) {
            // add signature tab
            // disable swiping
            mViewPager.setSwippingEnabled(false);
        } else {
            // saved signature tab
            mViewPager.setSwippingEnabled(true);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDialogDismissListener != null) {
            mOnDialogDismissListener.onDialogDismiss();
        }
    }

    /**
     * Sets the listener to {@link OnDialogDismissListener}.
     *
     * @param listener The listener
     */
    public void setOnDialogDismissListener(OnDialogDismissListener listener) {
        mOnDialogDismissListener = listener;
    }

    public void setOnCreateSignatureListener(OnCreateSignatureListener listener) {
        mOnCreateSignatureListener = listener;
    }

    @Override
    public void onSignatureCreated(@Nullable String filepath) {
        if (filepath != null) {
            onSignatureSelected(filepath);
        }
    }

    @Override
    public void onSignatureFromImage(@Nullable PointF targetPoint, int targetPage, @Nullable Long widget) {
        if (mOnCreateSignatureListener != null) {
            mOnCreateSignatureListener.onSignatureFromImage(mTargetPointPage, mTargetPage, mTargetWidget);
        }
        dismiss();
    }

    @Override
    public void onAnnotStyleDialogFragmentDismissed(AnnotStyleDialogFragment styleDialog) {
        if (mOnCreateSignatureListener != null) {
            mOnCreateSignatureListener.onAnnotStyleDialogFragmentDismissed(styleDialog);
        }
    }

    @Override
    public void onSignatureSelected(@NonNull String filepath) {
        if (mOnCreateSignatureListener != null) {
            mOnCreateSignatureListener.onSignatureCreated(filepath);
        }
        dismiss();
    }

    @Override
    public void onCreateSignatureClicked() {
        mViewPager.setCurrentItem(1);
    }
}
