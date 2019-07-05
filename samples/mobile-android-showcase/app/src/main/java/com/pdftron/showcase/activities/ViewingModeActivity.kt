package com.pdftron.showcase.activities

import android.os.Bundle
import android.widget.RadioButton
import com.pdftron.pdf.PDFRasterizer
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_view_modes.*


class ViewingModeActivity : FeatureActivity() {

    private val TAG = "ViewingModeActivity"

    private val colorModeArray = arrayOf(PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_NORMAL,
            PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_NIGHT,
            PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_CUSTOM,
            PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_SEPIA)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_view_modes, bottomSheetContainer, true)
        addDifferentViewModesControls()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        updateCodeSnippet(0)
    }

    private fun addDifferentViewModesControls() {
        color_modes.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            changeColorMode(rb.tag.toString().toInt())
        }
    }

    private fun changeColorMode(selected: Int) {
        val mode = colorModeArray[selected]

        if (mode != PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_CUSTOM) {
            mPdfViewCtrlTabHostFragment?.onViewModeColorSelected(mode)
        } else {
            getPDFViewCtrl()!!.colorPostProcessMode = PDFRasterizer.e_postprocess_invert
        }
    }

    private fun updateCodeSnippet(colorMode: Int) {
        if (feature!!.codeSnippets != null) {
            val code = feature!!.codeSnippets!![colorMode].code
            highlightCodeSnippet(code)
        }
    }

}
