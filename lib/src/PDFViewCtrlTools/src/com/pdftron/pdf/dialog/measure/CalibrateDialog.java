package com.pdftron.pdf.dialog.measure;

import android.app.AlertDialog;
import android.app.Dialog;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.pdftron.pdf.tools.R;

public class CalibrateDialog extends DialogFragment implements AdapterView.OnItemSelectedListener {
    public static final String TAG = CalibrateDialog.class.getName();

    public static final String ANNOT_SDFOBJ = "CalibrateDialog_sdfObj";
    public static final String ANNOT_PAGE = "CalibrateDialog_page";
    public static final String WORLD_UNIT = "CalibrateDialog_worldUnit";

    private CalibrateViewModel mViewModel;

    private CalibrateResult mCalibrateResult;

    private Spinner mSpinner;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;

    public static CalibrateDialog newInstance(long annot, int page, String worldUnit) {
        CalibrateDialog dialog = new CalibrateDialog();
        Bundle args = new Bundle();
        args.putLong(ANNOT_SDFOBJ, annot);
        args.putInt(ANNOT_PAGE, page);
        args.putString(WORLD_UNIT, worldUnit);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            long annot = args.getLong(ANNOT_SDFOBJ);
            int page = args.getInt(ANNOT_PAGE);
            mCalibrateResult = new CalibrateResult(annot, page);
            mCalibrateResult.worldUnit = args.getString(WORLD_UNIT);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        mViewModel = ViewModelProviders.of(activity).get(CalibrateViewModel.class);
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_calibrate, null);
        EditText editText = view.findViewById(R.id.measure_edit_text);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                try {
                    mCalibrateResult.userInput = Float.parseFloat(input);
                } catch (Exception ignored) {

                }
                setResult();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mSpinner = view.findViewById(R.id.measure_unit_spinner);

        mSpinnerAdapter = ArrayAdapter.createFromResource(activity,
                R.array.ruler_translate_unit, android.R.layout.simple_spinner_item);
        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(this);

        if (mCalibrateResult != null && mCalibrateResult.worldUnit != null) {
            int index = mSpinnerAdapter.getPosition(mCalibrateResult.worldUnit);
            if (index >= 0 && index < mSpinnerAdapter.getCount()) {
                mSpinner.setSelection(index);
            }
        }

        mViewModel.observeChanges(this, new Observer<CalibrateResult>() {
            @Override
            public void onChanged(@Nullable CalibrateResult result) {
                if (result == null) {
                    setPositiveButtonEnabled(false);
                } else {
                    setPositiveButtonEnabled(true);
                }
            }
        });
        setPositiveButtonEnabled(false);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.measure_calibrate_title)
                .setMessage(R.string.measure_calibrate_body)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCalibrateResult.userInput = null;
                        setResult();
                        dismiss();
                    }
                });
        return builder.create();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (parent.getId() == mSpinner.getId()) {
            if (position >= 0 && mSpinnerAdapter != null) {
                CharSequence unit = mSpinnerAdapter.getItem(position);
                if (unit != null && mCalibrateResult != null) {
                    mCalibrateResult.worldUnit = unit.toString();
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mViewModel.complete();
    }

    private void setResult() {
        mViewModel.set(mCalibrateResult);
    }

    private void setPositiveButtonEnabled(boolean enabled) {
        AlertDialog dialog = (AlertDialog) getDialog();
        Button posButton = dialog == null ? null : dialog.getButton(Dialog.BUTTON_POSITIVE);
        if (posButton != null) {
            posButton.setEnabled(enabled);
        }
    }
}
