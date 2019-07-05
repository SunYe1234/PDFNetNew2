package com.pdftron.showcase.activities

import android.net.Uri
import android.os.Bundle
import android.widget.RadioButton
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment
import com.pdftron.pdf.model.BaseFileInfo
import com.pdftron.pdf.utils.Utils
import com.pdftron.showcase.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_officetopdf.*
import org.apache.commons.io.FilenameUtils

class OfficeToPDFActivity : FeatureActivity() {

    private var files = arrayOf(
            Pair("bloodmeridianreport.docx", "Word"), // file name and tab title pair
            Pair("roderick.pptx", "PowerPoint"),
            Pair("chart_supported.xlsx", "Excel")
    )

    // Disposables
    private var mDisposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        showTab = true
        super.onCreate(savedInstanceState)
        mDisposables = CompositeDisposable()
        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_officetopdf, bottomSheetContainer, true)
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

    private fun addControl() {
        office_files.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            val index = rb.tag.toString().toInt()
            mPdfViewCtrlTabHostFragment!!.setCurrentTabByTag(files[index].first)
        }

        office_files.check(R.id.default_selection)
    }

    private fun addTabs() {
        mDisposables!!.add(loadFiles()
                !!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mPdfViewCtrlTabHostFragment!!.closeAllTabs()
                    for (path in it) {
                        val uri = path.first
                        val extension = FilenameUtils.getExtension(uri.path)
                        val name = FilenameUtils.getName(uri.path)
                        val tabFile = PdfViewCtrlTabFragment.createBasicPdfViewCtrlTabBundle(this, uri, null)
                        mPdfViewCtrlTabHostFragment!!.addTab(tabFile, name, path.second, extension, null, BaseFileInfo.FILE_TYPE_FILE)
                    }
                    mPdfViewCtrlTabHostFragment!!.setCurrentTabByTag(files[0].first)
                }, {
                    //TODO error handling
                })
        )
    }

    private fun loadFiles(): Single<ArrayList<Pair<Uri, String>>>? {
        return Single.fromCallable {
            val fileArray : ArrayList<Pair<Uri, String>> = ArrayList(files.size)
            for (file in files) {
                val path = getOfficeUriFromName(file.first)
                fileArray.add(Pair(path, file.second))
            }
            return@fromCallable fileArray
        }
    }

    private fun getOfficeUriFromName(fileName: String): Uri {
        val extension = FilenameUtils.getExtension(fileName)
        val name = FilenameUtils.removeExtension(fileName)
        val res = applicationContext.resources
        val fileId = res.getIdentifier(name, "raw", applicationContext.packageName)
        val file = Utils.copyResourceToLocal(this, fileId, name, ".$extension")
        return Uri.fromFile(file)
    }
}