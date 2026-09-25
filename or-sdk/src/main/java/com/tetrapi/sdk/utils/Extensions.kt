package com.tetrapi.sdk.utils

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
//  Copyright © 2026 Tetrapi. All rights reserved.
//

fun Context.isAppDebuggable() = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0

fun Snackbar.allowInfiniteLines(): Snackbar {
    val textView = view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
    textView.maxLines = Int.MAX_VALUE
    textView.isSingleLine = false

    return this
}

fun JSONObject.isMissing2FA() = getString("error") == "Invalid data: Missing required fields."
        && getJSONArray("missing_fields").length() == 1
        && getJSONArray("missing_fields").get(0) == "twoFACode"

fun String.decodeJwt(): JSONObject {
    val payloadB64Url = split(".")[1]
    val payloadJson = String(
        Base64.decode(payloadB64Url, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
        Charsets.UTF_8
    )

    return JSONObject(payloadJson)
}

fun String.isJwtExpired(): Boolean {
    val info = decodeJwt()
    return info.isJwtExpired()
}

fun JSONObject.isJwtExpired(): Boolean {
    val exp = optLong("exp")

    val now = System.currentTimeMillis() / 1000
    val earlyRefreshWindow = 5 * 60

    return exp <= now + earlyRefreshWindow
}