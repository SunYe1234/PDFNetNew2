package com.pdftron.showcase.activities

import android.net.Uri
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.Page
import com.pdftron.pdf.controls.PdfViewCtrlTabFragment
import com.pdftron.pdf.model.BaseFileInfo
import com.pdftron.pdf.utils.PdfViewCtrlSettingsManager
import com.pdftron.pdf.utils.Utils
import com.pdftron.sdf.SDFDoc
import com.pdftron.showcase.R
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_bottomsheet_button.*
import org.apache.commons.io.FilenameUtils
import java.io.File

class MergingActivity : DocumentTabbedViewerActivity() {

    override var files = arrayOf("invoice", "contract")

    private var mMergedFile : File? = null
    private val resultFile = "Merged File.pdf"

    override fun addControl() {
        button.text = getString(R.string.merge)
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_merge_type_black_24dp, 0, 0, 0)
        button.setOnClickListener {
            merge()
        }
    }

    private fun merge() {
        if (null != mMergedFile) {
            mPdfViewCtrlTabHostFragment?.removeTabAt(2)
        }
        mDisposables!!.add(mergeFiles()
        !!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    mMergedFile = it
                    var name = FilenameUtils.getName(it.absolutePath)
                    name = FilenameUtils.removeExtension(name)
                    val tabFile = PdfViewCtrlTabFragment.createBasicPdfViewCtrlTabBundle(this, Uri.fromFile(it), null)
                    mPdfViewCtrlTabHostFragment!!.addTab(tabFile, name, Utils.capitalize(name), "pdf", null, BaseFileInfo.FILE_TYPE_FILE)
                    mPdfViewCtrlTabHostFragment!!.setCurrentTabByTag(name)
                }, {
                    //TODO error handling
                })
        )
    }

    override fun handleTabDocumentLoaded(tag: String) {
        if (tag.contains(resultFile)) {
            mPdfViewCtrlTabHostFragment!!.onViewModeSelected(PdfViewCtrlSettingsManager.KEY_PREF_VIEWMODE_FACING_VALUE)
        }
    }

    private fun mergeFiles(): Single<File>? {
        return Single.fromCallable {
            return@fromCallable merge(mFileArray!!, filesDir, resultFile)
        }
    }

    fun merge(filesToMerge: List<Uri>, outputFolder: File, outputFilename: String): File? {
        // Define the merged document and output files
        var mergedDoc: PDFDoc? = null
        var inDoc: PDFDoc? = null
        var mergedFile: File? = null

        var shouldUnlockRead = false
        try {
            // Create merged document so we can pages toit
            mergedDoc = PDFDoc()
            mergedDoc.initSecurityHandler()
            for (file in filesToMerge) {
                // Add pages from inDoc to merged doc
                inDoc = PDFDoc(file.path)
                inDoc.lockRead()
                addPages(inDoc, mergedDoc)

                // Close and unlock inDoc
                inDoc.unlockRead()
                shouldUnlockRead = false
                Utils.closeQuietly(inDoc)
                inDoc = null
            }
            // Create the output merged file
            mergedFile = File(outputFolder, outputFilename)
            mergedDoc.save(mergedFile.absolutePath, SDFDoc.SaveMode.REMOVE_UNUSED, null)
        } catch (e: PDFNetException) {
            e.printStackTrace()
        } finally {
            if (shouldUnlockRead) {
                Utils.unlockReadQuietly(inDoc)
            }
            Utils.closeQuietly(inDoc)
            Utils.closeQuietly(mergedDoc)
        }
        return mergedFile
    }

    // Add pages from one file to another
    @Throws(PDFNetException::class)
    private fun addPages(inDoc: PDFDoc, mergedDoc: PDFDoc) {
        // Grab the pages from the input doc
        val copyPages = arrayOfNulls<Page>(inDoc.pageCount)
        val iterator = inDoc.pageIterator
        var j = 0
        while (iterator.hasNext()) {
            copyPages[j++] = iterator.next()
        }
        val importedPages = mergedDoc.importPages(copyPages, true)

        // Add imported pages to the output doc's page sequence
        for (page in importedPages) {
            mergedDoc.pagePushBack(page)
        }
    }
}