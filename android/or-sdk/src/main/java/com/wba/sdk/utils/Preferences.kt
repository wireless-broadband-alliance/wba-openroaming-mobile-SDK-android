package com.wba.sdk.utils

import android.content.Context
import androidx.core.content.edit

//
//  Preferences.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 WBA. All rights reserved.
//

/**
 * Utility class wrapping Android [android.content.SharedPreferences] for simple key-value persistence.
 *
 * Uses the application's package name as the preference file name to isolate SDK storage.
 *
 * @param context Host application context used to initialize shared preferences.
 */
class Preferences(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    /**
     * Saves a string value to shared preferences.
     *
     * @param key Preference key identifier.
     * @param value String value to save, or `null` to remove/clear the key.
     */
    fun saveString(key: String, value: String?) {
        sharedPreferences.edit { putString(key, value) }
    }

    /**
     * Retrieves a string value from shared preferences.
     *
     * @param key Preference key identifier.
     * @param defaultValue Fallback value returned if the key does not exist.
     * @return Stored string value or [defaultValue].
     */
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    /**
     * Saves a long value to shared preferences.
     *
     * @param key Preference key identifier.
     * @param value Long value to persist.
     */
    fun saveLong(key: String, value: Long) {
        sharedPreferences.edit { putLong(key, value) }
    }

    /**
     * Retrieves a long value from shared preferences.
     *
     * @param key Preference key identifier.
     * @param defaultValue Fallback value returned if the key does not exist (default: `0L`).
     * @return Stored long value or [defaultValue].
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    /**
     * Saves a boolean value to shared preferences.
     *
     * @param key Preference key identifier.
     * @param value Boolean value to persist.
     */
    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    /**
     * Retrieves a boolean value from shared preferences.
     *
     * @param key Preference key identifier.
     * @param defaultValue Fallback value returned if the key does not exist (default: `false`).
     * @return Stored boolean value or [defaultValue].
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}