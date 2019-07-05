package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.dialog.watermark.WatermarkDialog
import com.pdftron.pdf.dialog.watermark.WatermarkUtil
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

class WatermarkActivity : FeatureActivity() {

    private val mFileName = "newsletter"

    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = mFileName
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addControl()
    }

    private fun addControl() {
        button.text = getString(R.string.watermark_button)
        button.setOnClickListener {
            // Clear watermarks first
            WatermarkUtil.clearWatermark(getPDFViewCtrl()!!)
            closeBottomSheet()

            val fragment = WatermarkDialog.newInstance( getPDFViewCtrl()!!)
            fragment.show(supportFragmentManager)

            // Initially draw the watermark
            val color = fragment.annotStyle!!.textColor
            val opacity = fragment.annotStyle!!.opacity
            val textSize = fragment.annotStyle!!.textSize
            val text = fragment.annotStyle!!.overlayText
            WatermarkUtil.setTextWatermark(getPDFViewCtrl()!!, text,
                    color, opacity, textSize, true)
        }
    }
}
