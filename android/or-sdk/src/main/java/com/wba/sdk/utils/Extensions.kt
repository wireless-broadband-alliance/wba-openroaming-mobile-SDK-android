package com.wba.sdk.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.util.Base64
import android.widget.TextView
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject

//
//  Extensions.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Checks whether the host application is running in debug mode.
 *
 * @return `true` if the application build has [ApplicationInfo.FLAG_DEBUGGABLE] set, `false` otherwise.
 */
fun Context.isAppDebuggable() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

/**
 * Configures a [Snackbar] to display multi-line text without truncating.
 *
 * @return The updated [Snackbar] instance for method chaining.
 */
fun Snackbar.allowInfiniteLines(): Snackbar {
    val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.maxLines = Int.MAX_VALUE
    textView.isSingleLine = false

    return this
}

/**
 * Evaluates whether an API JSON error response indicates a missing 2FA code requirement.
 *
 * @return `true` if the error explicitly specifies a missing `twoFACode` field, `false` otherwise.
 */
fun JSONObject.isMissing2FA() = getString("error") == "Invalid data: Missing required fields."
        && getJSONArray("missing_fields").length() == 1
        && getJSONArray("missing_fields").get(0) == "twoFACode"

/**
 * Decodes a JWT (JSON Web Token) string and extracts its payload block as a [JSONObject].
 *
 * Assumes standard JWT structure (`header.payload.signature`).
 *
 * @return Decoded payload as a [JSONObject].
 */
fun String.decodeJwt(): JSONObject {
    val payloadB64Url = split(".")[1]
    val payloadJson = String(
        Base64.decode(payloadB64Url, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
        Charsets.UTF_8
    )

    return JSONObject(payloadJson)
}

/**
 * Decodes and checks whether a JWT token string is expired.
 *
 * @return `true` if the token payload is expired or within the 5-minute refresh window, `false` otherwise.
 */
fun String.isJwtExpired(): Boolean {
    val info = decodeJwt()
    return info.isJwtExpired()
}

/**
 * Checks if the `exp` claim in a JWT payload JSON indicates expiration,
 * including a 5-minute early refresh buffer window.
 *
 * @return `true` if expired or close to expiration, `false` otherwise.
 */
fun JSONObject.isJwtExpired(): Boolean {
    val exp = optLong("exp")

    val now = System.currentTimeMillis() / 1000
    val earlyRefreshWindow = 5 * 60

    return exp <= now + earlyRefreshWindow
}