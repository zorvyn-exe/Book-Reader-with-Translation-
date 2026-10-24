# ProGuard rules for Roman Urdu PDF Reader

# Keep JavascriptInterface annotations for WebView PDF.js bridge
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
-keep class com.romanurdu.pdfreader.utils.PdfBridgeInterface { *; }

# Keep Room database entities and models
-keep class com.romanurdu.pdfreader.data.model.** { *; }
-keepclassmembers class com.romanurdu.pdfreader.data.model.** { *; }
