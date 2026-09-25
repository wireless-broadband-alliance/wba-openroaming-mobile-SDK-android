package com.tetrapi.sdk.saml

import androidx.appcompat.app.AppCompatActivity

//
//  SAML.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

interface SAML {

    val url: String?

    suspend fun start(activity: AppCompatActivity): Result<String>
}