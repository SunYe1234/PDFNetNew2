package com.pdftron.showcase.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import com.pdftron.showcase.R

class WebviewActivity : AppCompatActivity() {

    private lateinit var webview: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.content_inapp_web_view
        )
        webview = findViewById<WebView>(R.id.my_web_view)

        webview.settings.javaScriptEnabled = true
        webview.settings.javaScriptCanOpenWindowsAutomatically = true
        webview.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                view?.loadUrl(request?.url.toString())
                return false
            }
        })

        if (intent.hasExtra("url")) {
            val url = intent.extras.get("url").toString()
            webview.loadUrl(url)
        }
    }

}