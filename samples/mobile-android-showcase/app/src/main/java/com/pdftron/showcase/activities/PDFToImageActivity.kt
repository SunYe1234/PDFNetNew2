package com.pdftron.showcase.activities

import android.os.Bundle
import android.support.v4.app.ShareCompat
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.pdftron.pdf.PDFDraw
import com.pdftron.pdf.utils.Utils
import com.pdftron.pdf.widget.recyclerview.ItemClickHelper
import com.pdftron.showcase.R
import com.pdftron.showcase.adapters.ImageExtractionAdapter
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import kotlinx.android.synthetic.main.control_recyclerview.*
import java.io.File

class PDFToImageActivity  : FeatureActivity() {

    // Disposables
    private var mDisposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mDisposables = CompositeDisposable()

        val bottomSheetContainer = feature_content_container
        layoutInflater.inflate(R.layout.control_recyclerview, bottomSheetContainer, true)
        recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recycler_view.setHasFixedSize(true)
        recycler_view.isNestedScrollingEnabled = false
    }

    override fun onDestroy() {
        super.onDestroy()

        if (!(mDisposables?.isDisposed!!)) {
            mDisposables?.dispose()
        }
    }

    override fun handleTabDocumentLoaded(tag: String) {
        super.handleTabDocumentLoaded(tag)

        updateImages()
    }

    private fun updateImages() {
        mDisposables!!.add(convert()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    recycler_view.visibility = View.VISIBLE
                    progress_bar.visibility = View.GONE
                    val itemClickHelper = ItemClickHelper()
                    itemClickHelper.attachToRecyclerView(recycler_view)
                    val size = resources.getDimensionPixelSize(R.dimen.item_feature_width)
                    val adapter = ImageExtractionAdapter(it, size)
                    recycler_view.adapter = adapter
                    itemClickHelper.setOnItemClickListener { recyclerView, view, position, id ->
                        val pngPath = adapter.getItem(position)
                        sendImageIntent(pngPath)
                    }
                }, {

                })
        )
    }

    private fun sendImageIntent(path: String) {
        val shareIntent = ShareCompat.IntentBuilder.from(this)
                .setType("image/png")
                .setStream(Utils.getUriForFile(this, File(path)))
                .intent
        if (shareIntent.resolveActivity(packageManager) != null) {
            startActivity(shareIntent)
        }
    }

    private fun convertImage() : ArrayList<String> {
        val results = ArrayList<String>()
        val pdfViewCtrl = getPDFViewCtrl()
        val pdfDoc = getPDFDoc()
        val draw = PDFDraw()
        var shouldUnlockRead = false
        try {
            draw.setDPI(92.0)
            pdfViewCtrl!!.docLockRead()
            shouldUnlockRead = true
            // convert pages
            val itr = pdfDoc!!.pageIterator
            while (itr.hasNext()) {
                val pg = itr.next()
                val pngFile = File.createTempFile("tmp", ".png", this.filesDir)
                draw.export(pg, pngFile.absolutePath)
                results.add(pngFile.absolutePath)
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            if (shouldUnlockRead) {
                pdfViewCtrl?.docUnlockRead()
            }
        }
        return results
    }

    private fun convert(): Single<ArrayList<String>> {
        return Single.fromCallable {
            return@fromCallable convertImage()
        }
    }
}