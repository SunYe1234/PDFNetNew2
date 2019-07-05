package com.pdftron.pdf.dialog.simpleinput;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.Utils;

public class TextInputDialog extends DialogFragment {
    public static final String TAG = TextInputDialog.class.getName();

    public static final String REQUEST_CODE = "TextInputDialog_requestcode";
    public static final String TITLE_RES = "TextInputDialog_titleres";
    public static final String HINT_RES = "TextInputDialog_hintres";
    public static final String POSITIVE_BUTTON_RES = "TextInputDialog_positivebtnres";
    public static final String NEGATIVE_BUTTON_RES = "TextInputDialog_negativebtnres";

    private int mRequestCode;
    private @StringRes
    int mTitleRes;
    private @StringRes
    int mHintRes;
    private @StringRes
    int mPosRes;
    private @StringRes
    int mNegRes;

    private TextInputViewModel mViewModel;

    public static TextInputDialog newInstance(int requestCode,
            @StringRes int titleRes, @StringRes int hintRes,
            @StringRes int posRes, @StringRes int negRes) {
        TextInputDialog dialog = new TextInputDialog();
        Bundle args = new Bundle();
        args.putInt(REQUEST_CODE, requestCode);
        args.putInt(TITLE_RES, titleRes);
        args.putInt(HINT_RES, hintRes);
        args.putInt(POSITIVE_BUTTON_RES, posRes);
        args.putInt(NEGATIVE_BUTTON_RES, negRes);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            mRequestCode = args.getInt(REQUEST_CODE);
            mTitleRes = args.getInt(TITLE_RES);
            mHintRes = args.getInt(HINT_RES);
            mPosRes = args.getInt(POSITIVE_BUTTON_RES);
            mNegRes = args.getInt(NEGATIVE_BUTTON_RES);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        mViewModel = ViewModelProviders.of(activity).get(TextInputViewModel.class);

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_text_input, null);
        TextInputEditText editText = view.findViewById(R.id.text_input);
        editText.setHint(mHintRes);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                TextInputResult textInputResult = new TextInputResult(mRequestCode, input);
                mViewModel.set(textInputResult);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mViewModel.observeChanges(this, new Observer<TextInputResult>() {
            @Override
            public void onChanged(@Nullable TextInputResult textInputResult) {
                if (textInputResult == null || Utils.isNullOrEmpty(textInputResult.getResult())) {
                    setPositiveButtonEnabled(false);
                } else {
                    setPositiveButtonEnabled(true);
                }
            }
        });
        setPositiveButtonEnabled(Utils.isNullOrEmpty(editText.getText() == null ?
                null : editText.getText().toString()));

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(mTitleRes)
                .setPositiveButton(mPosRes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                })
                .setNegativeButton(mNegRes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mViewModel.set(null);
                        dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mViewModel.complete();
    }

    private void setPositiveButtonEnabled(boolean enabled) {
        AlertDialog dialog = (AlertDialog) getDialog();
        Button posButton = dialog == null ? null : dialog.getButton(Dialog.BUTTON_POSITIVE);
        if (posButton != null) {
            posButton.setEnabled(enabled);
        }
    }
}
