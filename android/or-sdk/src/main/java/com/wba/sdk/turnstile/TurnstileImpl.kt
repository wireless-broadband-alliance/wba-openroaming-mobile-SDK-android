package com.wba.sdk.turnstile

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
import com.wba.sdk.R
import com.wba.sdk.utils.Preferences
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
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Default implementation of [Turnstile].
 *
 * Manages Cloudflare Turnstile challenge resolution by dynamically mounting a [WebView]
 * in the host [AppCompatActivity], loading the Turnstile web endpoint, and exposing a
 * [JSInterface] bridge to receive the generated verification token.
 *
 * @property preferences Utility used to read local configuration data such as `TURNSTILE_SITE_KEY`.
 */
class TurnstileImpl(
    private val preferences: Preferences
) : Turnstile {

    /**
     * Retrieves the stored Turnstile site key from local preferences.
     */
    override val siteKey: String?
        get() = preferences.getString("TURNSTILE_SITE_KEY")

    /**
     * Obtains a Cloudflare Turnstile verification token asynchronously.
     *
     * Wraps [fetchToken] inside [Result.runCatching] to gracefully capture exceptions.
     *
     * @param activity The host [AppCompatActivity] for rendering the web interface.
     * @return A [Result] containing the verification token on success or an error on failure.
     */
    override suspend fun getToken(activity: AppCompatActivity): Result<String> = runCatching {
        fetchToken(activity)
    }

    /**
     * Internal suspend method running on [Dispatchers.Main] that constructs the [WebView], configures
     * the JavaScript interface, attaches the view to the Activity layout, and waits for token resolution.
     *
     * Properly handles coroutine cancellation by detaching and destroying the [WebView].
     *
     * @param activity The host [AppCompatActivity].
     * @return The verification token string received from JavaScript.
     */
    private suspend fun fetchToken(activity: AppCompatActivity): String = withContext(Dispatchers.Main) {

        suspendCancellableCoroutine { continuation ->

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
                    val exception = Exception(error?.description?.toString())
                    continuation.resumeWithException(exception)

                    cleanup(activity, webView)
                }
            }

            val jsInterface = object : JSInterface() {

                @JavascriptInterface
                override fun getSiteKey() = siteKey

                @JavascriptInterface
                override fun onTokenReceived(token: String) {

                    if (continuation.isActive) {
                        continuation.resume(value = token)
                        cleanup(activity, webView)
                    }
                }
            }

            webView.addJavascriptInterface(jsInterface, "JSInterface")

            val url = String.format(activity.getString(R.string.open_roaming_api), "turnstile/android")
            webView.loadUrl(url)

            val container = activity.findViewById<ViewGroup>(android.R.id.content)
            container.addView(webView)

            continuation.invokeOnCancellation {
                cleanup(activity, webView)
            }
        }
    }

    /**
     * Safely removes the [webView] from the Activity's layout container and releases memory resources
     * on the main UI thread.
     *
     * @param activity The host [AppCompatActivity].
     * @param webView The [WebView] instance to detach and destroy.
     */
    private fun cleanup(activity: AppCompatActivity, webView: WebView) = activity.runOnUiThread {
        val container = activity.findViewById<ViewGroup>(android.R.id.content)
        container.removeView(webView)

        webView.visibility = View.GONE
        webView.destroy()
    }

    /**
     * JavaScript interface exposed to the web view to facilitate bi-directional communication
     * between web JavaScript and Kotlin.
     */
    open class JSInterface {

        /**
         * Provides the Turnstile site key to the web application.
         *
         * @return The site key string or a fallback placeholder.
         */
        @JavascriptInterface
        open fun getSiteKey(): String? {
            Log.i("Token", "A site key was settled")
            return "1x00000000000000000000AA"
        }

        /**
         * Callback invoked by JavaScript when Cloudflare Turnstile successfully generates a verification token.
         *
         * @param token The Turnstile verification token string.
         */
        @JavascriptInterface
        open fun onTokenReceived(token: String) {
            Log.i("Token", "A token was received")
        }
    }
}