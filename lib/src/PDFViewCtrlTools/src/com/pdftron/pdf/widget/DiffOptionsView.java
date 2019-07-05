package com.pdftron.pdf.widget;

import android.content.Context;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.pdftron.pdf.GState;
import com.pdftron.pdf.controls.AdvancedColorView;
import com.pdftron.pdf.controls.ColorPickerView;
import com.pdftron.pdf.model.FileInfo;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.Utils;

import java.util.ArrayList;

/**
 * Diffing options view.
 */
public class DiffOptionsView extends LinearLayout implements
    CompoundButton.OnCheckedChangeListener,
    View.OnClickListener,
    ColorPickerView.OnColorChangeListener {

    public interface DiffOptionsViewListener {
        void onSelectFile(View which);

        void onCompareFiles(ArrayList<Uri> files);
    }

    private DiffOptionsViewListener mDiffOptionsViewListener;

    private Uri mFile1;
    private Uri mFile2;

    // first item
    private TextView mFilename1;
    private Button mSelectButton1;
    private SwitchCompat mAnnotSwitch1;
    private LinearLayout mColorLayout1;
    private ImageView mColor1;
    private ImageView mColorChevron1;
    private AdvancedColorView mColorPicker1;

    // second item
    private TextView mFilename2;
    private Button mSelectButton2;
    private SwitchCompat mAnnotSwitch2;
    private LinearLayout mColorLayout2;
    private ImageView mColor2;
    private ImageView mColorChevron2;
    private AdvancedColorView mColorPicker2;

    private Spinner mBlendSpinner;
    private Button mCompareButton;

    public DiffOptionsView(Context context) {
        this(context, null);
    }

    public DiffOptionsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DiffOptionsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_diff_options, this);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setOrientation(VERTICAL);

        mFilename1 = findViewById(R.id.diff_file_1);
        mSelectButton1 = findViewById(R.id.diff_select_file_1);
        mSelectButton1.setOnClickListener(this);
        mAnnotSwitch1 = findViewById(R.id.diff_annotation_switch_1);
        mAnnotSwitch1.setOnCheckedChangeListener(this);
        mColorLayout1 = findViewById(R.id.diff_color_layout_1);
        mColorLayout1.setOnClickListener(this);
        mColor1 = findViewById(R.id.diff_color_1);
        int color1 = getContext().getResources().getColor(R.color.diff_color_1);
        mColor1.getDrawable()
            .mutate()
            .setColorFilter(color1, PorterDuff.Mode.SRC_IN);
        mColorChevron1 = findViewById(R.id.diff_color_chevron_1);
        mColorPicker1 = findViewById(R.id.diff_color_picker_1);
        mColorPicker1.setSelectedColor(color1);
        mColorPicker1.setOnColorChangeListener(this);

        mFilename2 = findViewById(R.id.diff_file_2);
        mSelectButton2 = findViewById(R.id.diff_select_file_2);
        mSelectButton2.setOnClickListener(this);
        mAnnotSwitch2 = findViewById(R.id.diff_annotation_switch_2);
        mAnnotSwitch2.setOnCheckedChangeListener(this);
        mColorLayout2 = findViewById(R.id.diff_color_layout_2);
        mColorLayout2.setOnClickListener(this);
        mColor2 = findViewById(R.id.diff_color_2);
        int color2 = getContext().getResources().getColor(R.color.diff_color_2);
        mColor2.getDrawable()
            .mutate()
            .setColorFilter(color2, PorterDuff.Mode.SRC_IN);
        mColorChevron2 = findViewById(R.id.diff_color_chevron_2);
        mColorPicker2 = findViewById(R.id.diff_color_picker_2);
        mColorPicker2.setSelectedColor(color2);
        mColorPicker2.setOnColorChangeListener(this);

        mBlendSpinner = findViewById(R.id.diff_blend_spinner);
        ArrayAdapter<CharSequence> blendAdapter = ArrayAdapter.createFromResource(getContext(),
            R.array.diff_blend_array, android.R.layout.simple_spinner_item);
        blendAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBlendSpinner.setAdapter(blendAdapter);
        mBlendSpinner.setSelection(GState.e_bl_darken);

        mCompareButton = findViewById(R.id.diff_compare);
        mCompareButton.setOnClickListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        buttonView.setText(isChecked ? R.string.diff_annotations_on : R.string.diff_annotations_off);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mSelectButton1.getId() || v.getId() == mSelectButton2.getId()) {
            if (mDiffOptionsViewListener != null) {
                mDiffOptionsViewListener.onSelectFile(v);
            }
        } else if (v.getId() == mCompareButton.getId()) {
            if (mFile1 != null && mFile2 != null) {
                if (mDiffOptionsViewListener != null) {
                    ArrayList<Uri> files = new ArrayList<>(2);
                    files.add(mFile1);
                    files.add(mFile2);
                    mDiffOptionsViewListener.onCompareFiles(files);
                }
            } else {
                Utils.safeShowAlertDialog(v.getContext(), R.string.diff_select_file_title, R.string.diff_compare);
            }
        } else if (v.getId() == mColorLayout1.getId()) {
            TransitionManager.beginDelayedTransition(this);
            if (mColorPicker1.getVisibility() == View.GONE) {
                mColorPicker1.setVisibility(View.VISIBLE);
                mColorChevron1.setImageResource(R.drawable.ic_arrow_down_white_24dp);
            } else {
                mColorPicker1.setVisibility(View.GONE);
                mColorChevron1.setImageResource(R.drawable.ic_chevron_right_black_24dp);
            }
        } else if (v.getId() == mColorLayout2.getId()) {
            TransitionManager.beginDelayedTransition(this);
            if (mColorPicker2.getVisibility() == View.GONE) {
                mColorPicker2.setVisibility(View.VISIBLE);
                mColorChevron2.setImageResource(R.drawable.ic_arrow_down_white_24dp);
            } else {
                mColorPicker2.setVisibility(View.GONE);
                mColorChevron2.setImageResource(R.drawable.ic_chevron_right_black_24dp);
            }
        }
    }

    @Override
    public void OnColorChanged(View view, int color) {
        if (view.getId() == mColorPicker1.getId()) {
            mColor1.getDrawable()
                .mutate()
                .setColorFilter(color, PorterDuff.Mode.SRC_IN);
        } else if (view.getId() == mColorPicker2.getId()) {
            mColor2.getDrawable()
                .mutate()
                .setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }
    }

    public void setDiffOptionsViewListener(@NonNull DiffOptionsViewListener listener) {
        mDiffOptionsViewListener = listener;
    }

    public void setFiles(FileInfo fileInfo1, FileInfo fileInfo2) {
        if (fileInfo1 != null) {
            mFilename1.setText(fileInfo1.getName());
            mFile1 = Uri.parse(fileInfo1.getAbsolutePath());
        }
        if (fileInfo2 != null) {
            mFilename2.setText(fileInfo2.getName());
            mFile2 = Uri.parse(fileInfo2.getAbsolutePath());
        }
    }

    public void setSelectFileButtonVisibility(boolean visible) {
        mSelectButton1.setVisibility(visible ? View.VISIBLE : View.GONE);
        mSelectButton2.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setAnnotationToggleVisibility(boolean visible) {
        mAnnotSwitch1.setVisibility(visible ? View.VISIBLE : View.GONE);
        mAnnotSwitch2.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setColorOptionVisibility(boolean visible) {
        mColorLayout1.setVisibility(visible ? View.VISIBLE : View.GONE);
        mColorLayout2.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void setCompareButtonVisibility(boolean visible) {
        mCompareButton.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public void handleFileSelected(FileInfo fileInfo, View which) {
        if (fileInfo == null || which == null) {
            return;
        }
        if (which.getId() == mSelectButton1.getId()) {
            mFilename1.setText(fileInfo.getName());
            mFile1 = Uri.parse(fileInfo.getAbsolutePath());
        } else if (which.getId() == mSelectButton2.getId()) {
            mFilename2.setText(fileInfo.getName());
            mFile2 = Uri.parse(fileInfo.getAbsolutePath());
        }
    }

    public int getColor1() {
        return mColorPicker1.getColor();
    }

    public int getColor2() {
        return mColorPicker2.getColor();
    }

    public int getBlendMode() {
        return mBlendSpinner.getSelectedItemPosition();
    }
}
