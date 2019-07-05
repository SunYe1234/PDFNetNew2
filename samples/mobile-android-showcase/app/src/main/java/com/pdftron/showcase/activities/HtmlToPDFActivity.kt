package com.pdftron.showcase.activities

import android.net.Uri
import android.util.Log
import android.view.View
import android.webkit.WebViewClient
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.utils.HTML2PDF
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_split_view.*

class HtmlToPDFActivity : FeatureWithComparisonActivity() {

    private val TAG = "HtmlToPDFActivity"

    override fun setUpViews() {
        super.setUpViews()
        left_web_view.visibility = View.VISIBLE
        titleL.text = getString(R.string.html_in_webview)
        titleR.text = getString(R.string.html_in_pdfviewctrl)
    }

    override fun setUpPdfCtrlViews() {
        super.setUpPdfCtrlViews()
        setUpDocs()
    }


    override fun resetSamples() {
        setUpDocs()
    }

    private fun setUpDocs() {

        val link = "https://developer.android.com/about/index.html"

        left_web_view.webViewClient = WebViewClient()
        left_web_view.loadUrl(link)

        rightPdfViewCtrl?.closeDoc()
        rightPdfViewCtrl?.invalidate()
        rightPdfViewCtrl?.clearThumbCache()
        rightPdfDoc?.close()

        val listener = object : HTML2PDF.HTML2PDFListener {
            override fun onConversionFinished(pdfOutput: String, isLocal: Boolean) {
                rightPdfDoc = PDFDoc(pdfOutput)
                rightPdfViewCtrl?.doc = rightPdfDoc
            }

            override fun onConversionFailed(error: String?) {
                Log.e(TAG, error)
            }
        }
        HTML2PDF.fromUrl(this, link, Uri.fromFile(this.filesDir), "index", listener)
    }

}