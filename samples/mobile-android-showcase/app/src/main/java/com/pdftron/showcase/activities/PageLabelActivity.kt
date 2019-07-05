package com.pdftron.showcase.activities

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.dialog.pagelabel.PageLabelDialog
import com.pdftron.pdf.dialog.pagelabel.PageLabelSetting
import com.pdftron.pdf.dialog.pagelabel.PageLabelSettingViewModel
import com.pdftron.pdf.dialog.pagelabel.PageLabelUtils
import com.pdftron.pdf.utils.CommonToast
import com.pdftron.pdf.utils.Event
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*
import kotlinx.android.synthetic.main.control_button_simple.*

class PageLabelActivity : FeatureActivity() {

    private lateinit var mFilePage: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addControl()
    }

    private fun addControl() {
        button.text = getString(R.string.page_label_button)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_edit_black_24dp, 0, 0, 0)

        val mPageLabelViewModel = ViewModelProviders.of(this).get(PageLabelSettingViewModel::class.java)

        mPageLabelViewModel.observeOnComplete(this, Observer<Event<PageLabelSetting>> { pageLabelSettingEvent ->
            if (pageLabelSettingEvent != null && !pageLabelSettingEvent.hasBeenHandled()) {
                val isSuccessful = PageLabelUtils.setPageLabel(getPDFViewCtrl()!!, pageLabelSettingEvent.contentIfNotHandled!!)
                if (isSuccessful) {
                    CommonToast.showText(this, getString(com.pdftron.pdf.tools.R.string.page_label_success), Toast.LENGTH_LONG)
                    updatePageTextView(getPDFViewCtrl()!!.currentPage)
                } else {
                    CommonToast.showText(this, getString(com.pdftron.pdf.tools.R.string.page_label_failed), Toast.LENGTH_LONG)
                }
            }
        })

        button.setOnClickListener {
            val page = getPDFViewCtrl()!!.currentPage
            val numPages = getPDFViewCtrl()!!.pageCount
            showPageLabelSettingsDialog(supportFragmentManager, page, page, numPages)
        }

        // Add text for page label text
        mFilePage = TextView(this, null, 0, R.style.RobotoTextViewStyle).apply {
            val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply {
                        setMargins(0, resources.getDimension(R.dimen.content_text_top_margin).toInt(), 0, 0)
                    }
            layoutParams = lp
        }

        getPDFViewCtrl()!!.addPageChangeListener { old_page, cur_page, state ->
            if (state == PDFViewCtrl.PageChangeState.END) {
                updatePageTextView(cur_page)
            }
        }
        control_button_simple.addView(mFilePage)
    }

    private fun updatePageTextView(currentPage: Int) {
        mFilePage.text = String.format(getString(R.string.page_label_text),
                PageLabelUtils.getPageNumberIndicator(getPDFViewCtrl()!!, currentPage))
    }

    fun showPageLabelSettingsDialog(fragmentManager: FragmentManager, fromPage: Int, toPage: Int, pageCount: Int) {
        val dialog = PageLabelDialog.newInstance(fromPage, toPage, pageCount)
        dialog.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.CustomAppTheme)
        dialog.show(fragmentManager, PageLabelDialog.TAG)
    }
}
