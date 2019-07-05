package com.pdftron.showcase.activities

import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.TextView
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.config.ToolManagerBuilder
import com.pdftron.pdf.utils.AppUtils
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.CustomBottomSheetBehavior
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.activity_feature.*
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*

open class FeatureWithComparisonActivity : FeatureActivity() {

    private val TAG = "FeatureWithComparisonActivity"
    private lateinit var splitViewFragment: SplitViewFragment
    var leftPdfViewCtrl: PDFViewCtrl? = null
    var rightPdfViewCtrl: PDFViewCtrl? = null
    var leftPdfDoc: PDFDoc? = null
    var rightPdfDoc: PDFDoc? = null
    lateinit var titleL: TextView
    lateinit var titleR: TextView
    lateinit var byteBarLeft: LinearLayout
    lateinit var byteBarRight: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        needSetUpSample = false
        mToolbarVisible = false
        super.onCreate(savedInstanceState)
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        setUpSplitView()
        addControl()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = feature.name
        supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        setUpViews()
        setUpPdfCtrlViews()
    }

    override fun calculateBottomSheet() {
        val layout = CoordinatorLayout.LayoutParams(CoordinatorLayout.LayoutParams.WRAP_CONTENT,
                CoordinatorLayout.LayoutParams.WRAP_CONTENT)
        layout.gravity = Gravity.BOTTOM or Gravity.END
        layout.bottomMargin = 120
        layout.marginEnd = 30
        bottom_sheet_ctrl_button.layoutParams = layout
        bottom_sheet_header.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                CustomBottomSheetBehavior.from(bottom_sheet).setPeekHeight(calculateHeight())
                CustomBottomSheetBehavior.from(bottom_sheet).setMinHeight(bottom_sheet_header.height)
            }
        })
        bottom_sheet.setPadding(0, 0, 0, 10)
    }

    open fun setUpViews() {
        leftPdfViewCtrl = findViewById<View>(R.id.left_ctrl_view) as PDFViewCtrl
        rightPdfViewCtrl = findViewById<View>(R.id.right_ctrl_view) as PDFViewCtrl
        val toolManagerL = ToolManagerBuilder.from().build(this, leftPdfViewCtrl!!)
        toolManagerL.isReadOnly = true
        toolManagerL.isBuiltInPageNumberIndicatorVisible = false
        leftPdfViewCtrl?.toolManager = toolManagerL
        val toolManagerR = ToolManagerBuilder.from().build(this, rightPdfViewCtrl!!)
        toolManagerR.isReadOnly = true
        toolManagerR.isBuiltInPageNumberIndicatorVisible = false
        rightPdfViewCtrl?.toolManager = toolManagerR
        titleL = findViewById(R.id.leftTitle)
        titleR = findViewById(R.id.rightTitle)
        byteBarLeft = findViewById(R.id.bytesTransferL)
        byteBarRight = findViewById(R.id.bytesTransferR)
    }

    private fun setUpSplitView() {
        splitViewFragment = SplitViewFragment()
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, splitViewFragment, null)
        ft.commit()
    }

    private fun addControl() {
        button.text = getString(R.string.reset_files)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reset, 0, 0, 0)
        button.setOnClickListener {
            resetSamples()
        }
    }

    open fun resetSamples() {

    }

    open fun setUpPdfCtrlViews() {
        try {
            AppUtils.setupPDFViewCtrl(leftPdfViewCtrl!!)
            leftPdfViewCtrl!!.setVerticalAlign(-1)
            leftPdfViewCtrl!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.SINGLE_CONT

            AppUtils.setupPDFViewCtrl(rightPdfViewCtrl!!)
            rightPdfViewCtrl!!.setVerticalAlign(-1)
            rightPdfViewCtrl!!.pagePresentationMode = PDFViewCtrl.PagePresentationMode.SINGLE_CONT
        } catch (e: PDFNetException) {
            // Handle exception
        }
    }

    open fun setUpDocs(leftResourceId: Int, leftFileName: String, rightResourceId: Int, rightFileName: String) {
        val file = Utils.copyResourceToLocal(this, leftResourceId, leftFileName, ".pdf")
        leftPdfDoc = PDFDoc(file.absolutePath)
        leftPdfViewCtrl!!.doc = leftPdfDoc

        val fileR = Utils.copyResourceToLocal(this, rightResourceId, rightFileName, ".pdf")
        rightPdfDoc = PDFDoc(fileR.absolutePath)
        rightPdfViewCtrl!!.doc = rightPdfDoc
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (leftPdfViewCtrl != null) {
            leftPdfViewCtrl?.destroy()
            leftPdfViewCtrl = null
        }

        if (leftPdfDoc != null) {
            try {
                leftPdfDoc?.close()
            } catch (e: Exception) {
                // handle exception
            } finally {
                leftPdfDoc = null
            }
        }

        if (rightPdfViewCtrl != null) {
            rightPdfViewCtrl?.destroy()
            rightPdfViewCtrl = null
        }

        if (rightPdfDoc != null) {
            try {
                rightPdfDoc?.close()
            } catch (e: Exception) {
                // handle exception
            } finally {
                rightPdfDoc = null
            }
        }
    }
}
