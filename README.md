# OpenRoaming Android SDK

The official WBA Android SDK for integrating **OpenRoaming (Wi-Fi Passpoint / Hotspot 2.0)** network provisioning, user authentication (Local & SAML SSO), Cloudflare Turnstile verification, and remote configuration management.

---

## 🚀 Features

- **Passpoint (Hotspot 2.0) Provisioning:** Ephemeral RSA key pair generation, encrypted RADIUS credential exchange, and automated `PasspointConfiguration` installation/removal via `WifiNetworkSuggestion`.
- **Flexible Authentication:** Support for local credentials (username/password), two-factor authentication (2FA/TOTP) validation, and SAML SSO assertion flows.
- **Cloudflare Turnstile Protection:** Integrated `WebView` handler for anti-bot challenge resolution.
- **Zero-Setup Initialization:** Transparent application startup bootstrapping powered by AndroidX App Startup (`androidx.startup`).
- **Session & Token Persistence:** Local preference storage and lifecycle management for JWT tokens and login state.

---

## 📋 Requirements

- **Android SDK:** API Level 26 (Android 8.0) or higher.
- **Hardware Support:** Device with Wi-Fi Passpoint (Hotspot 2.0) capability.
- **Kotlin:** 1.9+
- **Core Dependencies:**
  - `androidx.startup:startup-runtime`
  - `io.ktor:ktor-client-okhttp`
  - `com.google.android.material:material`

---

## 🛠️ Installation

Add the SDK dependency to your module-level `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.wba.sdk:openroaming-sdk:1.0.0")
}
```

Ensure the base API endpoint format string and fallback strings are defined in your project's `res/values/strings.xml`:

```xml
<resources>
    <string name="open_roaming_api">[https://api.openroaming.example.com/v1/%s](https://api.openroaming.example.com/v1/%s)</string>
    <string name="open_roaming_not_supported">Your device does not support Wi-Fi Passpoint (OpenRoaming).</string>
</resources>
```

---

## 💡 Usage

The SDK initializes automatically during application startup via `SDKInitializer`. You can access all feature modules through the `SDK` singleton.

### 1. Fetch Remote Configuration

```kotlin
lifecycleScope.launch {
    SDK.getConfig().fetch().onSuccess { config ->
        val platform = config.optJSONObject("platform")
        val turnstileSiteKey = SDK.getConfig().turnstileSiteKey
    }.onFailure { error ->
        // Handle configuration fetch error
    }
}
```

### 2. User Authentication

#### Local Authentication (with Turnstile)

```kotlin
lifecycleScope.launch {
    val turnstileToken = "TOKEN_OBTAINED_FROM_TURNSTILE"

    SDK.getUser().login(
        uuid = "user@example.com",
        password = "your_password",
        code = null, // Pass 2FA code if required
        token = turnstileToken
    ).onSuccess {
        // User successfully authenticated
    }.onFailure { error ->
        if (error is MissingTwoFAException) {
            // Prompt user for 2FA / TOTP code
        }
    }
}
```

#### SAML SSO Authentication

```kotlin
lifecycleScope.launch {
    val samlResponsePayload = "SAML_RESPONSE_STRING"

    SDK.getUser().login(response = samlResponsePayload).onSuccess {
        // SAML login successful
    }
}
```

#### Session State & JWT Token Access

```kotlin
val isLoggedIn: Boolean = SDK.getUser().isLoggedIn
val jwtToken: String? = SDK.getUser().jwtToken

// Logout and clear local session state
SDK.getUser().logout()
```

### 3. Cloudflare Turnstile Integration

Render and process Turnstile challenges using the provided `WebView` helper:

```kotlin
val webView = findViewById<WebView>(R.id.turnstileWebView)

SDK.getTurnstile().webView(
    webView = webView,
    onSuccess = { token ->
        // Use token for login, registration, or password reset
    },
    onError = { errorMessage ->
        // Handle Turnstile error
    }
)
```

### 4. Connect to OpenRoaming Network (Passpoint)

To request, decrypt credentials, and install the Passpoint network profile on the device:

```kotlin
val openRoaming = OpenRoaming()

openRoaming.onConnectionSuccess = {
    // Passpoint profile successfully installed
}

openRoaming.onConnectionError = { message ->
    // Profile installation failed or unsupported hardware
}

// Triggers network provisioning
openRoaming.connect(this@MainActivity)
```

### 5. Managing Installed Passpoint Networks

```kotlin
val openRoaming = OpenRoaming()

// Retrieve installed Passpoint network suggestions
val networks = openRoaming.networks(context)

// Remove a specific network suggestion
networks.firstOrNull()?.let { network ->
    openRoaming.remove(context, network)
}
```

---

## 🏗️ SDK Architecture Overview

| Module | Class / Interface | Description |
| :--- | :--- | :--- |
| **Core** | `SDK` | Main entry point and service locator singleton. |
| **Core** | `OpenRoaming` | Passpoint provisioning engine and `WifiNetworkSuggestion` manager. |
| **Core** | `API` | Internal Ktor network client for backend HTTP communication. |
| **User** | `User` / `UserImpl` | Account authentication (Local/SAML), 2FA validation, and session state. |
| **Config** | `Config` / `ConfigImpl` | Remote platform configuration fetching and property caching. |
| **Turnstile** | `Turnstile` / `TurnstileImpl` | Anti-bot Cloudflare Turnstile challenge loader and solver. |
| **SAML** | `SAML` / `SAMLImpl` | SAML SSO URL generation and authentication flow management. |
| **Utils** | `Preferences` | Wrapper around `SharedPreferences` for localized persistent storage. |

---

## 📄 License

Copyright © 2026 **WBA (Wireless Broadband Alliance)**. All rights reserved.
