package com.tetrapi.sdk.core

import android.content.Context
import com.tetrapi.sdk.R
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
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class API(private val context: Context) {

    private val client = HttpClient(OkHttp) {

        defaultRequest {
            header("User-Agent", "Android/App")
            header("Accept", "application/json")
        }
    }

    suspend fun config(): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "config")
        val response = client.get(url).bodyAsText()
        return JSONObject(response)
    }

    suspend fun info(token: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "user")
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
        }.bodyAsText()

        return JSONObject(response)
    }

    suspend fun code(params: JSONObject, type: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "twoFA/$type")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    suspend fun validate(params: JSONObject): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "twoFA/validate")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

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

    suspend fun register(params: JSONObject): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "auth/local/register")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    suspend fun reset(params: JSONObject): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "auth/local/reset")
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

    suspend fun refreshJwt(params: JSONObject, token: String): JSONObject {
        val url = String.format(context.getString(R.string.open_roaming_api), "user")
        val response = client.get(url) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(params.toString().trimIndent())
        }.bodyAsText()

        return JSONObject(response)
    }

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