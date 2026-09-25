package com.tetrapi.sdk.turnstile

import android.annotation.SuppressLint
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.tetrapi.sdk.R
import com.tetrapi.sdk.utils.Preferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//
//  TurnstileImpl.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class TurnstileImpl(
    private val preferences: Preferences
) : Turnstile {

    override val siteKey: String?
        get() = preferences.getString("TURNSTILE_SITE_KEY")

    override suspend fun getToken(activity: AppCompatActivity): Result<String> = runCatching {
        fetchToken(activity)
    }

    private suspend fun fetchToken(activity: AppCompatActivity): String = withContext(Dispatchers.Main) {

        suspendCancellableCoroutine {

            val webView = WebView(activity)
            webView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            webView.settings.apply {
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
            }

            webView.webViewClient = object : WebViewClient() {

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    val error = Exception(error?.description?.toString())
                    it.resumeWithException(error)

                    cleanup(activity, webView)
                }
            }

            val jsInterface = object : JSInterface() {

                @JavascriptInterface
                override fun getSiteKey() = siteKey

                @JavascriptInterface
                override fun onTokenReceived(token: String) {

                    if (it.isActive) {
                        it.resume(token)
                        cleanup(activity, webView)
                    }
                }
            }

            webView.addJavascriptInterface(jsInterface, "JSInterface")

            val url = String.format(activity.getString(R.string.open_roaming_api), "turnstile/android")
            webView.loadUrl(url)

            val container = activity.findViewById<ViewGroup>(android.R.id.content)
            container.addView(webView)

            it.invokeOnCancellation {
                cleanup(activity, webView)
            }
        }
    }

    private fun cleanup(activity: AppCompatActivity, webView: WebView) = activity.runOnUiThread {
        val container = activity.findViewById<ViewGroup>(android.R.id.content)
        container.removeView(webView)

        webView.visibility = View.GONE
        webView.destroy()
    }

    open class JSInterface {

        @JavascriptInterface
        open fun getSiteKey(): String? {
            Log.i("Token", "A site key was settled")
            return "1x00000000000000000000AA"
        }

        @JavascriptInterface
        open fun onTokenReceived(token: String) {
            Log.i("Token", "A token was received")
        }
    }
}