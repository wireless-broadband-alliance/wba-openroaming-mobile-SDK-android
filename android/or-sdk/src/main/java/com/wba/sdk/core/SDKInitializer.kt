package com.wba.sdk.core

import android.content.Context
import androidx.startup.Initializer
import com.wba.sdk.config.ConfigImpl
import com.wba.sdk.saml.SAMLImpl
import com.wba.sdk.turnstile.TurnstileImpl
import com.wba.sdk.user.UserImpl
import com.wba.sdk.utils.Preferences

//
//  SDKInitializer.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * AndroidX App Startup [Initializer] that automatically bootstraps the OpenRoaming SDK
 * during application startup.
 *
 * Instantiates required preferences, network utilities, and module implementations,
 * injecting them into the [SDK] singleton.
 */
class SDKInitializer : Initializer<Unit> {

    /**
     * Creates and configures the SDK dependencies on application startup.
     *
     * @param context Application context supplied by App Startup.
     */
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

    /**
     * Specifies dependencies required before this initializer runs.
     *
     * @return An empty list as [SDKInitializer] has no external App Startup dependencies.
     */
    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}