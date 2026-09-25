package com.wba.sdk.user

import org.json.JSONObject

//
//  User.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Interface defining user account management, authentication, and session handling.
 *
 * Provides capabilities for local and SAML authentication, account registration,
 * password reset, two-factor authentication (2FA/TOTP) validation, profile retrieval,
 * and session state control.
 */
interface User {

    /**
     * Indicates whether a user is currently authenticated and has an active session.
     */
    val isLoggedIn: Boolean

    /**
     * Retrieves the stored JWT authentication token for the current session,
     * or `null` if the user is not logged in.
     */
    val jwtToken: String?

    /**
     * Fetches profile details and metadata for the currently authenticated user.
     *
     * @return A [Result] containing a [JSONObject] with user profile data on success,
     * or an exception if the request fails or no JWT token is available.
     */
    suspend fun info(): Result<JSONObject>

    /**
     * Requests a verification code or TOTP setup identifier for two-factor authentication.
     *
     * @param uuid User identifier (typically email address).
     * @param password User account password.
     * @param type The code type requested (e.g., `"email"` or `"totp"`).
     * @param token Cloudflare Turnstile verification token.
     * @return A [Result] containing a confirmation message or TOTP ID on success.
     */
    suspend fun code(uuid: String, password: String, type: String, token: String): Result<String>

    /**
     * Validates a two-factor authentication code submitted by the user.
     *
     * @param uuid User identifier (email address).
     * @param password User account password.
     * @param code The 2FA/TOTP verification code to validate.
     * @return A [Result] containing the server confirmation message on success.
     */
    suspend fun validate(uuid: String, password: String, code: String): Result<String>

    /**
     * Initiates a password reset request for the given user email.
     *
     * @param uuid User email address.
     * @param token Cloudflare Turnstile verification token.
     * @return A [Result] containing the server response message on success.
     */
    suspend fun reset(uuid: String, token: String): Result<String>

    /**
     * Registers a new user account on the platform.
     *
     * @param uuid User email address.
     * @param password Desired account password.
     * @param nameFirst Optional first name.
     * @param nameLast Optional last name.
     * @param acceptedTOS Must be `true` to confirm acceptance of Terms & Conditions and Privacy Policy.
     * @param token Cloudflare Turnstile verification token.
     * @return A [Result] containing the registration confirmation message on success.
     */
    suspend fun register(uuid: String, password: String, nameFirst: String?, nameLast: String?, acceptedTOS: Boolean, token: String): Result<String>

    /**
     * Authenticates a user using traditional local credentials (username/password).
     *
     * @param uuid User identifier or email.
     * @param password Account password.
     * @param code Optional two-factor authentication code if 2FA is enabled.
     * @param token Cloudflare Turnstile verification token.
     * @return A [Result.success] on successful authentication, or [Result.failure] (e.g., [com.wba.sdk.utils.MissingTwoFAException]).
     */
    suspend fun login(uuid: String, password: String, code: String? = null, token: String): Result<Unit>

    /**
     * Authenticates a user using a SAML SSO authentication response payload.
     *
     * @param response The raw SAML assertion/response payload string captured from the SAML flow.
     * @return A [Result.success] on successful authentication or [Result.failure] on error.
     */
    suspend fun login(response: String): Result<Unit>

    /**
     * Logs out the current user by clearing stored session flags and JWT tokens.
     */
    fun logout()
}