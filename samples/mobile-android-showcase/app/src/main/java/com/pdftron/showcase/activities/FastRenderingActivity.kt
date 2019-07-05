package com.pdftron.showcase.activities

import android.os.Bundle
import android.util.Log
import com.pdftron.pdf.PDFDoc
import com.pdftron.showcase.R
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class FastRenderingActivity : FeatureWithComparisonActivity() {

    private val TAG = "FastRenderActivity"

    // Disposables
    private var mDisposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDisposables = CompositeDisposable()
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!(mDisposables?.isDisposed!!)) {
            mDisposables?.dispose()
        }
    }

    override fun setUpViews() {
        super.setUpViews()
        titleL.text = getString(R.string.fast_rendering)
        titleR.text = getString(R.string.no_fast_rendering)
    }

    override fun setUpPdfCtrlViews() {
        super.setUpPdfCtrlViews()
        setUpThumbnailRendering()
        setUpDocs()
    }

    override fun resetSamples() {
        setUpDocs()
    }

    private fun setUpDocs() {

        val fileCopy = File(cacheDir, "thumb_output.pdf")

        mDisposables!!.add(loadFile(fileCopy)
        !!.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    leftPdfDoc = PDFDoc(fileCopy!!.absolutePath)
                    leftPdfViewCtrl!!.doc = leftPdfDoc

                    rightPdfDoc = PDFDoc(fileCopy!!.absolutePath)
                    rightPdfViewCtrl!!.doc = rightPdfDoc
                }, {
                    //TODO error handling
                })
        )
    }

    @Throws(IOException::class)
    fun copyToLocalCache(fileName: String, outputPath: File) {
        var input: InputStream? = null
        var output: FileOutputStream? = null
        if (!outputPath.exists()) {
            try {
                input = assets.open(fileName)
                output = FileOutputStream(outputPath)
                val buffer = ByteArray(1024)
                var size: Int = input!!.read(buffer)
                // Just copy the entire contents of the file
                while (size != -1) {
                    output!!.write(buffer, 0, size)
                    size = input!!.read(buffer)
                }
                input!!.close()
                output!!.close()
            } finally {
                try {
                    if (input != null) input!!.close()
                    if (output != null) output!!.close()
                } catch (e: IOException) {
                    Log.e("copyToLocalCache", "Issue closing file streams")
                }

            }
        }
    }

    private fun setUpThumbnailRendering() {
        leftPdfViewCtrl?.progressiveRendering = false
        rightPdfViewCtrl?.progressiveRendering = true
        leftPdfViewCtrl?.setupThumbnails(true, true, true, 1024, 1024 * 1024 * 100, 0.5)
        rightPdfViewCtrl?.setupThumbnails(false, false, false, 0, 0, 0.0)
    }

    private fun loadFile(file: File): Completable {
        return Completable.fromCallable {
            copyToLocalCache("map_optimized.pdf", file)
        }
    }

}