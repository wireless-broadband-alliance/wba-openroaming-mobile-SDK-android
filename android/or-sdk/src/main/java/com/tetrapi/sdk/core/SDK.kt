package com.tetrapi.sdk.core

import com.tetrapi.sdk.config.Config
import com.tetrapi.sdk.saml.SAML
import com.tetrapi.sdk.turnstile.Turnstile
import com.tetrapi.sdk.user.User

//
//  SDK.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

object SDK {

    private lateinit var configInstance: Config
    private lateinit var turnstileInstance: Turnstile
    private lateinit var userInstance: User
    private lateinit var samlInstance: SAML

    internal fun initialize(
        config: Config,
        turnstile: Turnstile,
        user: User,
        saml: SAML
    ) {
        turnstileInstance = turnstile
        configInstance = config
        userInstance = user
        samlInstance = saml
    }

    fun getConfig() = if (!::configInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
        else configInstance

    fun getTurnstile() = if (!::turnstileInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
        else turnstileInstance

    fun getUser() = if (!::userInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
        else userInstance

    fun getSAML() = if (!::samlInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
        else samlInstance
}