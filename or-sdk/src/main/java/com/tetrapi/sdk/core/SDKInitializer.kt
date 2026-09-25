package com.tetrapi.sdk.core

import android.content.Context
import androidx.startup.Initializer
import com.tetrapi.sdk.config.ConfigImpl
import com.tetrapi.sdk.saml.SAMLImpl
import com.tetrapi.sdk.turnstile.TurnstileImpl
import com.tetrapi.sdk.user.UserImpl
import com.tetrapi.sdk.utils.Preferences

//
//  SDKInitializer.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class SDKInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        val preferences = Preferences(context)
        val api = API(context)

        return SDK.initialize(
            config = ConfigImpl(preferences, api),
            turnstile = TurnstileImpl(preferences),
            user = UserImpl(preferences, api),
            saml = SAMLImpl(preferences)
        )
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}