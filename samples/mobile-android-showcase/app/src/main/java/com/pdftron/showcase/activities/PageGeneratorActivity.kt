package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class PageGeneratorActivity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {
        button.text = getString(R.string.dialog_add_page_title)
        button.setOnClickListener {
            mPdfViewCtrlTabHostFragment!!.addNewPage()
        }
    }

}