package com.wba.sdk.core

import android.content.Context
import com.wba.sdk.R
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import org.json.JSONObject

//
//  API.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Internal network service client using Ktor with OkHttp engine to execute HTTP requests against
 * OpenRoaming backend endpoints.
 *
 * @property context Application context used to resolve API endpoint format strings from resources.
 */
class API(private val context: Context) {

    private val client = HttpClient(OkHttp) {
        defaultRequest {
            header("User-Agent", "Android/App")
            header("Accept", "application/json")
        }
    }

    /**
     * Fetches SDK remote configurations.
     *
     * @return [JSONObject] response containing platform, auth, turnstile, and SAML settings.
     */
    suspend fun config(): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "config")
        val response = client.get(url).bodyAsText()
        return JSONObject(response)
    }

    /**
     * Fetches current authenticated user profile metadata.
     *
     * @param token Bearer JWT session token.
     * @return [JSONObject] response containing user details.
     */
    suspend fun info(token: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "user")
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.bodyAsText()

        return JSONObject(response)
    }

    /**
     * Requests a 2FA code or TOTP configuration.
     *
     * @param params Request body payload JSON.
     * @param type Code delivery channel type (e.g., `"email"`, `"totp"`).
     * @return [JSONObject] response containing code message or TOTP ID.
     */
    suspend fun code(params: JSONObject, type: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "twoFA/$type")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    /**
     * Validates a submitted 2FA code.
     *
     * @param params JSON payload containing user credentials and the 2FA code.
     * @return [JSONObject] response indicating validation outcome.
     */
    suspend fun validate(params: JSONObject): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "twoFA/validate")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    /**
     * Executes user authentication (local JSON or SAML Form submission).
     *
     * @param params Payload containing credentials or SAML assertion.
     * @param type Auth scheme type (`"local"` or `"saml"`).
     * @return [JSONObject] response containing session tokens.
     */
    suspend fun login(params: JSONObject, type: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "auth/$type")
        val response = client.post(url) {

            when (type) {
                "local" -> {
                    contentType(ContentType.Application.Json)
                    setBody(params.toString())
                }
                else -> setBody(FormDataContent(Parameters.build {
                    val response = params.optString("response")
                    append("SAMLResponse", response)
                }))
            }
        }.bodyAsText()

        return JSONObject(response)
    }

    /**
     * Submits new account registration parameters.
     *
     * @param params Registration payload containing user details and Turnstile token.
     * @return [JSONObject] response containing registration outcome.
     */
    suspend fun register(params: JSONObject): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "auth/local/register")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    /**
     * Triggers password reset request for a user account.
     *
     * @param params Payload containing target email and Turnstile token.
     * @return [JSONObject] response confirming reset email dispatch.
     */
    suspend fun reset(params: JSONObject): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "auth/local/reset")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    /**
     * Refreshes user session JWT token.
     *
     * @param params JSON parameters.
     * @param token Existing Bearer JWT token.
     * @return [JSONObject] response with fresh token data.
     */
    suspend fun refreshJwt(params: JSONObject, token: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "user")
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    /**
     * Requests Android Passpoint profile configuration parameters using the generated public key.
     *
     * @param params Payload containing formatted RSA public key.
     * @param token Active Bearer JWT token.
     * @return [JSONObject] response containing Passpoint profile, FQDN, OIs, and encrypted credentials.
     */
    suspend fun profile(params: JSONObject, token: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "config/profile/android")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }
}