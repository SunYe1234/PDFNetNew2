package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_redaction.*

class RedactionActivity : FeatureActivity() {
    private val creationModeArray = arrayOf(
            ToolManager.ToolMode.TEXT_REDACTION,
            ToolManager.ToolMode.RECT_REDACTION)

    override fun onCreate(savedInstanceState: Bundle?) {
        fullScreenMode = false
        sampleFileName = "newsletter"
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_redaction, bottomSheetContainer, true)
        addControls()
    }

    private fun addControls() {
        text_redact.setOnClickListener {
            changeTool(0)
        }
        rect_redact.setOnClickListener {
            changeTool(1)
        }
    }

    private fun changeTool(which : Int) {
        getToolManager()!!.tool = getToolManager()!!.createTool(creationModeArray[which], null)
    }
}