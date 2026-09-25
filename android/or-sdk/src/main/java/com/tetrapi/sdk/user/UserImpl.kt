package com.tetrapi.sdk.user

import com.tetrapi.sdk.core.API
import com.tetrapi.sdk.utils.MissingTwoFAException
import com.tetrapi.sdk.utils.Preferences
import com.tetrapi.sdk.utils.isMissing2FA
import org.json.JSONObject

//
//  UserImpl.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class UserImpl(
    private val preferences: Preferences,
    private val api: API
) : User {

    override val isLoggedIn: Boolean
        get() = preferences.getBoolean("USER_LOGGED_IN")

    override val jwtToken: String?
        get() = preferences.getString("USER_JWT_TOKEN")

    override suspend fun info(): Result<JSONObject> = runCatching {
        val token = jwtToken ?: throw Exception("Unknown JWT Token Error")

        val response = api.info(token)
        val success = response.getBoolean("success")

        if (success) response.getJSONObject("data")
        else throw Exception(response.optString("error", "Unknown SDK Register Error"))
    }

    override suspend fun code(uuid: String, password: String, type: String, token: String): Result<String> = runCatching {
        if (uuid.isBlank() || password.isBlank()) {
            val message = "You need to set at least an email and password to get the code!"
            throw IllegalArgumentException(message)
        }

        val params = JSONObject().apply {
            put("uuid", uuid)
            put("password", password)
            put("turnstile_token", token)
        }

        val response = api.code(params, type)
        val success = response.getBoolean("success")

        if (success && type == "email") response.getJSONObject("data").getString("message")
        else if (success) response.getJSONObject("data").getString("totpId")
        else throw Exception(response.optString("error", "Unknown SDK Register Error"))
    }

    override suspend fun validate(uuid: String, password: String, code: String): Result<String> = runCatching {
        if (code.isBlank()) {
            val message = "You need to set the code to validate!"
            throw IllegalArgumentException(message)
        }

        val params = JSONObject().apply {
            put("uuid", uuid)
            put("password", password)
            put("code", code)
            put("type", "totp")
        }

        val response = api.validate(params)
        val success = response.getBoolean("success")

        if (success) response.getJSONObject("data").getString("message")
        else throw Exception(response.optString("error", "Unknown SDK Register Error"))
    }

    override suspend fun reset(uuid: String, token: String): Result<String> = runCatching {
        if (uuid.isBlank()) {
            val message = "You need to set your email to reset the password!"
            throw IllegalArgumentException(message)
        }

        val params = JSONObject().apply {
            put("email", uuid)
            put("turnstile_token", token)
        }

        val response = api.reset(params)
        val success = response.getBoolean("success")

        if (success) response.getString("data")
        else throw Exception(response.optString("error", "Unknown SDK Reset Error"))
    }

    override suspend fun register(uuid: String, password: String, nameFirst: String?, nameLast: String?, acceptedTOS: Boolean, token: String): Result<String> = runCatching {
        if (uuid.isBlank() || password.isBlank()) {
            val message = "You need to set at least an email and password to register!"
            throw IllegalArgumentException(message)
        }

        if (!acceptedTOS) {
            val message = "You need to accept or Terms & Conditions and Privacy Policy to register!"
            throw IllegalArgumentException(message)
        }

        val params = JSONObject().apply {
            put("email", uuid)
            put("password", password)
            if (nameFirst != null) put("first_name", nameFirst)
            if (nameLast != null) put("last_name", nameLast)
            put("turnstile_token", token)
        }

        val response = api.register(params)
        val success = response.getBoolean("success")

        if (success) response.getJSONObject("data").getString("message")
        else throw Exception(response.optString("error", "Unknown SDK Register Error"))
    }

    override suspend fun login(uuid: String, password: String, code: String?, token: String): Result<Unit> = runCatching {
        if (uuid.isBlank() || password.isBlank()) {
            val message = "You need to set your username and password to login!"
            throw IllegalArgumentException(message)
        }

        val params = JSONObject().apply {
            put("uuid", uuid)
            put("password", password)
            if (code != null) put("twoFACode", code)
            put("turnstile_token", token)
        }

        val response = api.login(params, "local")
        val success = response.getBoolean("success")

        if (success) save(response)
        else getException(response)
    }

    override suspend fun login(response: String): Result<Unit> = runCatching {
        val params = JSONObject().apply {
            put("response", response)
        }

        val response = api.login(params, "saml")
        val success = response.getBoolean("success")

        if (success) save(response)
        else getException(response)
    }

    private fun save(data: JSONObject) {
        preferences.saveBoolean("USER_LOGGED_IN", true)

        val token = data.getJSONObject("data").getString("token")
        preferences.saveString("USER_JWT_TOKEN", token)
    }

    private fun getException(response: JSONObject): String {
        val message = response.optString("error", "Unknown SDK Register Error")
        if (response.isMissing2FA()) throw MissingTwoFAException(message)
        else throw Exception(message)
    }

    override fun logout() {
        preferences.saveBoolean("USER_LOGGED_IN", false)
        preferences.saveString("USER_JWT_TOKEN", null)
    }
}