package com.pdftron.showcase.activities

import android.os.Bundle
import android.widget.RadioButton
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_display_modes.*


class DisplayModeActivity : FeatureActivity() {

    private val TAG = "DisplayModeActivity"
    private var transitionId = 0
    private var layoutId = 0
    private var zoomId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_display_modes, bottomSheetContainer, true)
        addDisplayModeControls()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        updateCodeSnippet()
    }

    private fun addDisplayModeControls() {
        fit_page.isChecked = true
        single_display.isChecked = true
        single_page.isChecked = true
        rg1.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            changeTransition(rb.tag.toString().toInt())
        }

        rg2.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            changeLayout(rb.tag.toString().toInt())
        }

        rg3.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            changeFitMode(rb.tag.toString().toInt())
        }
    }

    private fun changeTransition(checkedId: Int) {
        // set facing cover continuous mode to page presentation mode.
        transitionId = checkedId
        updateView()
    }

    private fun changeLayout(checkedId: Int) {
        layoutId = checkedId
        updateView()
    }

    private fun changeFitMode(checkedId: Int) {
        zoomId = checkedId
        when (checkedId) {
            0 -> getPDFViewCtrl()!!.pageViewMode = PDFViewCtrl.PageViewMode.FIT_WIDTH
            1 -> getPDFViewCtrl()!!.pageViewMode = PDFViewCtrl.PageViewMode.FIT_PAGE
            else -> throw RuntimeException("Undefined fit mode")
        }
        updateCodeSnippet()
    }

    private fun updateView() {
        if (transitionId == 0 && layoutId == 0) getPDFViewCtrl()!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.SINGLE
        if (transitionId == 0 && layoutId == 1) getPDFViewCtrl()!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.FACING
        if (transitionId == 0 && layoutId == 2) getPDFViewCtrl()!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.FACING_COVER
        if (transitionId == 1 && layoutId == 0) getPDFViewCtrl()!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.SINGLE_CONT
        if (transitionId == 1 && layoutId == 1) getPDFViewCtrl()!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.FACING_CONT
        if (transitionId == 1 && layoutId == 2) getPDFViewCtrl()!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.FACING_COVER_CONT
        updateCodeSnippet()
    }

    private fun updateCodeSnippet() {
        val value = transitionId * 10 + layoutId
        val displayId = if (value >= 10) (value % 10) + 3 else value
        val codeLayout = feature.codeSnippets!![displayId].code
        val codeZoom = feature.codeSnippets!![zoomId + 6].code
        val code = codeLayout.joinToString("\n") + "\n" + codeZoom.joinToString("\n")
        highlightCodeSnippet(code)
    }
}