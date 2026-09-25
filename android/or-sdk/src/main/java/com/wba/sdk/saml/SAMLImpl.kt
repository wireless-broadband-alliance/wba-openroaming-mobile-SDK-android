package com.wba.sdk.saml

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
import com.wba.sdk.utils.Preferences
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

//
//  SAMLImpl.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Default implementation of [SAML].
 *
 * Manages SAML Single Sign-On (SSO) authentication by dynamically injecting a [WebView]
 * into the current Activity's root layout, evaluating JavaScript to capture SAML assertions,
 * and passing the captured token back to Kotlin coroutines.
 *
 * @property preferences Local storage utility used to retrieve the SAML entry point URL.
 */
class SAMLImpl(
    private val preferences: Preferences
) : SAML {

    /**
     * Retrieves the SAML start URL stored in local preferences under key `SAML_START`.
     */
    override val url: String?
        get() = preferences.getString("SAML_START")

    /**
     * Dynamically attaches a [WebView] overlay to the given [activity], loads the SAML entry point,
     * and listens for the posted `SAMLResponse` via JavaScript injection.
     *
     * Automatically detaches and cleans up the [WebView] once authentication completes or fails.
     *
     * @param activity The host [AppCompatActivity] to display the authentication web interface.
     * @return A [Result] containing the captured SAML response string.
     */
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
                    it.resume(value = response)
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

    /**
     * Safely removes the [webView] from the Activity root container and releases its resources
     * on the main UI thread.
     *
     * @param activity The host [AppCompatActivity] containing the view.
     * @param webView The [WebView] instance to destroy.
     */
    private fun cleanup(activity: AppCompatActivity, webView: WebView) = activity.runOnUiThread {
        val container = activity.findViewById<ViewGroup>(android.R.id.content)
        container.removeView(webView)

        webView.visibility = View.GONE
        webView.destroy()
    }

    /**
     * JavaScript snippet injected into loaded pages to intercept `SAMLResponse` hidden form fields
     * upon direct detection or form submission.
     */
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

    /**
     * JavaScript bridge class exposed to the web view to receive callbacks from injected scripts.
     */
    open class JSInterface {

        @JavascriptInterface
        open fun onSAMLResponse(response: String) {
            Log.i("SAML", "A response was returned")
        }
    }
}