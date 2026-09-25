package com.wba.sdk.core

import com.wba.sdk.config.Config
import com.wba.sdk.saml.SAML
import com.wba.sdk.turnstile.Turnstile
import com.wba.sdk.user.User

//
//  SDK.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Main entry point for accessing OpenRoaming SDK feature modules.
 *
 * Provides accessors to [Config], [Turnstile], [User], and [SAML] instances.
 * The SDK is automatically initialized on application startup via [SDKInitializer].
 */
object SDK {

    private lateinit var configInstance: Config
    private lateinit var turnstileInstance: Turnstile
    private lateinit var userInstance: User
    private lateinit var samlInstance: SAML

    /**
     * Internal method invoked during application bootstrap to inject concrete feature implementations.
     *
     * @param config The [Config] instance.
     * @param turnstile The [Turnstile] instance.
     * @param user The [User] instance.
     * @param saml The [SAML] instance.
     */
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

    /**
     * Retrieves the [Config] module instance for remote and local configuration management.
     *
     * @return The active [Config] instance.
     * @throws IllegalStateException If the SDK has not been initialized properly.
     */
    fun getConfig(): Config = if (!::configInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
    else configInstance

    /**
     * Retrieves the [Turnstile] module instance for Cloudflare challenge resolution.
     *
     * @return The active [Turnstile] instance.
     * @throws IllegalStateException If the SDK has not been initialized properly.
     */
    fun getTurnstile(): Turnstile = if (!::turnstileInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
    else turnstileInstance

    /**
     * Retrieves the [User] module instance for authentication and account management.
     *
     * @return The active [User] instance.
     * @throws IllegalStateException If the SDK has not been initialized properly.
     */
    fun getUser(): User = if (!::userInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
    else userInstance

    /**
     * Retrieves the [SAML] module instance for SAML Single Sign-On operations.
     *
     * @return The active [SAML] instance.
     * @throws IllegalStateException If the SDK has not been initialized properly.
     */
    fun getSAML(): SAML = if (!::samlInstance.isInitialized) throw IllegalStateException("SDK initialization failed.")
    else samlInstance
}