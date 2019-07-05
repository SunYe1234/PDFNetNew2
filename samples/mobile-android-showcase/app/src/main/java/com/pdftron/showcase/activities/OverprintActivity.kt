package com.pdftron.showcase.activities

import android.view.View
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.utils.Utils
import com.pdftron.sdf.SDFDoc
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.control_button_simple.*

class OverprintActivity  : FeatureWithComparisonActivity() {

    private val TAG = "OverprintActivity"

    private val filename = "pdftron_logo_cropped_glasses"

    override fun setUpViews() {
        super.setUpViews()
        titleL.text = getString(R.string.overprint_on)
        titleR.text = getString(R.string.overprint_off)
        control_button_simple.visibility = View.GONE
        byteBarLeft.visibility = View.GONE
        byteBarRight.visibility = View.GONE
    }

    override fun setUpPdfCtrlViews() {
        super.setUpPdfCtrlViews()
        resetSamples()
        setupOverprint()
    }

    override fun resetSamples() {
        setUpDocs(R.raw.pdftron_logo_cropped_glasses, filename + "L", R.raw.pdftron_logo_cropped_glasses, filename + "R")
    }

    override fun setUpDocs(leftResourceId: Int, leftFileName: String, rightResourceId: Int, rightFileName: String) {
        val file = Utils.copyResourceToLocal(this, leftResourceId, leftFileName, ".pdf")
        leftPdfDoc = PDFDoc(file.absolutePath)
        leftPdfDoc!!.save(file.absolutePath, SDFDoc.SaveMode.LINEARIZED, null)
        leftPdfViewCtrl!!.doc = leftPdfDoc

        val fileR = Utils.copyResourceToLocal(this, rightResourceId, rightFileName, ".pdf")
        rightPdfDoc = PDFDoc(fileR.absolutePath)
        rightPdfViewCtrl!!.doc = rightPdfDoc
    }

    private fun setupOverprint() {
        leftPdfViewCtrl!!.clearThumbCache()
        leftPdfViewCtrl!!.setOverprint(PDFViewCtrl.OverPrintMode.PDFX)

        rightPdfViewCtrl!!.clearThumbCache()
        rightPdfViewCtrl!!.setOverprint(PDFViewCtrl.OverPrintMode.OFF)
    }
}