package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class FlattenActivity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        showTab = true
        sampleFileName = "sample_preload"
        super.onCreate(savedInstanceState)

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {
        button.text = getString(R.string.tools_qm_flatten)
        button.setOnClickListener {
            mPdfViewCtrlTabHostFragment!!.onFlattenOptionSelected()
        }
    }

}