package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class TextSearchActivity : FeatureActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    override fun handleStartSearchMode() {
        button.text = getString(R.string.button_exit_search)
        closeBottomSheet()
    }

    override fun handleExitSearchMode() {
        button.text = getString(R.string.button_start_search)
    }

    private fun addControl() {
        button.text = getString(R.string.open_text_search)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_search_white_24dp, 0, 0, 0)
        button.setOnClickListener {
            // toggle search visibility
            if (getPdfViewCtrlTabFragment()!!.isSearchMode) {
                mPdfViewCtrlTabHostFragment!!.exitSearchMode()
            } else {
                mPdfViewCtrlTabHostFragment!!.startSearchMode()
            }
        }
    }

}