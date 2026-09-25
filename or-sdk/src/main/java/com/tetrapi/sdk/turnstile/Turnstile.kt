package com.tetrapi.sdk.turnstile

import androidx.appcompat.app.AppCompatActivity

//
//  Turnstile.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

interface Turnstile {

    val siteKey: String?

    suspend fun getToken(activity: AppCompatActivity): Result<String>
}