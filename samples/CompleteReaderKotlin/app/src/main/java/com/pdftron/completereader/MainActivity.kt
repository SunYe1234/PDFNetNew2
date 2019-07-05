package com.pdftron.completereader

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import com.pdftron.common.PDFNetException
import com.pdftron.demo.app.AdvancedReaderActivity
import com.pdftron.demo.app.SimpleReaderActivity
import com.pdftron.pdf.PDFNet
import com.pdftron.pdf.config.PDFViewCtrlConfig
import com.pdftron.pdf.config.ToolManagerBuilder
import com.pdftron.pdf.config.ViewerConfig
import com.pdftron.pdf.controls.DiffActivity
import com.pdftron.pdf.utils.AppUtils
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.pdf.utils.Utils

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Utils.isLollipop()) {
            window.statusBarColor = Color.BLACK
        }

        if (supportActionBar != null) {
            supportActionBar!!.title = "PDFNet Demo"
        }

        try {
            PDFNet.initialize(this, R.raw.pdfnet, AppUtils.getLicenseKey(applicationContext)!!)
        } catch (e: PDFNetException) {
            showLicenseRequestDialog()
        }

        val simpleReaderButton = findViewById<Button>(R.id.simpleReaderButton)
        simpleReaderButton.setOnClickListener { openSimpleReaderActivity() }

        val simpleReaderImage = findViewById<ImageView>(R.id.simpleReaderImage)
        simpleReaderImage.setOnClickListener { openSimpleReaderActivity() }

        val completeReaderButton = findViewById<Button>(R.id.completeReaderButton)
        completeReaderButton.setOnClickListener { openCompleteReaderActivity() }

        val completeReaderImage = findViewById<ImageView>(R.id.completeReaderImage)
        completeReaderImage.setOnClickListener { openCompleteReaderActivity() }

        val diffButton = findViewById<Button>(R.id.diffButton)
        diffButton.setOnClickListener { openDiffActivity() }

        val diffImage = findViewById<ImageView>(R.id.diffImage)
        diffImage.setOnClickListener { openDiffActivity() }
    }

    private fun showLicenseRequestDialog() {
        val dialog = AlertDialog.Builder(this)
                .setTitle(R.string.missing_license_key)
                .setMessage(Html.fromHtml(getString(R.string.missing_license_key_msg)))
                .setCancelable(false)
                .create()
        dialog.show()
        (dialog.findViewById<View>(android.R.id.message) as TextView).movementMethod = LinkMovementMethod.getInstance()
    }

    private fun openDiffActivity() {
        DiffActivity.open(this, R.raw.diff_doc_1, R.raw.diff_doc_2)
    }

    private fun openCompleteReaderActivity() {
        PdfViewCtrlSettingsManager.setMultipleTabs(this, true)
        AdvancedReaderActivity.setDebug(BuildConfig.DEBUG)
        AdvancedReaderActivity.open(this)
    }

    private fun openSimpleReaderActivity() {
        val tmBuilder = ToolManagerBuilder.from()
                .setUseDigitalSignature(false)
                .setAutoResizeFreeText(false)
        var cutoutMode = 0
        if (Utils.isPie()) {
            cutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        var builder = ViewerConfig.Builder()
        builder = builder
                .fullscreenModeEnabled(true)
                .multiTabEnabled(true)
                .documentEditingEnabled(true)
                .longPressQuickMenuEnabled(true)
                .showPageNumberIndicator(true)
                .showBottomNavBar(true)
                .showThumbnailView(true)
                .showBookmarksView(true)
                .showSearchView(true)
                .showShareOption(true)
                .showDocumentSettingsOption(true)
                .showAnnotationToolbarOption(true)
                .showOpenFileOption(true)
                .showOpenUrlOption(true)
                .showEditPagesOption(true)
                .showPrintOption(true)
                .showCloseTabOption(true)
                .showAnnotationsList(true)
                .showOutlineList(true)
                .showUserBookmarksList(true)
                .pdfViewCtrlConfig(PDFViewCtrlConfig.getDefaultConfig(this))
                .toolManagerBuilder(tmBuilder)
        if (Utils.isPie()) {
            builder = builder.layoutInDisplayCutoutMode(cutoutMode)
        }
        val config = builder.build()
        SimpleReaderActivity.open(this, config)
    }
}
