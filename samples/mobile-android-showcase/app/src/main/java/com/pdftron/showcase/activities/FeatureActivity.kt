package com.pdftron.showcase.activities

import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import com.pddstudio.highlightjs.HighlightJsView
import com.pddstudio.highlightjs.models.Language
import com.pddstudio.highlightjs.models.Theme
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.config.ToolManagerBuilder
import com.pdftron.pdf.config.ViewerBuilder
import com.pdftron.pdf.config.ViewerConfig
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment
import com.pdftron.pdf.controls.PdfViewCtrlTabHostFragment
import com.pdftron.pdf.model.FileInfo
import com.pdftron.pdf.tools.ToolManager
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.pdf.utils.PdfViewCtrlTabsManager
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.CustomBottomSheetBehavior
import com.pdftron.showcase.R
import com.pdftron.showcase.helpers.Helpers.fromHtml
import com.pdftron.showcase.models.Feature
import kotlinx.android.synthetic.main.activity_feature.*
import kotlinx.android.synthetic.main.content_bottom_sheet.*

open class FeatureActivity : AppCompatActivity() {

    protected lateinit var feature: Feature
    protected var mPdfViewCtrlTabHostFragment: PdfViewCtrlTabHostFragment? = null
        private set
    var showTab = false
    var needSetUpSample = true
    var autoSelect = true
    var annotationLayerEnabled = false
    var highlightJsView: HighlightJsView? = null
    var nightMode = false
    var mCodeSnippet = ""
    var cardState = View.VISIBLE
    var fullScreenMode = true
    var sampleFileName = "sample"
    protected var mToolbarVisible = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIncomingIntent()
        setContentView(R.layout.activity_feature)
        if (needSetUpSample) {
            setUpSampleView(sampleFileName)
        }

