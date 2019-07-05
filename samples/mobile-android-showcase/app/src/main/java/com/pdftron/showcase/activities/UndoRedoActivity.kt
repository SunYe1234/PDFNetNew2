package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_undo_redo.*

class UndoRedoActivity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        fullScreenMode = false

        super.onCreate(savedInstanceState)

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_undo_redo, bottomSheetContainer, true)
        addControls()
    }

    private fun addControls() {
        undo.setOnClickListener {
            mPdfViewCtrlTabHostFragment?.currentPdfViewCtrlFragment?.undo()
        }
        redo.setOnClickListener {
            mPdfViewCtrlTabHostFragment?.currentPdfViewCtrlFragment?.redo()
        }
    }

    override fun handleTabDocumentLoaded(tag: String) {
        PdfViewCtrlSettingsManager.updateDoubleRowToolbarInUse(this@UndoRedoActivity, true)
        mPdfViewCtrlTabHostFragment!!.onOpenAnnotationToolbar(null as ToolManager.ToolMode?)
    }

}