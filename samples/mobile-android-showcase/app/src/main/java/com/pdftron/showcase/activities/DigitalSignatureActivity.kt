package com.pdftron.showcase.activities

import android.os.Bundle
import com.pdftron.pdf.config.PDFViewCtrlConfig
import com.pdftron.pdf.config.ToolManagerBuilder
import com.pdftron.pdf.config.ViewerConfig

class DigitalSignatureActivity : FeatureActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = "digital_signature"
        super.onCreate(savedInstanceState)
    }

    override fun getViewerConfig(): ViewerConfig {
        val pdfviewCtrlConfig = PDFViewCtrlConfig.getDefaultConfig(this)
        pdfviewCtrlConfig.isThumbnailUseEmbedded = false
        val toolManagerBuilder = ToolManagerBuilder.from()
                .setUseDigitalSignature(true)
        val builder = ViewerConfig.Builder()
        return builder
                .multiTabEnabled(true)
                .showCloseTabOption(false)
                .pdfViewCtrlConfig(pdfviewCtrlConfig)
                .toolManagerBuilder(toolManagerBuilder)
                .build()
    }

}