package com.pdftron.showcase.activities

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.Element
import com.pdftron.pdf.ElementReader
import com.pdftron.pdf.Image
import com.pdftron.pdf.widget.recyclerview.ItemClickHelper
import com.pdftron.showcase.R
import com.pdftron.showcase.adapters.ImageExtractionAdapter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.content_bottom_sheet.*
import java.io.File
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.control_recyclerview.*
import kotlin.collections.ArrayList
import android.support.v4.app.ShareCompat
import android.view.View
import com.pdftron.pdf.utils.Utils


class ImageExtractionActivity : FeatureActivity() {

    // Disposables
    private var mDisposables: CompositeDisposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        sampleFileName = "newsletter"
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
        mDisposables!!.add(extract()
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

    private fun extractImage() : ArrayList<String> {
        val results = ArrayList<String>()
        val pdfViewCtrl = getPDFViewCtrl()
        val pdfDoc = getPDFDoc()
        var shouldUnlockRead = false
        try {
            pdfViewCtrl!!.docLockRead()
            shouldUnlockRead = true
            // extract image
            val reader = ElementReader()
            val itr = pdfDoc!!.pageIterator
            while (itr.hasNext()) {
                reader.begin(itr.next())
                imageExtract(reader, results)
                reader.end()
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

    @Throws(PDFNetException::class)
    internal fun imageExtract(reader: ElementReader, results: ArrayList<String>) {
        var element: Element?
        while (true) {
            element = reader.next()
            if (element == null) {
                break
            }
            when (element.type) {
                Element.e_image, Element.e_inline_image -> {

                    val ctm = element.ctm
                    val x2 = 1.0
                    val y2 = 1.0
                    ctm.multPoint(x2, y2)

                    if (element.type == Element.e_image) {
                        val image = Image(element.xObject)

                        val pngFile = File.createTempFile("tmp", ".png", this.filesDir)
                        image.exportAsPng(pngFile.absolutePath)
                        results.add(pngFile.absolutePath)
                    }
                }
                Element.e_form -> {
                    reader.formBegin()
                    imageExtract(reader, results)
                    reader.end()
                }
            }
        }
    }

    private fun extract(): Single<ArrayList<String>> {
        return Single.fromCallable {
            return@fromCallable extractImage()
        }
    }
}