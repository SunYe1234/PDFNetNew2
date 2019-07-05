package com.pdftron.pdf.dialog.simpleinput;

import android.support.annotation.NonNull;

/**
 * Utility class for gson
 */
public class TextInputResult {
    private final int requestCode;
    @NonNull
    private final String result;

    public TextInputResult(int requestCode, @NonNull String result) {
        this.requestCode = requestCode;
        this.result = result;
    }

    public int getRequestCode() {
        return requestCode;
    }

    @NonNull
    public String getResult() {
        return result;
    }

}
