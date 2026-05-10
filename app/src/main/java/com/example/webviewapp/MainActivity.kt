package com.example.webviewapp

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var btnBack: ImageButton
    private lateinit var btnForward: ImageButton
    private lateinit var btnRefresh: ImageButton
    private lateinit var tvUrl: TextView
    private lateinit var offlineLayout: FrameLayout

    // Yahan apni website ka URL daalo
    private val HOME_URL = "https://www.google.com"

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Views initialize karo
        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        btnBack = findViewById(R.id.btnBack)
        btnForward = findViewById(R.id.btnForward)
        btnRefresh = findViewById(R.id.btnRefresh)
        tvUrl = findViewById(R.id.tvUrl)
        offlineLayout = findViewById(R.id.offlineLayout)

        setupWebView()
        setupButtons()
        setupSwipeRefresh()

        // Home URL load karo
        webView.loadUrl(HOME_URL)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true          // JavaScript on
            domStorageEnabled = true          // Local storage on
            loadWithOverviewMode = true       // Fit to screen
            useWideViewPort = true            // Wide viewport
            builtInZoomControls = true        // Zoom allow
            displayZoomControls = false       // Zoom buttons hide
            setSupportZoom(true)
            allowFileAccess = true
            javaScriptCanOpenWindowsAutomatically = true
            mediaPlaybackRequiresUserGesture = false  // Auto video play
        }

        // WebViewClient - links same app mein open hon
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
                offlineLayout.visibility = View.GONE
                tvUrl.text = url ?: HOME_URL
                updateNavButtons()
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
                swipeRefresh.isRefreshing = false
                updateNavButtons()
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()
                // External links browser mein open karo (optional)
                return if (url.startsWith("http://") || url.startsWith("https://")) {
                    false // App mein hi open karo
                } else {
                    // tel:, mailto: etc. system app mein open karo
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    true
                }
            }

            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                super.onReceivedError(view, errorCode, description, failingUrl)
                offlineLayout.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            }
        }

        // WebChromeClient - progress bar ke liye
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                progressBar.progress = newProgress
            }
        }
    }

    private fun setupButtons() {
        btnBack.setOnClickListener {
            if (webView.canGoBack()) webView.goBack()
        }

        btnForward.setOnClickListener {
            if (webView.canGoForward()) webView.goForward()
        }

        btnRefresh.setOnClickListener {
            webView.reload()
        }

        // Offline screen ka retry button
        findViewById<View>(R.id.btnRetry).setOnClickListener {
            offlineLayout.visibility = View.GONE
            webView.reload()
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )
        swipeRefresh.setOnRefreshListener {
            webView.reload()
        }
    }

    private fun updateNavButtons() {
        btnBack.alpha = if (webView.canGoBack()) 1.0f else 0.4f
        btnForward.alpha = if (webView.canGoForward()) 1.0f else 0.4f
    }

    // Back button press hone par webview back jaye
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    // App pause/resume par webview handle karo
    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
