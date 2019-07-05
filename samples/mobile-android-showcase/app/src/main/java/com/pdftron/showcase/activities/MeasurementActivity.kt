package com.pdftron.showcase.activities

import android.os.Bundle
import android.widget.RadioButton
import com.pdftron.pdf.config.ToolStyleConfig
import com.pdftron.pdf.model.AnnotStyle.*
import com.pdftron.pdf.tools.Pan
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_measurement.*

class MeasurementActivity : FeatureActivity() {

    private val creationModeArray = arrayOf(
            ToolManager.ToolMode.RULER_CREATE,
            ToolManager.ToolMode.PERIMETER_MEASURE_CREATE,
            ToolManager.ToolMode.AREA_MEASURE_CREATE)

    private var mSelectedIndex : Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        fullScreenMode = false
        sampleFileName = "floorplan"
        ToolStyleConfig.getInstance().addDefaultStyleMap(CUSTOM_ANNOT_TYPE_RULER, R.style.CustomMeasureStyle)
        ToolStyleConfig.getInstance().addDefaultStyleMap(CUSTOM_ANNOT_TYPE_PERIMETER_MEASURE, R.style.CustomMeasureStyle)
        ToolStyleConfig.getInstance().addDefaultStyleMap(CUSTOM_ANNOT_TYPE_AREA_MEASURE, R.style.CustomMeasureStyle)

        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_measurement, bottomSheetContainer, true)
        addControl()
    }

    private fun addControl() {
        measurement_tools.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            if (rb != null) {
                val index = rb.tag.toString().toInt()
                mSelectedIndex = index
                getPdfViewCtrlTabFragment()!!.annotationToolbar?.closeEditToolbar()
                if (index == 0) {
                    PdfViewCtrlSettingsManager.updateDoubleRowToolbarInUse(this@MeasurementActivity, true)
                    mPdfViewCtrlTabHostFragment!!.onOpenAnnotationToolbar(ToolManager.ToolMode.RULER_CREATE)
                } else {
                    getToolManager()!!.onOpenEditToolbar(creationModeArray[index])
                }
            }
        }

        snap_switch.setOnCheckedChangeListener { _, isChecked ->
            getToolManager()!!.isSnappingEnabledForMeasurementTools = isChecked
        }
    }

    override fun handleTabDocumentLoaded(tag: String) {
        super.handleTabDocumentLoaded(tag)

        getToolManager()!!.addToolChangedListener { newTool, _ ->
            if (newTool is Pan) {
                measurement_tools.clearCheck()
            }
        }
    }
}