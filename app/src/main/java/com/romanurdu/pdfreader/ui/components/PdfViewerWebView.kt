package com.romanurdu.pdfreader.ui.components

import android.annotation.SuppressLint
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.romanurdu.pdfreader.ui.theme.ReadingTheme
import com.romanurdu.pdfreader.ui.viewmodel.TapCoordinates
import com.romanurdu.pdfreader.utils.PdfBridgeInterface
import java.io.File
import java.net.URLEncoder

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun PdfViewerWebView(
    pdfFilePath: String,
    theme: ReadingTheme,
    zoomScale: Float,
    targetPage: Int,
    onWordTapped: (word: String, sentence: String, coords: TapCoordinates) -> Unit,
    onPageChanged: (page: Int, total: Int, scrollPercent: Float) -> Unit,
    onScannedPdfDetected: (pageNum: Int) -> Unit,
    onDocumentLoaded: (total: Int) -> Unit,
    onDocumentError: (error: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Update theme when changed
    LaunchedEffect(theme) {
        webViewRef?.evaluateJavascript("window.setTheme('${theme.jsValue}');", null)
    }

    // Update zoom when changed
    LaunchedEffect(zoomScale) {
        webViewRef?.evaluateJavascript("window.setZoom($zoomScale);", null)
    }

    // Jump to page if requested
    LaunchedEffect(targetPage) {
        if (targetPage > 1) {
            webViewRef?.evaluateJavascript("window.jumpToPage($targetPage);", null)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    builtInZoomControls = true
                    displayZoomControls = false
                    setSupportZoom(true)
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                }

                // Attach JavaScript Bridge
                val bridge = PdfBridgeInterface(
                    onWordTappedCallback = { word, sentence, left, top, right, bottom, winW, winH, dpr, pageNum ->
                        onWordTapped(
                            word,
                            sentence,
                            TapCoordinates(left, top, right, bottom, winW, winH, dpr, pageNum)
                        )
                    },
                    onPageChangedCallback = { current, total, scroll ->
                        onPageChanged(current, total, scroll)
                    },
                    onScannedPdfDetectedCallback = { pageNum ->
                        onScannedPdfDetected(pageNum)
                    },
                    onDocumentLoadedCallback = { total ->
                        onDocumentLoaded(total)
                    },
                    onDocumentErrorCallback = { error ->
                        onDocumentError(error)
                    }
                )
                addJavascriptInterface(bridge, "AndroidBridge")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        // Trigger initial theme & zoom settings
                        view?.evaluateJavascript("window.setTheme('${theme.jsValue}');", null)
                        view?.evaluateJavascript("window.setZoom($zoomScale);", null)
                        if (targetPage > 1) {
                            view?.evaluateJavascript("window.jumpToPage($targetPage);", null)
                        }
                    }
                }

                webChromeClient = WebChromeClient()

                // Encode local file URI for PDF.js viewer
                val encodedFile = URLEncoder.encode(Uri.fromFile(File(pdfFilePath)).toString(), "UTF-8")
                loadUrl("file:///android_asset/pdfjs/viewer.html?file=$encodedFile")

                webViewRef = this
            }
        },
        update = { webView ->
            webViewRef = webView
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.destroy()
        }
    }
}
