package com.cufa.workshop

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

/**
 * CUFA Workshop PRO
 * Hybrid shell: the same Workshop UI/database is used on web/PWA/Android.
 * Native Bluetooth/OBDLink MX+ lives in the Android OBD layer.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var web: WebView
    private val permission = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestBluetooth()
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            webChromeClient = WebChromeClient()
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    if (url == null) return false
                    if (url.startsWith("tel:") || url.startsWith("mailto:") || url.contains("maps.google")) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))); return true
                    }
                    return false
                }
            }
            loadUrl("https://auto-servis-cufa-dubrava.netlify.app/admin.html")
        }
        setContentView(web)
    }

    private fun requestBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permission.launch(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT))
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
