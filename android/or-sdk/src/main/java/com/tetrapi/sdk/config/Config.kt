package com.tetrapi.sdk.config

//
//  Config.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

interface Config {

    val isExpired: Boolean
    val termsOfService: String?
    val privacyPolicy: String?

    suspend fun fetchConfig(expirationDays: Int = 2): Result<Unit>

    fun isAuthActive(type: String): Boolean
}