        setBottomSheetContent()
        adjustBottomSheetPadding(mToolbarVisible)
        //change bottom sheet full screen margin to status bar height + action bar height
        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (fullScreenMode) {
                if (visibility == 0) {
                    //System bars are visible
                    adjustBottomSheetPadding(mToolbarVisible)
                    if (cardState == View.VISIBLE) {
                        openBottomSheet()
                    } else {
                        closeBottomSheet()
                    }
                } else if (visibility == 6) {
                    adjustBottomSheetPadding(false)
                    cardState = bottom_sheet.visibility
                    bottom_sheet_ctrl_button.visibility(View.INVISIBLE)
                    bottom_sheet.visibility = View.INVISIBLE
                }
            }
        }

        close_bottom_card_btn.setOnClickListener {
            closeBottomSheet()
        }

        bottom_sheet_ctrl_button.setOnClickListener {
            openBottomSheet()
        }

        see_code_btn.setOnClickListener {
            CustomBottomSheetBehavior.from(bottom_sheet).setState(CustomBottomSheetBehavior.STATE_EXPANDED)

        }

        open_doc_btn.setOnClickListener {
            launchWebView()
        }

        CustomBottomSheetBehavior.from(bottom_sheet).setBottomSheetCallback(object : CustomBottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == CustomBottomSheetBehavior.STATE_COLLAPSED) {
                    scroll_view.scrollY = 0
                    see_code_btn.visibility = View.VISIBLE
                    open_doc_btn.visibility = View.VISIBLE
                    open_doc_description.visibility = View.INVISIBLE
                    code_snippet_container.visibility = View.INVISIBLE
                    share_button.visibility = View.INVISIBLE
                    color_button.visibility = View.INVISIBLE
                } else {
                    code_snippet_container.visibility = View.VISIBLE
                    open_doc_description.visibility = View.VISIBLE
                    share_button.visibility = View.VISIBLE
                    color_button.visibility = View.VISIBLE
                    see_code_btn.visibility = View.INVISIBLE
                    open_doc_btn.visibility = View.INVISIBLE
                }
                onBottomSheetStateChanged(bottomSheet, newState)
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        })

        calculateBottomSheet()
    }

    protected open fun onBottomSheetStateChanged(bottomSheet: View, newState: Int) {

    }

    open fun calculateBottomSheet() {
        bottom_sheet_header.viewTreeObserver.addOnGlobalLayoutListener {
            CustomBottomSheetBehavior.from(bottom_sheet).setPeekHeight(calculateHeight() + getNavigationBarHeight())
            CustomBottomSheetBehavior.from(bottom_sheet).setMinHeight(bottom_sheet_header.height + getNavigationBarHeight())
        }
    }

    protected fun openBottomSheet() {
        bottom_sheet.visibility = View.VISIBLE
        bottom_sheet_ctrl_button.visibility(View.INVISIBLE)
    }

    protected fun closeBottomSheet() {
        bottom_sheet.visibility = View.INVISIBLE
        bottom_sheet_ctrl_button.visibility(View.VISIBLE)
    }

    fun adjustBottomSheetPadding(toolbarVisible: Boolean) {
        val layoutParams = bottom_sheet_container.layoutParams as CoordinatorLayout.LayoutParams
        if (toolbarVisible) {
            layoutParams.setMargins(0, getStatusBarHeight() + getActionBarHeight(), 0, 0)
            bottom_sheet_container.layoutParams = layoutParams
        } else {
            layoutParams.setMargins(0, 0, 0, 0)
            bottom_sheet_container.layoutParams = layoutParams
        }
    }

    open fun getViewerConfig(): ViewerConfig {
        val toolManagerBuilder = ToolManagerBuilder.from()
                .setAnnotationLayerEnabled(annotationLayerEnabled)
                .setAutoSelect(autoSelect)
        val builder = ViewerConfig.Builder()
        return builder
                .multiTabEnabled(showTab)
                .fullscreenModeEnabled(fullScreenMode)
                .showCloseTabOption(false)
                .saveCopyExportPath(this.filesDir.absolutePath)
                .openUrlCachePath(this.filesDir.absolutePath)
                .toolManagerBuilder(toolManagerBuilder)
                .build()
    }

    open fun createPdfViewerFragment(fileName: String): PdfViewCtrlTabHostFragment {
        return ViewerBuilder.withUri(getFileUriFromName(fileName))
                .usingNavIcon(R.drawable.ic_arrow_back_white_24dp)
                .usingConfig(getViewerConfig())
                .build(this)
    }

    fun setUpSampleView(fileName: String) {
        // clear tabs
        PdfViewCtrlTabsManager.getInstance().cleanup()
        PdfViewCtrlTabsManager.getInstance().clearAllPdfViewCtrlTabInfo(this)

        mPdfViewCtrlTabHostFragment = createPdfViewerFragment(fileName)
        mPdfViewCtrlTabHostFragment!!.setToolbarTimerDisabled(true)
        mPdfViewCtrlTabHostFragment!!.addHostListener(object : PdfViewCtrlTabHostFragment.TabHostListener {
            override fun onNavButtonPressed() {
                finish()
            }

            override fun onExitSearchMode() {
                handleExitSearchMode()
            }

            override fun onToolbarOptionsItemSelected(p0: MenuItem?): Boolean {
                return false
            }

            override fun onTabHostShown() {
                mPdfViewCtrlTabHostFragment?.currentPdfViewCtrlFragment?.setSavingEnabled(false)
                mPdfViewCtrlTabHostFragment?.currentPdfViewCtrlFragment?.setStateEnabled(false)
            }

            override fun canShowFileInFolder(): Boolean {
                return false
            }

            override fun onStartSearchMode() {
                handleStartSearchMode()
            }

            override fun canShowFileCloseSnackbar(): Boolean {
                return false
            }

            override fun onToolbarPrepareOptionsMenu(p0: Menu?): Boolean {
                return false
            }

            override fun onTabChanged(p0: String?) {
                mPdfViewCtrlTabHostFragment?.currentPdfViewCtrlFragment?.setSavingEnabled(false)
                mPdfViewCtrlTabHostFragment?.currentPdfViewCtrlFragment?.setStateEnabled(false)

                handleTabChanged(p0)
            }

            override fun onOpenDocError() {

            }

            override fun onTabHostHidden() {

            }

            override fun onTabPaused(p0: FileInfo?, p1: Boolean) {

            }

            override fun canRecreateActivity(): Boolean {
                return true
            }

            override fun onJumpToSdCardFolder() {

            }

            override fun onToolbarCreateOptionsMenu(p0: Menu?, p1: MenuInflater?): Boolean {
                return false
            }

            override fun onLastTabClosed() {

            }

            override fun onShowFileInFolder(p0: String?, p1: String?, p2: Int) {

            }

            override fun onTabDocumentLoaded(tag: String) {
                handleTabDocumentLoaded(tag)
            }

        })

        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, mPdfViewCtrlTabHostFragment!!, null)
        ft.commit()

        // stop auto hide toolbars
        mPdfViewCtrlTabHostFragment!!.stopHideToolbarsTimer()
    }

    protected open fun handleTabDocumentLoaded(tag: String) {

    }

    protected open fun handleStartSearchMode() {

    }

    protected open fun handleExitSearchMode() {

    }

    protected open fun handleTabChanged(p0: String?) {

    }

    fun getFileUriFromName(fileName: String): Uri {
        val res = applicationContext.resources
        val fileId = res.getIdentifier(fileName, "raw", applicationContext.packageName)
        val file = Utils.copyResourceToLocal(this, fileId, fileName, ".pdf")
        return Uri.fromFile(file)
    }

    fun calculateHeight(): Int {
        return bottom_sheet_header.height + peek_container.height + see_code_btn.height + open_doc_btn.height
    }

    fun getDrawableFromName(fileName: String): Drawable {
        val resourceId = Utils.getResourceDrawable(this, fileName)
        return resources.getDrawable(resourceId)
    }

    //go back to explore page when back button in action bar is clicked
    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (item?.itemId == android.R.id.home) {
            onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()

        mPdfViewCtrlTabHostFragment?.closeAllTabs()
        PdfViewCtrlSettingsManager.setColorMode(this, PdfViewCtrlSettingsManager.KEY_PREF_COLOR_MODE_NORMAL)
        PdfViewCtrlSettingsManager.updateViewMode(this, PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_SINGLEPAGE_VALUE)
    }

    private fun launchWebView() {
        val webPage = Intent(this, WebviewActivity::class.java)
        webPage.putExtra("url", feature.link)
        startActivity(webPage)
    }

    private fun setBottomSheetContent() {
        feature_title.text = feature.name
        feature_description.text = feature.cardDescription

        //intercept click event to launch in app browser
        val newString = feature.codeSnippetDescription?.replace("documentation", "<a href=\"" + feature.link + "\"><b>" + "DOCUMENTATION" + "</b></a>")
        val htmlString = fromHtml(newString!!)
        val strBuilder = SpannableStringBuilder(htmlString)
        val url = strBuilder.getSpans(0, htmlString.length, URLSpan::class.java)
        val start = strBuilder.getSpanStart(url[0])
        val end = strBuilder.getSpanEnd(url[0])
        val clickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                launchWebView()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        strBuilder.setSpan(clickable, start, end, 0)
        strBuilder.removeSpan(url[0])


        open_doc_description.text = strBuilder
        open_doc_description.linksClickable = true
        open_doc_description.movementMethod = LinkMovementMethod.getInstance()

        share_button.setOnClickListener {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, concatStringLines(feature.codeSnippet))
                type = "text/plain"
            }
            startActivity(sendIntent)
        }

        color_button.setOnClickListener { changeCodeSnippetColor() }

        highlightJsView = findViewById(R.id.code_snippet)
        highlightJsView!!.theme = Theme.ATELIER_DUNE_LIGHT
        highlightJsView!!.highlightLanguage = Language.JAVA
        highlightJsView!!.setZoomSupportEnabled(true)
        highlightJsView!!.setShowLineNumbers(true)
        highlightCodeSnippet(feature.codeSnippet)
    }

    private fun changeCodeSnippetColor() {
        if (!nightMode) {
            nightMode = true
            color_button.setImageResource(R.drawable.ic_mode_day_black_24dp)
            share_button.setColorFilter(Color.argb(255, 255, 255, 255))
            color_button.setColorFilter(Color.argb(255, 255, 255, 255))
            highlightJsView!!.theme = Theme.ATELIER_DUNE_DARK
            highlightJsView!!.refresh()
            code_snippet_container.setCardBackgroundColor(resources.getColor(R.color.codeSnippet_night, null))
            codeSnippetBar.setBackgroundResource(R.color.codeSnippet_night)

        } else {
            nightMode = false
            color_button.setImageResource(R.drawable.ic_mode_night_black_24dp)
            share_button.setColorFilter(Color.argb(255, 0, 0, 0))
            color_button.setColorFilter(Color.argb(255, 0, 0, 0))
            highlightJsView!!.theme = Theme.ATELIER_DUNE_LIGHT
            highlightJsView!!.refresh()
            code_snippet_container.setCardBackgroundColor(resources.getColor(R.color.codeSnippet_day, null))
            codeSnippetBar.setBackgroundResource(R.color.codeSnippet_day)
        }
    }

    open fun highlightCodeSnippet(codeSnippet_: ArrayList<String>) {
        var codeSnippet = codeSnippet_.joinToString("\n")
        mCodeSnippet = codeSnippet
        highlightJsView!!.setSource(codeSnippet)
        highlightJsView!!.refresh()
    }

    open fun highlightCodeSnippet(codeSnippet_string: String) {
        mCodeSnippet = codeSnippet_string
        highlightJsView!!.setSource(codeSnippet_string)
        highlightJsView!!.refresh()
    }

    private fun getIncomingIntent() {
        if (intent.hasExtra("feature")) {
            feature = intent.extras!!.get("feature") as Feature
        }
    }

    fun getPdfViewCtrlTabFragment(): PdfViewCtrlTabFragment? {
        return mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment
    }

    fun getPDFViewCtrl(): PDFViewCtrl? {
        return mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment.pdfViewCtrl
    }

    fun getToolManager(): ToolManager? {
        return mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment.toolManager
    }

    fun getPDFDoc(): PDFDoc? {
        return mPdfViewCtrlTabHostFragment!!.currentPdfViewCtrlFragment.pdfDoc
    }

    fun getStatusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId)
        }
        return 0
    }

    fun getActionBarHeight(): Int {
        val styledAttributes = this.theme.obtainStyledAttributes(
                intArrayOf(android.R.attr.actionBarSize)
        )
        val actionBarHeight = styledAttributes.getDimension(0, 0f).toInt()
        styledAttributes.recycle()

        return actionBarHeight
    }

    private fun getNavigationBarHeight(): Int {
        if (!this.fullScreenMode || !hasNavBar(resources)) {
            return 0
        }
        val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
        if (resourceId > 0) {
            return resources.getDimensionPixelSize(resourceId)
        }
        return 0
    }

    private fun hasNavBar(resources: Resources): Boolean {
        if (Utils.isEmulator()) {
            return true
        }
        val id = resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return id > 0 && resources.getBoolean(id)
    }

    private fun concatStringLines(strings: List<String>): String {
        val strBuilder = StringBuilder()
        for (str in strings) {
            strBuilder.appendln(str)
        }
        return strBuilder.toString()
    }
}

fun FloatingActionButton.visibility(visibility: Int) {
    if (visibility == View.VISIBLE) {
        show()
    } else {
        hide()
    }
}
