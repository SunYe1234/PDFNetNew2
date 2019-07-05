package com.pdftron.showcase.activities

import android.os.Bundle
import android.util.SparseArray
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.TextExtractor
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_extraction.*

open class TextExtractionActivity : FeatureActivity(), PDFViewCtrl.PageChangeListener {

    // Disposables
    private var mDisposables: CompositeDisposable? = null

    protected open val sFileName = "newsletter"

    private val mTextCache : SparseArray<String> = SparseArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = sFileName
        super.onCreate(savedInstanceState)

        mDisposables = CompositeDisposable()

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_extraction, bottomSheetContainer, true)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        setup()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!(mDisposables?.isDisposed!!)) {
            mDisposables?.dispose()
        }
    }

    private fun setup() {
        addControl()
    }

    protected open fun addControl() {
        getPDFViewCtrl()!!.addPageChangeListener(this)
    }

    override fun onPageChange(old_page: Int, cur_page: Int, state: PDFViewCtrl.PageChangeState?) {
        if (state == PDFViewCtrl.PageChangeState.END) {
            updateText(cur_page)
        }
    }

    private fun updateText(pageNum: Int) {
        mDisposables!!.add(extract(pageNum)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (pageNum == getPDFViewCtrl()!!.currentPage) {
                        extraction_text.text = it
                    }
                }, {

                })
        )
    }

    private fun extractText(pageNum: Int): String {
        if (!Utils.isNullOrEmpty(mTextCache[pageNum])) {
            return mTextCache[pageNum]
        }
        val pdfViewCtrl = getPDFViewCtrl()
        val pdfDoc = getPDFDoc()
        val txt = TextExtractor()
        var shouldUnlockRead = false
        try {
            pdfViewCtrl!!.docLockRead()
            shouldUnlockRead = true
            // extract text
            val page = pdfDoc!!.getPage(pageNum)
            txt.begin(page)
            val result = txt.asText
            mTextCache.put(pageNum, result)
            return result
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl?.docUnlockRead()
            }
            txt.destroy()
        }
        return ""
    }

    private fun extract(pageNum: Int): Single<String> {
        return Single.fromCallable {
            return@fromCallable extractText(pageNum)
        }
    }
}