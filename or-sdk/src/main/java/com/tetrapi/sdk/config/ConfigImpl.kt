package com.tetrapi.sdk.config

import com.tetrapi.sdk.core.API
import com.tetrapi.sdk.utils.Preferences
import org.json.JSONObject

//
//  ConfigImpl.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class ConfigImpl(
    private val preferences: Preferences,
    private val api: API
) : Config {

    override val isExpired: Boolean
        get() {
            val expiration = preferences.getLong("CONFIG_EXPIRATION")
            return expiration == 0L || System.currentTimeMillis() > expiration
        }

    override val termsOfService: String?
        get() = preferences.getString("TOS")

    override val privacyPolicy: String?
        get() = preferences.getString("PRIVACY_POLICY")

    override fun isAuthActive(type: String) = preferences.getBoolean(type)

    override suspend fun fetchConfig(expirationDays: Int): Result<Unit> = runCatching {
        val response = api.config()
        val success = response.getBoolean("success")

        if (success) save(expirationDays, response.getJSONObject("data"))
        else throw Exception(response.optString("error", "Unknown SDK Config Error"))
    }

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
            preferences.saveString("SAML_START", "https://wifi.tetrapi.pt/saml/login")
        }

        val expirationTime = System.currentTimeMillis() + (expirationDays * 24L * 60 * 60 * 1000)
        preferences.saveLong("CONFIG_EXPIRATION", expirationTime)
    }
}