package com.pdftron.showcase.activities

import android.net.Uri
import android.support.v4.app.DialogFragment
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment
import com.pdftron.pdf.dialog.diffing.DiffOptionsDialogFragment
import com.pdftron.pdf.dialog.diffing.DiffUtils
import com.pdftron.pdf.model.BaseFileInfo
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_bottomsheet_button.*
import org.apache.commons.io.FilenameUtils
import java.io.File

class PageDiffingActivity : DocumentTabbedViewerActivity() {

    override var files = arrayOf("diff_doc_1", "diff_doc_2")
    private var mResultFile: Uri? = null
    private val resultFile = "compare-result.pdf"

    private val mDisposable = CompositeDisposable()

    override fun onDestroy() {
        super.onDestroy()

        if (!(mDisposables?.isDisposed!!)) {
            mDisposables?.dispose()
        }
    }

    override fun addControl() {
        button.text = getString(R.string.diff_compare)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_compare_arrows_black_24dp, 0, 0, 0)
        button.setOnClickListener {
            compare()
        }
    }

    private fun compare() {
        if (mFileArray!!.size >= 2) {
            val fragment = DiffOptionsDialogFragment.newInstance(
                    mFileArray!![0], mFileArray!![1]
            )
            fragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomAppTheme)
            fragment.setDiffOptionsDialogListener { color1, color2, blendMode ->
                compareFile(color1, color2, blendMode)
            }
            fragment.show(supportFragmentManager, DiffOptionsDialogFragment.TAG)
        }
    }

    private fun compareFile(color1: Int, color2: Int,
                            blendMode: Int) {
        if (null != mResultFile) {
            mPdfViewCtrlTabHostFragment?.removeTabAt(2)
        }
        val resultFile = File(this.filesDir, resultFile)
        mDisposable.add(DiffUtils.compareFiles(this, mFileArray, color1, color2, blendMode, resultFile)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ uri ->
                    mResultFile = uri
                    var name = FilenameUtils.getName(uri.path)
                    name = FilenameUtils.removeExtension(name)
                    val tabFile = PdfViewCtrlTabFragment.createBasicPdfViewCtrlTabBundle(this, uri, null)
                    mPdfViewCtrlTabHostFragment!!.addTab(tabFile, name, Utils.capitalize(name), "pdf", null, BaseFileInfo.FILE_TYPE_FILE)
                    mPdfViewCtrlTabHostFragment!!.setCurrentTabByTag(name)
                }, { }))
    }
}