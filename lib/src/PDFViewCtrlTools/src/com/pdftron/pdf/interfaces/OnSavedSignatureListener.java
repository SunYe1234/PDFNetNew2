package com.pdftron.pdf.interfaces;

import android.support.annotation.NonNull;

/**
 * Callback interface to be invoked when a signature has been selected.
 */
public interface OnSavedSignatureListener {
    /**
     * Called when a signature is selected.
     *
     * @param filepath The file path of the saved signature
     */
    void onSignatureSelected(@NonNull String filepath);

    /**
     * Called when create signature is selected.
     */
    void onCreateSignatureClicked();
}
