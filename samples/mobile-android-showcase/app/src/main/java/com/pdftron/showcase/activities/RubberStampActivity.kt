package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.tools.RubberStampCreate
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class RubberStampActivity : FeatureActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {
        button.text = getString(R.string.add_rubber_stamp)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_annotation_stamp_black_24dp, 0, 0, 0)
        button.setOnClickListener {
            val tool = getToolManager()!!.createTool(ToolManager.ToolMode.RUBBER_STAMPER, null) as RubberStampCreate
            getToolManager()!!.tool = tool
        }
    }
}