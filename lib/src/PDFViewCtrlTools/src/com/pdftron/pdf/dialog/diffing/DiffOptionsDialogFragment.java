package com.pdftron.pdf.dialog.diffing;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pdftron.pdf.controls.CustomSizeDialogFragment;
import com.pdftron.pdf.tools.R;
import com.pdftron.pdf.utils.Utils;
import com.pdftron.pdf.widget.DiffOptionsView;

import java.util.ArrayList;

public class DiffOptionsDialogFragment extends CustomSizeDialogFragment {

    public final static String TAG = DiffOptionsDialogFragment.class.getName();

    public interface DiffOptionsDialogListener {
        void onDiffOptionsConfirmed(int color1, int color2, int blendMode);
    }

    private final static String BUNDLE_FILE_1 = "bundle_file_1";
    private final static String BUNDLE_FILE_2 = "bundle_file_2";

    private Uri mFile1;
    private Uri mFile2;

    private DiffOptionsView mDiffOptions;

    private DiffOptionsDialogListener mDiffOptionsDialogListener;

    public static DiffOptionsDialogFragment newInstance(Uri file1, Uri file2) {
        DiffOptionsDialogFragment fragment = new DiffOptionsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BUNDLE_FILE_1, file1);
        bundle.putParcelable(BUNDLE_FILE_2, file2);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setDiffOptionsDialogListener(DiffOptionsDialogListener listener) {
        mDiffOptionsDialogListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Utils.isTablet(getContext())) {
            mHeight = 500;
        }

        Bundle args = getArguments();
        if (args != null) {
            mFile1 = args.getParcelable(BUNDLE_FILE_1);
            mFile2 = args.getParcelable(BUNDLE_FILE_2);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_diff_tool, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.diff_options_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mDiffOptions = view.findViewById(R.id.diff_options_view);
        mDiffOptions.setSelectFileButtonVisibility(false);
        mDiffOptions.setAnnotationToggleVisibility(false);
        mDiffOptions.setFiles(DiffUtils.getUriInfo(view.getContext(), mFile1), DiffUtils.getUriInfo(view.getContext(), mFile2));
        mDiffOptions.setDiffOptionsViewListener(new DiffOptionsView.DiffOptionsViewListener() {
            @Override
            public void onSelectFile(View which) {

            }

            @Override
            public void onCompareFiles(ArrayList<Uri> files) {
                if (mDiffOptionsDialogListener != null) {
                    mDiffOptionsDialogListener.onDiffOptionsConfirmed(
                        mDiffOptions.getColor1(),
                        mDiffOptions.getColor2(),
                        mDiffOptions.getBlendMode()
                    );
                }
                dismiss();
            }
        });
    }
}
