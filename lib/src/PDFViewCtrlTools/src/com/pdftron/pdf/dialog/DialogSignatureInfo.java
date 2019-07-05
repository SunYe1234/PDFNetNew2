package com.pdftron.pdf.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.pdftron.pdf.tools.R;

public class DialogSignatureInfo extends AlertDialog {

    private TextView locationInfo;
    private TextView reasonInfo;
    private TextView nameInfo;

    public DialogSignatureInfo(Context context) {
        super(context);

        View view = LayoutInflater.from(getContext()).inflate(R.layout.tools_dialog_signatureinfo, null);
        locationInfo = view.findViewById(R.id.tools_dialog_signatureinfo_location);
        reasonInfo = view.findViewById(R.id.tools_dialog_signatureinfo_reason);
        nameInfo = view.findViewById(R.id.tools_dialog_signatureinfo_name);

        setView(view);

        setTitle(context.getString(R.string.tools_digitalsignature_signature_info));

        setButton(DialogInterface.BUTTON_POSITIVE, context.getString(R.string.ok), (DialogInterface.OnClickListener) null);
    }

    public void setLocation(String text) {
        this.locationInfo.setText(text);
    }

    public void setReason(String reason) {
        this.reasonInfo.setText(reason);
    }

    public void setName(String name) {
        this.nameInfo.setText(name);
    }
}
