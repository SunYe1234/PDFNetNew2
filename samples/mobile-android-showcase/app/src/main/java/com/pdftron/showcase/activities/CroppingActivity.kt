package com.pdftron.showcase.activities

import android.os.Bundle
import android.widget.Button
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*
import kotlinx.android.synthetic.main.control_button_simple.*

class CroppingActivity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        showTab = true
        sampleFileName = "numbered"
        super.onCreate(savedInstanceState)

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {

        val saveCropButton = layoutInflater.inflate(R.layout.content_bottomsheet_button, button_container, false) as Button

        saveCropButton.text = getString(R.string.save_cropped_copy)
        saveCropButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_save_black_24dp, 0, 0, 0)
        saveCropButton.setOnClickListener {
            saveCroppedCopy()
        }

        button.text = getString(R.string.pref_viewmode_user_crop)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.user_crop, 0, 0, 0)
        button.setOnClickListener {
            mPdfViewCtrlTabHostFragment!!.onViewModeSelected(
                    PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_USERCROP_VALUE
            )
        }

        button_container.addView(saveCropButton)
    }

    private fun saveCroppedCopy() {
        mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment.setSavingEnabled(true)
        mPdfViewCtrlTabHostFragment!!.onSaveCroppedCopySelected()
    }

}