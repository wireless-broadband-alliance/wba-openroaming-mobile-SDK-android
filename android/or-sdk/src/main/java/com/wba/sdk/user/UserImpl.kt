package com.wba.sdk.user

import com.wba.sdk.core.API
import com.wba.sdk.utils.MissingTwoFAException
import com.wba.sdk.utils.Preferences
import com.wba.sdk.utils.isMissing2FA
import org.json.JSONObject

//
//  UserImpl.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Default implementation of [User].
 *
 * Handles account authentication (local and SAML), user registration, 2FA workflows,
 * profile fetching, and session persistence through [API] requests and [Preferences] storage.
 *
 * @property preferences Utility used to read and write user session data and JWT tokens locally.
 * @property api Network client interface for executing user management endpoints.
 */
class UserImpl(
    private val preferences: Preferences,
    private val api: API
) : User {

    /**
     * Checks if the user is logged in by querying key `USER_LOGGED_IN` in local preferences.
     */
    override val isLoggedIn: Boolean
        get() = preferences.getBoolean("USER_LOGGED_IN")

    /**
     * Retrieves the stored JWT token from local preferences under key `USER_JWT_TOKEN`.
     */
    override val jwtToken: String?
        get() = preferences.getString("USER_JWT_TOKEN")

    /**
     * Fetches account information from the API using the stored [jwtToken].
     *
     * @return [Result] containing user data JSON on success or an error if unauthenticated/failed.
     */
    override suspend fun info(): Result<JSONObject> = runCatching {
        val token = jwtToken ?: throw Exception("Unknown JWT Token Error")

        val response = api.info(token)
        val success = response.getBoolean("success")

        if (success) response.getJSONObject("data")
        else throw Exception(response.optString("error", "Unknown SDK Register Error"))
    }

    /**
     * Requests a 2FA verification code or TOTP setup token via API.
     *
     * @throws IllegalArgumentException if `uuid` or `password` are blank.
     */
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

    /**
     * Submits a 2FA code to validate user identity.
     *
     * @throws IllegalArgumentException if `code` is blank.
     */
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

    /**
     * Requests a password reset link/email for the given email address.
     *
     * @throws IllegalArgumentException if `uuid` is blank.
     */
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

    /**
     * Submits registration parameters for new user account creation.
     *
     * @throws IllegalArgumentException if required fields are missing or terms are not accepted.
     */
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

    /**
     * Performs traditional username/password authentication against the backend API.
     *
     * On success, persists session tokens locally via [save].
     *
     * @throws IllegalArgumentException if `uuid` or `password` are blank.
     * @throws MissingTwoFAException if the account requires a 2FA verification code.
     */
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

    /**
     * Performs SAML authentication against the backend API using the SAML response token.
     *
     * On success, persists session tokens locally via [save].
     */
    override suspend fun login(response: String): Result<Unit> = runCatching {
        val params = JSONObject().apply {
            put("response", response)
        }

        val response = api.login(params, "saml")
        val success = response.getBoolean("success")

        if (success) save(response)
        else getException(response)
    }

    /**
     * Saves user login state and the returned JWT token to local [Preferences].
     *
     * @param data JSON object returned by the API containing session tokens.
     */
    private fun save(data: JSONObject) {
        preferences.saveBoolean("USER_LOGGED_IN", true)

        val token = data.getJSONObject("data").getString("token")
        preferences.saveString("USER_JWT_TOKEN", token)
    }

    /**
     * Evaluates API failure responses and throws either a specialized [MissingTwoFAException]
     * or a generic [Exception].
     *
     * @param response JSON response containing error metadata.
     * @return Nothing, always throws an exception.
     */
    private fun getException(response: JSONObject): Nothing {
        val message = response.optString("error", "Unknown SDK Register Error")
        if (response.isMissing2FA()) throw MissingTwoFAException(message)
        else throw Exception(message)
    }

    /**
     * Clears session values (`USER_LOGGED_IN` and `USER_JWT_TOKEN`) from local storage.
     */
    override fun logout() {
        preferences.saveBoolean("USER_LOGGED_IN", false)
        preferences.saveString("USER_JWT_TOKEN", null)
    }
}