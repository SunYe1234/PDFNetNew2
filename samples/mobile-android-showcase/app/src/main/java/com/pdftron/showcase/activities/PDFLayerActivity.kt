package com.pdftron.showcase.activities

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.config.PDFViewCtrlConfig
import com.pdftron.pdf.utils.Utils
import com.pdftron.pdf.dialog.pdflayer.PdfLayerUtils
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_pdflayer.*

class PDFLayerActivity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = "construction_drawing"
        super.onCreate(savedInstanceState)

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_pdflayer, bottomSheetContainer, true)
    }

    override fun handleTabDocumentLoaded(tag: String) {
        super.handleTabDocumentLoaded(tag)

        addControl()
    }

    private fun addControl() {
        // Gets all layers
        var shouldUnlockRead = false
        val pdfViewCtrl = getPDFViewCtrl()
        // disable thumb
        pdfViewCtrl!!.setupThumbnails(false, false, false, 0, 0, 0.0)
        pdfViewCtrl.rotateCounterClockwise()
        pdfViewCtrl.pagePresentationMode = PDFViewCtrl.PagePresentationMode.SINGLE_CONT
        pdfViewCtrl.setVerticalAlign(-1)
        val config = PDFViewCtrlConfig(this)
        pdfViewCtrl.setPageSpacingDP(config.pageHorizontalColumnSpacing,
                config.pageVerticalColumnSpacing,
                config.pageHorizontalPadding,
                Utils.convPix2Dp(this, getStatusBarHeight().toFloat() + getActionBarHeight().toFloat()).toInt())
        try {
            pdfViewCtrl.docLockRead()
            shouldUnlockRead = true
            val pdfDoc = getPDFDoc()
            val layers = PdfLayerUtils.getLayers(pdfViewCtrl, pdfDoc)
            layer_view.setup(layers)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl.docUnlockRead()
            }
        }

        // Set checked layers
        layer_view.recyclerView.layoutManager = GridLayoutManager(this, 2)
        layer_view.itemClickHelper.setOnItemClickListener { recyclerView, view, position, id ->
            val layerInfo = layer_view.adapter?.getItem(position)
            if (layerInfo != null) {
                layerInfo.checked = !layerInfo.checked
                layer_view.adapter?.notifyItemChanged(position)
                try {
                    PdfLayerUtils.setLayerCheckedChange(pdfViewCtrl, layerInfo.group, layerInfo.checked)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}