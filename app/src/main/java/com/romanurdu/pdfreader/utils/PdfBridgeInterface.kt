package com.romanurdu.pdfreader.utils

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/**
 * JavaScript Interface bridge connected to WebView PDF.js viewer
 */
class PdfBridgeInterface(
    private val onWordTappedCallback: (word: String, sentence: String, left: Float, top: Float, right: Float, bottom: Float, windowWidth: Float, windowHeight: Float, dpr: Float, pageNum: Int) -> Unit,
    private val onPageChangedCallback: (currentPage: Int, totalPages: Int, scrollPercent: Float) -> Unit,
    private val onScannedPdfDetectedCallback: (pageNum: Int) -> Unit,
    private val onDocumentLoadedCallback: (totalPages: Int) -> Unit,
    private val onDocumentErrorCallback: (error: String) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())

    @JavascriptInterface
    fun onWordTapped(
        word: String,
        sentence: String,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        windowWidth: Float,
        windowHeight: Float,
        dpr: Float,
        pageNum: Int
    ) {
        mainHandler.post {
            onWordTappedCallback(word, sentence, left, top, right, bottom, windowWidth, windowHeight, dpr, pageNum)
        }
    }

    @JavascriptInterface
    fun onPageChanged(currentPage: Int, totalPages: Int, scrollPercent: Float) {
        mainHandler.post {
            onPageChangedCallback(currentPage, totalPages, scrollPercent)
        }
    }

    @JavascriptInterface
    fun onScannedPdfDetected(pageNum: Int) {
        mainHandler.post {
            onScannedPdfDetectedCallback(pageNum)
        }
    }

    @JavascriptInterface
    fun onDocumentLoaded(totalPages: Int) {
        mainHandler.post {
            onDocumentLoadedCallback(totalPages)
        }
    }

    @JavascriptInterface
    fun onDocumentError(error: String) {
        mainHandler.post {
            onDocumentErrorCallback(error)
        }
    }
}
