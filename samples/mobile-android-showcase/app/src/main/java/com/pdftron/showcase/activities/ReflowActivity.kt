package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*


class ReflowActivity : FeatureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = "newsletter"
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {
        button.text = getString(R.string.enable_reading_mode)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_view_mode_reflow_black_24dp, 0, 0, 0)
        button.setOnClickListener {
            if (mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment.isReflowMode) {
                button.text = getString(R.string.button_enable_reflow)
            } else {
                button.text = getString(R.string.button_disable_reflow)
            }
            mPdfViewCtrlTabHostFragment!!.onToggleReflow()
        }
    }
}