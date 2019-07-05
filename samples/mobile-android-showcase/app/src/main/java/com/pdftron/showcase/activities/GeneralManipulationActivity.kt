package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*


class GeneralManipulationActivity : FeatureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {
        button.text = getString(R.string.open_page_browser)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_thumbnails_grid_black_24dp, 0, 0, 0)
        button.setOnClickListener {
            mPdfViewCtrlTabHostFragment!!.onPageThumbnailOptionSelected(true, null)
        }
    }
}