
package com.elimuconnect.offline

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.webkit.WebViewAssetLoader

/**
 * Elimu Connect Offline - first Android shell.
 * All bundled web content is served from the APK using WebViewAssetLoader.
 */
class MainActivity : ComponentActivity() {

    private lateinit var webView: WebView
    private lateinit var progress: ProgressBar
    private lateinit var assetLoader: WebViewAssetLoader

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(18, 59, 104)
        window.navigationBarColor = Color.rgb(18, 59, 104)

        assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                WebViewAssetLoader.AssetsPathHandler(this)
            )
            .build()

        val root = FrameLayout(this).apply {
            setBackgroundColor(Color.WHITE)
        }

        // Initialize progress before WebView callbacks can use it.
        progress = ProgressBar(
            this,
            null,
            android.R.attr.progressBarStyleHorizontal
        ).apply {
            isIndeterminate = true
            visibility = View.VISIBLE
        }

        webView = WebView(this).apply {
            setBackgroundColor(Color.WHITE)

            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            settings.loadsImagesAutomatically = true
            settings.mixedContentMode =
                android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW

            webChromeClient = WebChromeClient()

            webViewClient = object : WebViewClient() {

                override fun shouldInterceptRequest(
                    view: WebView,
                    request: WebResourceRequest
                ) = assetLoader.shouldInterceptRequest(request.url)

                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest
                ): Boolean {
                    val uri = request.url

                    if (uri.host == "appassets.androidplatform.net") {
                        return false
                    }

                    // External websites and services remain online-only.
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                        true
                    } catch (_: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            "This resource needs internet access.",
                            Toast.LENGTH_SHORT
                        ).show()
                        true
                    }
                }

                override fun onPageFinished(
                    view: WebView,
                    url: String?
                ) {
                    progress.setVisibility(View.GONE)
                }
            }
        }

        root.addView(
            webView,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )

        root.addView(
            progress,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                8
            )
        )

        setContentView(root)

        webView.loadUrl(
            "https://appassets.androidplatform.net/assets/site/appaccessdaily.html"
        )

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        finish()
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        if (::webView.isInitialized) {
            webView.stopLoading()
            webView.destroy()
        }
        super.onDestroy()
    }
}
