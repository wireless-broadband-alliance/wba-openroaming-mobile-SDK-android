package com.tetrapi.sdk.user

import org.json.JSONObject

//
//  User.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

interface User {

    val isLoggedIn: Boolean
    val jwtToken: String?

    suspend fun info(): Result<JSONObject>

    suspend fun code(uuid: String, password: String, type: String, token: String): Result<String>

    suspend fun validate(uuid: String, password: String, code: String): Result<String>

    suspend fun reset(uuid: String, token: String): Result<String>

    suspend fun register(uuid: String, password: String, nameFirst: String?, nameLast: String?, acceptedTOS: Boolean, token: String): Result<String>

    suspend fun login(uuid: String, password: String, code: String? = null, token: String): Result<Unit>

    suspend fun login(response: String): Result<Unit>

    fun logout()
}