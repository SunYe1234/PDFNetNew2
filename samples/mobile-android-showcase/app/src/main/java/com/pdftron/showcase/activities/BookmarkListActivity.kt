package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class BookmarkListActivity : FeatureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = "sample_preload"
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {

        button.text = getString(R.string.open_bookmark_list)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_bookmarks_white_24dp, 0, 0, 0)
        button.setOnClickListener {
            mPdfViewCtrlTabHostFragment!!.onOutlineOptionSelected(0)
        }
    }
}