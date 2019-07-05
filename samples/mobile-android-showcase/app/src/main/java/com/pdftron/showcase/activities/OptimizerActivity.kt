package com.pdftron.showcase.activities

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*
import kotlinx.android.synthetic.main.control_button_simple.*


class OptimizerActivity : FeatureActivity() {

    private lateinit var mFileSizeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        showTab = true
        super.onCreate(savedInstanceState)

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)

        addControl()
    }

    private fun addControl() {
        mFileSizeText = TextView(this, null, 0, R.style.RobotoTextViewStyle).apply {
            val lp = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT)
                    .apply {
                        setMargins(0, resources.getDimension(R.dimen.content_text_top_margin).toInt(), 0, 0)
                    }
            layoutParams = lp
        }

        button.text = getString(R.string.optimize_basic_title)
        button.setOnClickListener {
            mPdfViewCtrlTabHostFragment!!.onSaveOptimizedCopySelected()
        }
        control_button_simple.addView(mFileSizeText)
    }

    override fun handleTabDocumentLoaded(tag: String) {
        updateFileSize()
    }

    override fun handleTabChanged(p0: String?) {
        updateFileSize()
    }

    private fun updateFileSize() {
        val fileSize = getPdfViewCtrlTabFragment()!!.currentFileSize
        val fileSizeStr = Utils.humanReadableByteCount(if (fileSize < 0) 0 else fileSize, false)
        mFileSizeText.text = getString(R.string.current_file_size, fileSizeStr)
    }
}