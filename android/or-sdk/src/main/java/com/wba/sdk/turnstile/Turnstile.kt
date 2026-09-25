package com.wba.sdk.turnstile

import androidx.appcompat.app.AppCompatActivity

//
//  Turnstile.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Interface defining Cloudflare Turnstile bot detection and challenge verification.
 *
 * Provides methods to retrieve the configured Turnstile site key and asynchronously
 * execute the Turnstile challenge to obtain a verification token.
 */
interface Turnstile {

    /**
     * The site key required by Cloudflare Turnstile, retrieved from local configuration settings,
     * or `null` if not yet loaded.
     */
    val siteKey: String?

    /**
     * Initiates the Turnstile challenge flow using an overlay web container.
     *
     * @param activity The host [AppCompatActivity] used to mount the web view during token generation.
     * @return A [Result] containing the generated Turnstile token string on success,
     * or an exception if web loading fails or the process is interrupted.
     */
    suspend fun getToken(activity: AppCompatActivity): Result<String>
}