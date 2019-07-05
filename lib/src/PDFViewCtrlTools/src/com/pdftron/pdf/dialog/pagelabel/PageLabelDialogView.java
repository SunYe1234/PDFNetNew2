package com.pdftron.pdf.dialog.pagelabel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.pdftron.pdf.tools.R;

class PageLabelDialogView extends PageLabelView {

    private RadioButton mPageAll;
    private RadioButton mPageSelected;
    private RadioButton mPageRange;
    private EditText mFromPageEditTxt;
    private EditText mToPageEditTxt;
    private Spinner mStyleSpinner;
    private EditText mPrefixEditTxt;
    private EditText mStartNumEditTxt;
    private TextView mMaxPage;
    private TextView mPreview;

    private String mSelectedPageStr;
    private String mMaxPageStr;
    private String mPreviewTextStr;
    private String mInvalidStartNumStr;
    private String mInvalidPageRange;

    PageLabelDialogView(@NonNull ViewGroup parent,
                        @NonNull PageLabelSettingChangeListener listener) {
        super(parent, listener);
        Context context = parent.getContext();

        // Grab string resources
        mSelectedPageStr = context.getResources().getString(R.string.page_label_selected_page);
        mMaxPageStr = context.getResources().getString(R.string.page_label_max_page);
        mPreviewTextStr = context.getString(R.string.page_label_preview);
        mInvalidStartNumStr = context.getString(R.string.page_label_invalid_start);
        mInvalidPageRange = context.getString(R.string.page_label_invalid_range);

        View container = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.dialog_page_label, parent, true);

        setupPageSettings(container, listener);
        setupNumberingSettings(container, listener);
        mPreview = container.findViewById(R.id.page_label_preview);
    }

    @Override
    public void updatePreview(String preview) {
        mPreview.setText(String.format("%s: %s", mPreviewTextStr, preview));
    }

    @Override
    public void initViewStates(@Nullable PageLabelSetting mInitState) {
        // Initialize view states
        if (mInitState != null) {
            // Initialize radio buttons
            mPageAll.setChecked(mInitState.isAll());
            mPageSelected.setChecked(mInitState.isSelectedPage());
            mPageRange.setChecked(!mInitState.isAll() && !mInitState.isSelectedPage());

            // Initialize the page range section
            mToPageEditTxt.setText(String.valueOf(mInitState.getToPage()));
            mFromPageEditTxt.setText(String.valueOf(mInitState.getFromPage()));
            setPageRangeEnabled(mFromPageEditTxt, mToPageEditTxt,
                !mInitState.isAll() && !mInitState.isSelectedPage());

            // Initialize the max page text and current selected page text
            String selectedPage = String.format(mSelectedPageStr, mInitState.selectedPage);
            mPageSelected.setText(selectedPage);

            String numPages = String.format(mMaxPageStr, mInitState.numPages);
            mMaxPage.setText(numPages);

            // Initialize numbering settings
            mStyleSpinner.setSelection(mInitState.getStyle().ordinal());
            mPrefixEditTxt.setText(mInitState.getPrefix());
            mStartNumEditTxt.setEnabled(mInitState.getStyle() != PageLabelSetting.PageLabelStyle.NONE);
            mStartNumEditTxt.setText(String.valueOf(mInitState.getStartNum()));
        }
    }

    @Override
    public void invalidFromPage(boolean isValid) {
        if (isValid) {
            mFromPageEditTxt.setError(null);
        } else {
            mFromPageEditTxt.setError(mInvalidPageRange);
        }
    }

    @Override
    public void invalidToPage(boolean isValid) {
        if (isValid) {
            mToPageEditTxt.setError(null);
        } else {
            mToPageEditTxt.setError(mInvalidPageRange);
        }
    }

    @Override
    public void invalidStartNumber(boolean isValid) {
        if (isValid) {
            mStartNumEditTxt.setError(null);
        } else {
            mStartNumEditTxt.setError(mInvalidStartNumStr);
        }
    }

    private void setupPageSettings(@NonNull final View container,
                                   @NonNull final PageLabelSettingChangeListener listener) {

        // Setup radio buttons
        mPageAll = container.findViewById(R.id.radio_pages_all);
        mPageSelected = container.findViewById(R.id.radio_pages_selected);
        mPageRange = container.findViewById(R.id.radio_pages_range);
        mFromPageEditTxt = container.findViewById(R.id.page_range_from_edittext);
        mToPageEditTxt = container.findViewById(R.id.page_range_to_edittext);
        mMaxPage = container.findViewById(R.id.page_range_max);

        // Setup listeners for radio buttons
        mPageAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPageSelected.setChecked(false);
                    mPageRange.setChecked(false);
                    listener.setSelectedPage(false);
                }
                listener.setAll(isChecked);
            }
        });

        mPageSelected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPageAll.setChecked(false);
                    mPageRange.setChecked(false);
                    listener.setAll(false);
                }
                listener.setSelectedPage(isChecked);
            }
        });

        mPageRange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mPageAll.setChecked(false);
                    mPageSelected.setChecked(false);
                    listener.setAll(false);
                    listener.setSelectedPage(false);
                }
                setPageRangeEnabled(mFromPageEditTxt, mToPageEditTxt, isChecked);
            }
        });

        // Setup listeners for edit texts for page settings
        mFromPageEditTxt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String fromText = s.toString();
                String toText = mToPageEditTxt.getEditableText().toString();
                listener.setPageRange(fromText, toText);
            }
        });
        mToPageEditTxt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String fromText = mFromPageEditTxt.getEditableText().toString();
                String toText = s.toString();
                listener.setPageRange(fromText, toText);
            }
        });
    }

    private void setupNumberingSettings(@NonNull View container, @NonNull final PageLabelSettingChangeListener listener) {
        mStyleSpinner = container.findViewById(R.id.numbering_style_spinner);
        mStyleSpinner.setAdapter(getStyleSpinnerAdapter(container));
        mPrefixEditTxt = container.findViewById(R.id.numbering_prefix_edittext);
        mStartNumEditTxt = container.findViewById(R.id.numbering_start_edittext);

        // Setup listeners
        mStyleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PageLabelSetting.PageLabelStyle style = PageLabelSetting.PageLabelStyle.values()[position];
                listener.setStyle(style);
                mStartNumEditTxt.setEnabled(style != PageLabelSetting.PageLabelStyle.NONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mPrefixEditTxt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                listener.setPrefix(s.toString());
            }
        });

        mStartNumEditTxt.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                listener.setStartNumber(s.toString());
            }
        });
    }

    private SpinnerAdapter getStyleSpinnerAdapter(View parent) {
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(parent.getContext(), android.R.layout.simple_spinner_item);
        for (PageLabelSetting.PageLabelStyle style : PageLabelSetting.PageLabelStyle.values()) {
            adapter.add(style.mLabel);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private void setPageRangeEnabled(final EditText fromPageEt, final EditText toPageEt, boolean isEnabled) {
        fromPageEt.setEnabled(isEnabled);
        toPageEt.setEnabled(isEnabled);
    }

    // Simple text watcher to only watch for afterTextChanged
    private static abstract class SimpleTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // do nothing
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // do nothing
        }
    }

}
