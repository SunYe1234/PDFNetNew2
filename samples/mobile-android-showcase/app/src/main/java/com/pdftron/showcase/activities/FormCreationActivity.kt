package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_form_creation.*

class FormCreationActivity : FeatureActivity() {

    private val creationModeArray = arrayOf(
            ToolManager.ToolMode.FORM_TEXT_FIELD_CREATE,
            ToolManager.ToolMode.FORM_CHECKBOX_CREATE,
            ToolManager.ToolMode.FORM_SIGNATURE_CREATE)

    override fun onCreate(savedInstanceState: Bundle?) {
        fullScreenMode = false
        sampleFileName = "form_creation"
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_form_creation, bottomSheetContainer, true)
        addControls()
    }

    private fun addControls() {
        text_field.setOnClickListener {
            changeTool(0)
        }
        checkbox.setOnClickListener {
            changeTool(1)
        }
        signature.setOnClickListener {
            changeTool(2)
        }
    }

    private fun changeTool(which : Int) {
        getPDFViewCtrl()!!.currentPage = which + 1
        getToolManager()!!.tool = getToolManager()!!.createTool(creationModeArray[which], null)
    }
}