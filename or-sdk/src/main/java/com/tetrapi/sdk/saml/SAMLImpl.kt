package com.tetrapi.sdk.saml

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
import com.tetrapi.sdk.utils.Preferences
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//
//  SAMLImpl.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class SAMLImpl(
    private val preferences: Preferences
) : SAML {

    override val url: String?
        get() = preferences.getString("SAML_START")

    override suspend fun start(activity: AppCompatActivity): Result<String> = runCatching {

        suspendCancellableCoroutine {

            val webView = WebView(activity)
            webView.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

            webView.settings.apply {
                @SuppressLint("SetJavaScriptEnabled")
                javaScriptEnabled = true
                domStorageEnabled = true
            }

            webView.webViewClient = object : WebViewClient() {

                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                    val error = Exception(error?.description?.toString())
                    it.resumeWithException(error)

                    cleanup(activity, webView)
                }

                override fun onPageFinished(view: WebView, url: String?) {
                    super.onPageFinished(view, url)
                    view.evaluateJavascript(script, null)
                }
            }

            val jsInterface = object : JSInterface() {

                @JavascriptInterface
                override fun onSAMLResponse(response: String) {
                    it.resume(response)
                    cleanup(activity, webView)
                }
            }

            webView.addJavascriptInterface(jsInterface, "JSInterface")
            webView.loadUrl(url ?: return@suspendCancellableCoroutine run {
                val error = Exception("There is no SAML URL set!")
                it.resumeWithException(error)

                cleanup(activity, webView)
            })

            val container = activity.findViewById<ViewGroup>(android.R.id.content)
            container.addView(webView)
        }
    }

    private fun cleanup(activity: AppCompatActivity, webView: WebView) = activity.runOnUiThread {
        val container = activity.findViewById<ViewGroup>(android.R.id.content)
        container.removeView(webView)

        webView.visibility = View.GONE
        webView.destroy()
    }

    private val script = """
        (function() {
            function capture() {
                var samlInput = document.querySelector('input[name="SAMLResponse"]');
                if (samlInput && samlInput.value) {
                    JSInterface.onSAMLResponse(samlInput.value);
                }
            }
            capture(); 
            
            var originalSubmit = HTMLFormElement.prototype.submit;
            HTMLFormElement.prototype.submit = function() {
                capture();
                originalSubmit.call(this);
            };
        })();
    """.trimIndent()

    open class JSInterface {

        @JavascriptInterface
        open fun onSAMLResponse(response: String) {
            Log.i("SAML", "A response was returned")
        }
    }
}