package com.wba.sdk.config

//
//  Config.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Interface responsible for managing remote and local SDK configurations.
 *
 * Provides properties to check cache validity, access Terms of Service and Privacy Policy,
 * validate active authentication methods, and synchronize settings with the backend API.
 */
interface Config {

    /**
     * Indicates whether the currently cached configuration has expired or has not been downloaded yet.
     */
    val isExpired: Boolean

    /**
     * Retrieves the Terms of Service obtained from the remote configuration,
     * or `null` if no local cache exists.
     */
    val termsOfService: String?

    /**
     * Retrieves the Privacy Policy obtained from the remote configuration,
     * or `null` if no local cache exists.
     */
    val privacyPolicy: String?

    /**
     * Fetches updated configuration settings from the server and persists them in local storage.
     *
     * @param expirationDays The duration in days for which the fetched settings remain valid (default: 2 days).
     * @return [Result.success] on successful fetch and persistence,
     * or [Result.failure] containing the error if network or parsing fails.
     */
    suspend fun fetchConfig(expirationDays: Int = 2): Result<Unit>

    /**
     * Checks if a specific authentication method is currently enabled in local settings.
     *
     * @param type Key/identifier of the authentication feature to check (e.g., `AUTH_LOCAL`, `AUTH_SAML`).
     * @return `true` if the specified authentication method is active, `false` otherwise.
     */
    fun isAuthActive(type: String): Boolean
}