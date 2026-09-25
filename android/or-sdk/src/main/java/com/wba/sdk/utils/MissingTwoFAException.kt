package com.wba.sdk.utils

/**
 * Custom exception thrown when an authentication attempt fails specifically due to
 * a missing Two-Factor Authentication (2FA/TOTP) code.
 *
 * @param message Detailed error message returned by the server or SDK.
 */
class MissingTwoFAException(message: String) : Exception(message)