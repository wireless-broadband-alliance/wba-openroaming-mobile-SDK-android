package com.wba.sdk.saml

import androidx.appcompat.app.AppCompatActivity

//
//  SAML.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Interface defining the SAML (Security Assertion Markup Language) authentication flow.
 *
 * Provides properties and methods to retrieve the SAML entry point URL and initiate
 * interactive web-based Single Sign-On (SSO) authentication.
 */
interface SAML {

    /**
     * The starting SAML authentication URL retrieved from local storage,
     * or `null` if the configuration has not been set yet.
     */
    val url: String?

    /**
     * Initiates the SAML web authentication process within the provided [AppCompatActivity].
     *
     * This method opens an overlay web interface, handles SAML redirects, captures the resulting
     * SAML response payload, and returns it asynchronously.
     *
     * @param activity The host [AppCompatActivity] where the SAML web view will be rendered.
     * @return A [Result] containing the SAML response token/payload string on success,
     * or an exception if the URL is missing, web loading fails, or authentication is canceled.
     */
    suspend fun start(activity: AppCompatActivity): Result<String>
}