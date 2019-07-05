package com.pdftron.showcase.activities

import android.net.Uri
import android.os.Bundle
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment
import com.pdftron.pdf.model.BaseFileInfo
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.content_bottomsheet_button.*
import org.apache.commons.io.FilenameUtils


open class DocumentTabbedViewerActivity : FeatureActivity() {

    protected open var files = arrayOf("math_formular", "music_sheet", "construction_drawing")
    protected var mFileArray: ArrayList<Uri>? = null

    // Disposables
    protected var mDisposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        showTab = true
        super.onCreate(savedInstanceState)
        mDisposables = CompositeDisposable()
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_button_simple, bottomSheetContainer, true)
        addControl()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        addTabs()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!(mDisposables?.isDisposed!!)) {
            mDisposables?.dispose()
        }
    }

    protected open fun addControl() {
        button.text = getString(R.string.reset_tabs)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_reset, 0, 0, 0)
        button.setOnClickListener {
            addTabs()
        }
    }

    private fun addTabs() {
        mDisposables!!.add(loadFiles()
        !!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mFileArray = it
                    mPdfViewCtrlTabHostFragment!!.closeAllTabs()
                    for (path in it) {
                        var name = FilenameUtils.getName(path.path)
                        name = FilenameUtils.removeExtension(name)
                        val tabFile = PdfViewCtrlTabFragment.createBasicPdfViewCtrlTabBundle(this, path, null)
                        mPdfViewCtrlTabHostFragment!!.addTab(tabFile, name, Utils.capitalize(name), "pdf", null, BaseFileInfo.FILE_TYPE_FILE)
                    }
                    mPdfViewCtrlTabHostFragment!!.setCurrentTabByTag(files[0])
                }, {
                    //TODO error handling
                })
        )
    }

    private fun loadFiles(): Single<ArrayList<Uri>>? {
        return Single.fromCallable {
            val fileArray: ArrayList<Uri> = ArrayList<Uri>(files.size)
            for (file in files) {
                val path = getFileUriFromName(file)
                fileArray.add(path)
            }
            return@fromCallable fileArray
        }
    }
}