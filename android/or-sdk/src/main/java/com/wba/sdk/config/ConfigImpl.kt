package com.wba.sdk.config

import com.wba.sdk.core.API
import com.wba.sdk.utils.Preferences
import org.json.JSONObject

//
//  ConfigImpl.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Default implementation of [Config].
 *
 * Handles fetching remote configuration via [API] and persisting settings locally using [Preferences].
 *
 * @property preferences Utility for reading and writing key-value data to local storage.
 * @property api Client used for HTTP communication with backend services.
 */
class ConfigImpl(
    private val preferences: Preferences,
    private val api: API
) : Config {

    /**
     * Checks if the local configuration is expired by comparing the current timestamp
     * against the stored `CONFIG_EXPIRATION` value.
     */
    override val isExpired: Boolean
        get() {
            val expiration = preferences.getLong("CONFIG_EXPIRATION")
            return expiration == 0L || System.currentTimeMillis() > expiration
        }

    /**
     * Returns the Terms of Service content or URL stored in local preferences under key `TOS`.
     */
    override val termsOfService: String?
        get() = preferences.getString("TOS")

    /**
     * Returns the Privacy Policy content or URL stored in local preferences under key `PRIVACY_POLICY`.
     */
    override val privacyPolicy: String?
        get() = preferences.getString("PRIVACY_POLICY")

    /**
     * Queries local storage to verify whether a specific authentication feature is enabled.
     *
     * @param type The key corresponding to the authentication feature in preferences.
     * @return `true` if the feature is enabled; `false` otherwise.
     */
    override fun isAuthActive(type: String): Boolean = preferences.getBoolean(type)

    /**
     * Performs a network request to retrieve the latest SDK configuration.
     * On success, delegates JSON parsing and local saving to [save].
     *
     * @param expirationDays Validity duration for the new configuration in days.
     * @return A [Result] indicating success or containing the server error.
     */
    override suspend fun fetchConfig(expirationDays: Int): Result<Unit> = runCatching {
        val response = api.config()
        val success = response.getBoolean("success")

        if (success) save(expirationDays, response.getJSONObject("data"))
        else throw Exception(response.optString("error", "Unknown SDK Config Error"))
    }

    /**
     * Parses the [JSONObject] returned by the API and saves the `platform`, `turnstile`,
     * `auth`, and `saml` blocks into local [Preferences], calculating and storing the new expiration timestamp.
     *
     * @param expirationDays Number of days until the config expires.
     * @param data JSON object containing platform configurations.
     */
    private fun save(expirationDays: Int, data: JSONObject) {
        data.getJSONObject("platform").apply {
            preferences.saveString("TOS", getString("TOS"))
            preferences.saveString("PRIVACY_POLICY", getString("PRIVACY_POLICY"))
        }

        data.getJSONObject("turnstile").apply {
            preferences.saveString("TURNSTILE_SITE_KEY", getString("TURNSTILE_KEY"))
        }

        data.getJSONObject("auth").apply {
            preferences.saveBoolean("AUTH_LOCAL", getBoolean("AUTH_METHOD_LOGIN_TRADITIONAL_ENABLED"))
            preferences.saveBoolean("AUTH_SAML", getBoolean("AUTH_METHOD_SAML_ENABLED"))
        }

        if (data.has("saml")) data.getJSONObject("saml").apply {
            preferences.saveString("SAML_START", "https://wifi.wballiance.pt/saml/login") //TODO need to change this
        }

        val expirationTime = System.currentTimeMillis() + (expirationDays * 24L * 60 * 60 * 1000)
        preferences.saveLong("CONFIG_EXPIRATION", expirationTime)
    }
}