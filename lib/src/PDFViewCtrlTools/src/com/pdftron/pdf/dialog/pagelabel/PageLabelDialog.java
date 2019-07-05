package com.pdftron.pdf.dialog.pagelabel;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.pdftron.pdf.tools.R;


/**
 * Dialog Fragment containing UI for getting Page label modification settings
 * from the user.
 */
public final class PageLabelDialog extends DialogFragment implements PageLabelComponent.DialogButtonInteractionListener {
    public static final String TAG = PageLabelComponent.class.getName();

    public static final String FROM_PAGE = "PageLabelDialogView_Initial_frompage";
    public static final String TO_PAGE = "PageLabelDialogView_Initial_topage";
    public static final String MAX_PAGE = "PageLabelDialogView_Initial_maxpage";
    public static final String PREFIX = "PageLabelDialogView_Initial_prefix";

    @NonNull
    private PageLabelComponent pageLabelComponent;

    /**
     * Create a {@link PageLabelDialog} with initial values set for page range.
     *
     * @param fromPage start page for page range
     * @param toPage   end page for page range
     * @param maxPage  maximum number of pages
     * @return the initialized {@link PageLabelDialog}
     */
    public static PageLabelDialog newInstance(int fromPage, int toPage, int maxPage) {
        PageLabelDialog fragment = new PageLabelDialog();
        Bundle args = new Bundle();
        args.putInt(FROM_PAGE, fromPage);
        args.putInt(TO_PAGE, toPage);
        args.putInt(MAX_PAGE, maxPage);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Create a {@link PageLabelDialog} with initial values set for page range and initial prefix.
     *
     * @param fromPage      start page used for initial page range
     * @param toPage        end page used for initial page range
     * @param maxPage       maximum number of pages
     * @param initialPrefix initial prefix set
     * @return the initialized {@link PageLabelDialog}
     */
    public static PageLabelDialog newInstance(int fromPage, int toPage, int maxPage, @Nullable String initialPrefix) {
        PageLabelDialog fragment = new PageLabelDialog();
        Bundle args = new Bundle();
        args.putInt(FROM_PAGE, fromPage);
        args.putInt(TO_PAGE, toPage);
        args.putInt(MAX_PAGE, maxPage);
        args.putString(PREFIX, initialPrefix);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        FragmentActivity activity = getActivity();
        if (activity == null) {
            return super.onCreateDialog(savedInstanceState);
        }

        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.fragment_container, null);
        ViewGroup container = view.findViewById(R.id.container);

        Bundle args = getArguments();
        if (args != null) {
            int fromPage = args.getInt(FROM_PAGE);
            int toPage = args.getInt(TO_PAGE);
            int maxPage = args.getInt(MAX_PAGE);
            @Nullable String prefix = args.getString(PREFIX);
            pageLabelComponent = (prefix == null) ?
                    new PageLabelComponent(activity, container, fromPage, toPage, maxPage, this) :
                    new PageLabelComponent(activity, container, fromPage, toPage, maxPage, prefix, this);

        } else {
            pageLabelComponent = new PageLabelComponent(activity, container,
                    1, 1, this);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setTitle(R.string.page_label_setting_title)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        pageLabelComponent.completeSettings();
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });
        return builder.create();
    }

    private void setPositiveButtonEnabled(boolean enabled) {
        AlertDialog dialog = (AlertDialog) getDialog();
        Button posButton = dialog == null ? null : dialog.getButton(Dialog.BUTTON_POSITIVE);
        if (posButton != null) {
            posButton.setEnabled(enabled);
        }
    }

    @Override
    public void disallowSave() {
        setPositiveButtonEnabled(false);
    }

    @Override
    public void allowSave() {
        setPositiveButtonEnabled(true);
    }
}
