package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.controls.AnnotationToolbar
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class AnnotationToolbarActivity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        fullScreenMode = false
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        getPdfViewCtrlTabFragment()!!.addAnnotationToolbarListener(object : AnnotationToolbar.AnnotationToolbarListener {
            override fun onAnnotationToolbarClosed() {
                button.text = getString(R.string.button_open_toolbar)
            }

            override fun onAnnotationToolbarShown() {
                button.text = getString(R.string.button_close_toolbar)
            }

            override fun onShowAnnotationToolbarByShortcut(p0: Int) {
            }
        })
    }

    private fun addControl() {
        button.text = getString(R.string.open_annotation_toolbar)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_mode_edit_white, 0, 0, 0)
        button.setPadding(Utils.convDp2Pix(this, 30f).toInt(),
                button.paddingTop,
                Utils.convDp2Pix(this, 30f).toInt(),
                button.paddingBottom)
        button.setOnClickListener {
            // Toggle annotation toolbar visibility
            if (getPdfViewCtrlTabFragment()!!.isAnnotationMode) {
                getPdfViewCtrlTabFragment()!!.hideAnnotationToolbar()
            } else {
                mPdfViewCtrlTabHostFragment!!.onOpenAnnotationToolbar(null as ToolManager.ToolMode?)
            }
        }

    }
}