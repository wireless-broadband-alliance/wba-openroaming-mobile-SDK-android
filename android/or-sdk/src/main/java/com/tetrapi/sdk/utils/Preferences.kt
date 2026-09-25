package com.tetrapi.sdk.utils

import android.content.Context
import androidx.core.content.edit

//
//  Preferences.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class Preferences(context: Context) {

    private val sharedPreferences = context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)

    fun saveString(key: String, value: String?) {
        sharedPreferences.edit { putString(key, value) }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun saveLong(key: String, value: Long) {
        sharedPreferences.edit { putLong(key, value) }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        sharedPreferences.edit { putBoolean(key, value) }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }
}
