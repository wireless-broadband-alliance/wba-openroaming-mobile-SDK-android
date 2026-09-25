package com.tetrapi.sdk.core

import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.net.wifi.hotspot2.PasspointConfiguration
import android.net.wifi.hotspot2.pps.Credential
import android.net.wifi.hotspot2.pps.HomeSp
import android.os.Build
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.tetrapi.sdk.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.util.Base64
import javax.crypto.Cipher

//
//  OpenRoaming.kt
//  OpenRoaming SDK
//
//  Created by Fábio Carvalho
//  Copyright © 2026 Tetrapi. All rights reserved.
//

class OpenRoaming {

    private fun isPasspointSupported(context: Context): Boolean {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return isPasspointSupported(context, wifiManager)
    }

    private fun isPasspointSupported(context: Context, wifiManager: WifiManager) = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> wifiManager.isWifiPasspointEnabled
        else -> context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_PASSPOINT)
    }

    private fun generateRSAKeys(): KeyPair {
        val generator = KeyPairGenerator.getInstance("RSA")
        generator.initialize(1024)

        return generator.generateKeyPair()
    }

    private fun decryptPassword(key: PrivateKey, password: String): String {
        val encryptedBytes = Base64.getDecoder().decode(password)

        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, key)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8).trim()
    }

    fun connect(activity: FragmentActivity) = activity.lifecycleScope.launch(Dispatchers.IO) {
        // Checks if device supports OpenRoaming
        val isSupported = isPasspointSupported(activity)
        if (!isSupported) return@launch onConnectionError(activity.getString(R.string.open_roaming_not_supported))

        // Generate RSA Keys to get Android profile
        val keyPair = generateRSAKeys()
        val publicKeyBase64 = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val publicPemFormatted = "-----BEGIN PUBLIC KEY-----\n$publicKeyBase64\n-----END PUBLIC KEY-----"

        val token = SDK.getUser().jwtToken
        val params = JSONObject().apply {
            put("public_key", publicPemFormatted)
        }

        val request = runCatching {
            val api = API(activity)
            api.profile(params, token ?: "todo")
        }

        request.onSuccess {
            val success = it.getBoolean("success")
            activity.runOnUiThread {
                if (success) connect(activity, keyPair, it.getJSONObject("data"))
                else onConnectionError(it.getString("error"))
            }
        }

        request.onFailure {

            activity.runOnUiThread {
                onConnectionError(it.message)
            }
        }
    }

    private fun connect(context: Context, keyPair: KeyPair, data: JSONObject) {
        // Set up HomeSp (Home Service Provider) information
        val homeSp = HomeSp()
        homeSp.friendlyName = data.getString("friendlyName")
        homeSp.fqdn = data.getString("fqdn")

        val ois = data.getJSONArray("roamingConsortiumOis")
        val oi1 = ois.getString(0).toLong(16)
        val oi2 = ois.getString(1).toLong(16)
        homeSp.roamingConsortiumOis = longArrayOf(oi1, oi2)

        // Set up User Credentials
        val userCredential = Credential.UserCredential()
        userCredential.username = data.getString("radiusUsername")

        val password = decryptPassword(keyPair.private, data.getString("radiusPassword"))
        val passwordB64 = Base64.getEncoder().encodeToString(password.toByteArray(Charsets.UTF_8))
        userCredential.password = passwordB64

        userCredential.eapType = data.getInt("eapType")
        userCredential.nonEapInnerMethod = data.getString("nonEapInnerMethod")

        // Set up Credential for authentication
        val credential = Credential()
        credential.realm = data.getString("realm")
        credential.userCredential = userCredential

        // Create a new PasspointConfiguration instance
        val passpointConfig = PasspointConfiguration()
        passpointConfig.homeSp = homeSp
        passpointConfig.credential = credential

        // Add or update the Passpoint configuration
        val suggestions = ArrayList<WifiNetworkSuggestion>()
        suggestions.add(WifiNetworkSuggestion.Builder().setPasspointConfig(passpointConfig).build())

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.addNetworkSuggestions(suggestions)

        onConnectionSuccess()
    }

    var onConnectionSuccess: () -> Unit = {
        /** Connection succeeded */
    }

    var onConnectionError: (message: String?) -> Unit = {
        /** Connection failed */
    }

    fun networks(context: Context): ArrayList<PasspointConfiguration> {
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.networkSuggestions.mapNotNull { it.passpointConfig }.toCollection(ArrayList())
    }

    fun remove(context: Context, network: PasspointConfiguration) {
        val suggestions = WifiNetworkSuggestion.Builder()
            .setPasspointConfig(network)
            .build()

        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiManager.removeNetworkSuggestions(listOf(suggestions))
    }
}