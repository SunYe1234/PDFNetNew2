package com.pdftron.showcase.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.pdftron.common.PDFNetException
import com.pdftron.pdf.PDFDoc
import com.pdftron.pdf.PDFViewCtrl
import com.pdftron.pdf.config.PDFViewCtrlConfig
import com.pdftron.pdf.controls.ThumbnailSlider
import com.pdftron.showcase.R
import kotlinx.android.synthetic.main.content_split_view.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL


class StreamingActivity : FeatureWithComparisonActivity() {

    private val TAG = "StreamingActivity"
    private lateinit var progressBarL: ProgressBar
    private lateinit var progressBarR: ProgressBar
    private lateinit var progressBarRCircle: ProgressBar
    lateinit var byteLeft: TextView
    private lateinit var byteRight: TextView
    private lateinit var connectStatus: TextView
    private var downloadTask: DownloadTask? = null
    private var filePath = ""

    private val fileLink = "https://s3.amazonaws.com/pdftron/files/pdf/linearization/50mb-linearized.pdf"

    private var broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(contxt: Context?, intent: Intent?) {
            val notConnected = intent?.getBooleanExtra(ConnectivityManager
                    .EXTRA_NO_CONNECTIVITY, false)
            if (notConnected!!) {
                disconnect()
            } else {
                connect()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(broadcastReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    }

    private var loadOnce = false
    private lateinit var thumbNailSliderL: ThumbnailSlider
    private lateinit var thumbNailSliderR: ThumbnailSlider

    private fun pauseSamples() {
        downloadTask?.cancel(true)
        progressBarR.visibility = View.GONE
    }

    private fun connect() {
        connectStatus = findViewById(R.id.connect_status)
        connectStatus.visibility = View.GONE
        resetSamples()
    }

    private fun disconnect() {
        connectStatus.visibility = View.VISIBLE
        connectStatus.bringToFront()
        pauseSamples()
    }

    private fun deleteCacheFile() {
        val file = File(this.filePath)
        if (file.exists()) {
            file.delete()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        deleteCacheFile()
        pauseSamples()
    }

    override fun onPause() {
        super.onPause()
        deleteCacheFile()
        pauseSamples()
    }

    override fun setUpViews() {
        super.setUpViews()
        byteBarLeft.visibility = View.VISIBLE
        byteBarRight.visibility = View.VISIBLE
        progressBarL = findViewById(R.id.progressBarL)
        progressBarL.visibility = View.GONE
        progressBarR = findViewById(R.id.progressBarR)
        progressBarRCircle = findViewById(R.id.progressBarRCircle)
        thumbNailSliderL = findViewById(R.id.thumbnailSliderL)
        thumbNailSliderL.visibility = View.VISIBLE
        thumbNailSliderR = findViewById(R.id.thumbnailSliderR)
        byteLeft = findViewById(R.id.stream_byte_transferred)
        byteRight = findViewById(R.id.no_stream_byte_transferred)
        connectStatus = findViewById(R.id.connect_status)

        titleL.text = getString(R.string.streaming)
        titleR.text = getString(R.string.no_streaming)

        filePath = File(this.filesDir, "streaming_new.pdf").absolutePath
        viewFromHttpUrl(fileLink)
        downloadFile()
    }

    override fun setUpPdfCtrlViews() {
        super.setUpPdfCtrlViews()
        leftPdfViewCtrl!!.setCaching(false)
        rightPdfViewCtrl!!.setCaching(false)
        thumbNailSliderL.setPdfViewCtrl(leftPdfViewCtrl)
    }

    override fun resetSamples() {
        byteLeft.text = (0).toString()
        byteRight.text = (0).toString()
        leftPdfViewCtrl!!.closeDoc()
        rightPdfViewCtrl!!.closeDoc()
        leftPdfViewCtrl!!.invalidate()
        rightPdfViewCtrl!!.invalidate()
        leftPdfViewCtrl!!.setCaching(false)
        rightPdfViewCtrl!!.setCaching(false)
        leftPdfViewCtrl!!.clearThumbCache()
        rightPdfViewCtrl!!.clearThumbCache()
        thumbnailSliderR.visibility = View.GONE
        viewFromHttpUrl(fileLink)
        downloadFile()

    }

    override fun onResume() {
        super.onResume()
        if (!loadOnce) {
            loadOnce = true
        } else {
            resetSamples()
        }

    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(broadcastReceiver)
    }

    private fun downloadFile() {
        downloadTask?.cancel(true)
        downloadTask = DownloadTask(this)
        downloadTask!!.execute(fileLink)
    }

    @Throws(PDFNetException::class)
    fun viewFromHttpUrl(myHttpUrl: String) {
        val options = PDFViewCtrl.HTTPRequestOptions()
        options.restrictDownloadUsage(true)
        leftPdfViewCtrl?.setRenderedContentCacheSize(1)
        leftPdfViewCtrl?.openUrlAsync(myHttpUrl, null, null, options)
        leftPdfViewCtrl?.addDocumentDownloadListener { state, page_num, page_downloaded, page_count, message ->
            if (leftPdfViewCtrl?.doc?.hasDownloader()!!) {
                val byte = leftPdfViewCtrl?.doc?.downloadedByteCount
                val totalByte = leftPdfViewCtrl?.doc?.totalRemoteByteCount
                if (byte == null && byteLeft.text == "0") {
                    byteLeft.text = (0).toString()
                    progressBarL.progress = 0
                } else {
                    byteLeft.text = (byte?.div(1024 * 1024)).toString()
                    progressBarL.progress = (byte!! * 100 / totalByte!! as Long).toInt()
                }
            }
            if (state == PDFViewCtrl.DownloadState.FINISHED) {
                leftPdfViewCtrl?.setRenderedContentCacheSize(PDFViewCtrlConfig.getDefaultConfig(this@StreamingActivity).renderedContentCacheSize)
            }
        }
    }


    private class DownloadTask constructor(val context: StreamingActivity) : AsyncTask<String, Int, String>() {

        private val TAG = "DownloadTask"
        var input: InputStream? = null
        var output: OutputStream? = null
        var connection: HttpURLConnection? = null

        override fun onPreExecute() {
            super.onPreExecute()
            context.deleteCacheFile()
            context.progressBarR.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): String {
            try {
                val url = URL(params[0])
                connection = url.openConnection() as HttpURLConnection
                connection!!.connect()

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection!!.getResponseCode() !== HttpURLConnection.HTTP_OK) {
                    return ("Server returned HTTP " + connection!!.getResponseCode()
                            + " " + connection!!.getResponseMessage())
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                val fileLength = connection!!.getContentLength()

                // download the file
                input = connection!!.getInputStream()
                output = FileOutputStream(context.filePath)

                val data = ByteArray(2048)
                var total: Long = 0
                var count: Int = input!!.read(data)
                while (count != -1) {
                    // allow canceling with back button
                    if (isCancelled) {
                        input!!.close()
                        return String()
                    }
                    total += count.toLong()
                    // publishing the progress....
                    if (fileLength > 0) {
                        // only if total length is known
                        val percentage = (total * 100 / fileLength).toInt()
                        publishProgress(percentage, (total / (1024 * 1024)).toInt())
                    }
                    output!!.write(data, 0, count)
                    count = input!!.read(data)
                }
            } catch (e: Exception) {
                return e.toString()
            } finally {
                try {
                    if (output != null)
                        output!!.close()
                    if (input != null)
                        input!!.close()
                } catch (ignored: IOException) {
                    Log.e(TAG, "failed in get pdf from url: " + ignored.localizedMessage)
                }
            }
            return String()
        }

        override fun onProgressUpdate(vararg values: Int?) {
            super.onProgressUpdate(*values)
            // if we get here, length is known, now set indeterminate to false
            context.progressBarR.isIndeterminate = false
            context.progressBarR.max = 100
            context.progressBarR.progress = values[0]!!
            context.byteRight.text = values[1]!!.toString()
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            context.progressBarR.visibility = View.GONE
            context.progressBarRCircle.visibility = View.GONE
            if (result != null && result != "") {
                Log.e(TAG, "onPostExecute. downlaod failed. reason: " + result)
//                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
                context.rightPdfDoc = PDFDoc(context.filePath)
                context.rightPdfViewCtrl?.setDoc(context.rightPdfDoc)
                context.thumbNailSliderR.visibility = View.VISIBLE
                context.thumbNailSliderR.setPdfViewCtrl(context.rightPdfViewCtrl)
            }

        }

    }

}