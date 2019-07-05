package com.pdftron.showcase.activities

import android.view.View
import com.pdftron.showcase.R

class ComplexRenderingActivity : FeatureWithComparisonActivity() {

    private val TAG = "ComplexRenderActivity"

    override fun setUpViews() {
        super.setUpViews()
        titleL.text = getString(R.string.progressive_rendering)
        titleR.text = getString(R.string.no_progressive_rendering)
        byteBarLeft.visibility = View.GONE
        byteBarRight.visibility = View.GONE
    }

    override fun setUpPdfCtrlViews() {
        super.setUpPdfCtrlViews()
        setUpProgressInterval()
        setUpDocs(R.raw.construction_drawing, "construction_drawing", R.raw.construction_drawing, "construction_drawing")
    }

    override fun resetSamples() {
        setUpDocs(R.raw.construction_drawing, "construction_drawing", R.raw.construction_drawing, "construction_drawing")
    }

    private fun setUpProgressInterval() {
        leftPdfViewCtrl!!.clearThumbCache()
        leftPdfViewCtrl!!.setupThumbnails(false, false, false, 0, 0, 0.0)
        leftPdfViewCtrl!!.setProgressiveRendering(true, 50, 100)

        rightPdfViewCtrl!!.clearThumbCache()
        rightPdfViewCtrl!!.setupThumbnails(false, false, false, 0, 0, 0.0)
        rightPdfViewCtrl!!.progressiveRendering = false
    }

}
