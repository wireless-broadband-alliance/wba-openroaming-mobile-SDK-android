# Release V1.0.0

### Breaking Changes & Architectural Refactoring

- **Decoupling of SDK and Application**: Separated the monolithic codebase into two independent modules:
  - **`or-sdk`**: The core, standalone OpenRoaming Android SDK library.
  - **Action Required:** Existing installations must update their build configuration to import `or-sdk` as a project dependency:
    ```kotlin
    // settings.gradle.kts
    include(":or-sdk")

    // app/build.gradle.kts
    dependencies {
        implementation(project(":or-sdk"))
    }
    ```

- **Unified SDK Entry Point**: Refactored module initialization to use a central `SDK` singleton service locator.
  - Direct class instantiation for core services has been replaced in favor of `SDK.getUser()`, `SDK.getConfig()`, and `SDK.getTurnstile()`.

### New Features & Core Functionality

- **Wi-Fi Passpoint (Hotspot 2.0) Provisioning**:
  - Ephemeral RSA key pair generation and encrypted RADIUS credential exchange.
  - Automated `PasspointConfiguration` installation and removal on Android devices via `WifiNetworkSuggestion`.

- **Comprehensive Authentication Options**:
  - Support for local credential login (username and password).
  - Two-Factor Authentication (2FA / TOTP) challenge validation handling (`MissingTwoFAException`).
  - SAML Single Sign-On (SSO) browser assertion and token exchange flows.

- **Cloudflare Turnstile Anti-Bot Protection**:
  - Integrated `WebView` helper and JavaScript bridge for resolving Turnstile verification challenges directly within the app flow.

- **Zero-Setup App Startup Bootstrapping**:
  - Automated initialization using AndroidX App Startup (`androidx.startup:startup-runtime`), eliminating mandatory manual `init()` calls in `Application` classes.

- **Session & Local Persistence**:
  - Encrypted local storage wrapper (`Preferences`) for JWT session tokens, refresh state, and user login metadata via `SharedPreferences`.

### Platform Requirements & Dependencies

- Target Android API Level 30 (Android 11.0) or higher.
- Requires Kotlin 1.9+.
- Integrated core third-party libraries:
  - `androidx.startup:startup-runtime`
  - `io.ktor:ktor-client-okhttp`
  - `com.google.android.material:material`

---

# Release V0.9.0 (Legacy Monolith)

- Initial prototype containing integrated UI and OpenRoaming logic within a single application module prior to SDK separation.
