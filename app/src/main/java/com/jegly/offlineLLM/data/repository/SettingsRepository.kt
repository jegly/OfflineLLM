package com.jegly.offlineLLM.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Prefer a StrongBox-backed master key when available. Fall back gracefully when not.
    private val masterKeyAndBackend: Pair<MasterKey, String> by lazy { createMasterKeyAndBackend() }
    val secureStorageBackend: String
        get() = masterKeyAndBackend.second

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "offlinellm_secure_prefs",
            MASTER_KEY_ALIAS,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        private const val MASTER_KEY_ALIAS = "offlinellm_secure_prefs_master_key"

        const val KEY_TEMPERATURE = "temperature"
        const val KEY_MAX_TOKENS = "max_tokens"
        const val KEY_CONTEXT_SIZE = "context_size"
        const val KEY_TOP_P = "top_p"
        const val KEY_TOP_K = "top_k"
        const val KEY_MIN_P = "min_p"
        const val KEY_REPEAT_PENALTY = "repeat_penalty"
        const val KEY_BIOMETRIC_LOCK = "biometric_lock"
        const val KEY_SCREENSHOT_PROTECTION = "screenshot_protection"
        const val KEY_TAPJACKING_PROTECTION = "tapjacking_protection"
        const val KEY_SENSITIVE_DATA_ACCESSIBILITY = "sensitive_data_accessibility"
        const val KEY_AUTO_LOCK_ON_BACKGROUND = "auto_lock_on_background"
        const val KEY_ACTIVE_MODEL_ID = "active_model_id"
        const val KEY_SYSTEM_PROMPT_KEY = "system_prompt_key"
        const val KEY_CUSTOM_SYSTEM_PROMPT = "custom_system_prompt"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        const val KEY_NUM_THREADS = "num_threads"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_DISABLE_THINKING = "disable_thinking"
        const val KEY_MATH_LATEX_HINTS = "math_latex_hints"
        const val KEY_TRANSLATOR_FROM = "translator_from"
        const val KEY_TRANSLATOR_TO = "translator_to"
        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_MAX_TOKENS = 2048
        const val DEFAULT_CONTEXT_SIZE = 4096
        const val DEFAULT_TOP_P = 0.9f
        const val DEFAULT_TOP_K = 40
        const val DEFAULT_MIN_P = 0.1f
        const val DEFAULT_REPEAT_PENALTY = 1.1f
        val DEFAULT_NUM_THREADS = (Runtime.getRuntime().availableProcessors() / 2).coerceIn(4, 8)

        const val DEFAULT_SCREENSHOT_PROTECTION = true
        const val DEFAULT_TAPJACKING_PROTECTION = true
        const val DEFAULT_SENSITIVE_DATA_ACCESSIBILITY = true
        const val DEFAULT_AUTO_LOCK_ON_BACKGROUND = false
    }

    private fun createMasterKeyAndBackend(): Pair<MasterKey, String> {
        val strongBoxAvailable = try {
            context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
        } catch (_: Exception) {
            false
        }

        return try {
            val requestedKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .setRequestStrongBoxBacked(true)
                .build()

            // Jetpack Security doesn't consistently expose StrongBox state across versions.
            // Use best-effort reflection; fall back to feature detection.
            val reflectStrongBox = runCatching {
                val m = requestedKey.javaClass.getMethod("isStrongBoxBacked")
                (m.invoke(requestedKey) as? Boolean) ?: false
            }.getOrDefault(false)

            val backend = when {
                reflectStrongBox -> "StrongBox"
                strongBoxAvailable -> "TEE" // StrongBox available, but the key may have fallen back.
                else -> "TEE"
            }
            requestedKey to backend
        } catch (_: Exception) {
            val fallbackKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            fallbackKey to "TEE"
        }
    }

    var temperature: Float
        get() = prefs.getFloat(KEY_TEMPERATURE, DEFAULT_TEMPERATURE)
        set(value) = prefs.edit().putFloat(KEY_TEMPERATURE, value).apply()

    var maxTokens: Int
        get() = prefs.getInt(KEY_MAX_TOKENS, DEFAULT_MAX_TOKENS)
        set(value) = prefs.edit().putInt(KEY_MAX_TOKENS, value).apply()

    var contextSize: Int
        get() = prefs.getInt(KEY_CONTEXT_SIZE, DEFAULT_CONTEXT_SIZE)
        set(value) = prefs.edit().putInt(KEY_CONTEXT_SIZE, value).apply()

    var topP: Float
        get() = prefs.getFloat(KEY_TOP_P, DEFAULT_TOP_P)
        set(value) = prefs.edit().putFloat(KEY_TOP_P, value).apply()

    var topK: Int
        get() = prefs.getInt(KEY_TOP_K, DEFAULT_TOP_K)
        set(value) = prefs.edit().putInt(KEY_TOP_K, value).apply()

    var minP: Float
        get() = prefs.getFloat(KEY_MIN_P, DEFAULT_MIN_P)
        set(value) = prefs.edit().putFloat(KEY_MIN_P, value).apply()

    var repeatPenalty: Float
        get() = prefs.getFloat(KEY_REPEAT_PENALTY, DEFAULT_REPEAT_PENALTY)
        set(value) = prefs.edit().putFloat(KEY_REPEAT_PENALTY, value).apply()

    var biometricLock: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_LOCK, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, value).apply()

    var screenshotProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SCREENSHOT_PROTECTION, DEFAULT_SCREENSHOT_PROTECTION)
        set(value) = prefs.edit().putBoolean(KEY_SCREENSHOT_PROTECTION, value).apply()

    var tapjackingProtectionEnabled: Boolean
        get() = prefs.getBoolean(KEY_TAPJACKING_PROTECTION, DEFAULT_TAPJACKING_PROTECTION)
        set(value) = prefs.edit().putBoolean(KEY_TAPJACKING_PROTECTION, value).apply()

    var sensitiveDataAccessibilityEnabled: Boolean
        get() = prefs.getBoolean(KEY_SENSITIVE_DATA_ACCESSIBILITY, DEFAULT_SENSITIVE_DATA_ACCESSIBILITY)
        set(value) = prefs.edit().putBoolean(KEY_SENSITIVE_DATA_ACCESSIBILITY, value).apply()

    var autoLockOnBackgroundEnabled: Boolean
        get() = prefs.getBoolean(KEY_AUTO_LOCK_ON_BACKGROUND, DEFAULT_AUTO_LOCK_ON_BACKGROUND)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_LOCK_ON_BACKGROUND, value).apply()

    var activeModelId: Long
        get() = prefs.getLong(KEY_ACTIVE_MODEL_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_MODEL_ID, value).apply()

    var systemPromptKey: String
        get() = prefs.getString(KEY_SYSTEM_PROMPT_KEY, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_SYSTEM_PROMPT_KEY, value).apply()

    var customSystemPrompt: String
        get() = prefs.getString(KEY_CUSTOM_SYSTEM_PROMPT, "") ?: ""
        set(value) = prefs.edit().putString(KEY_CUSTOM_SYSTEM_PROMPT, value).apply()

    var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()

    var numThreads: Int
        get() = prefs.getInt(KEY_NUM_THREADS, DEFAULT_NUM_THREADS)
        set(value) = prefs.edit().putInt(KEY_NUM_THREADS, value).apply()

    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

    var accentColor: String
        get() = prefs.getString(KEY_ACCENT_COLOR, "dynamic") ?: "dynamic"
        set(value) = prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()

    var disableThinking: Boolean
        get() = prefs.getBoolean(KEY_DISABLE_THINKING, true)
        set(value) = prefs.edit().putBoolean(KEY_DISABLE_THINKING, value).apply()

    var mathLatexHints: Boolean
        get() = prefs.getBoolean(KEY_MATH_LATEX_HINTS, false)
        set(value) = prefs.edit().putBoolean(KEY_MATH_LATEX_HINTS, value).apply()

    var translatorFrom: String
        get() = prefs.getString(KEY_TRANSLATOR_FROM, "en") ?: "en"
        set(value) = prefs.edit().putString(KEY_TRANSLATOR_FROM, value).apply()

    var translatorTo: String
        get() = prefs.getString(KEY_TRANSLATOR_TO, "es") ?: "es"
        set(value) = prefs.edit().putString(KEY_TRANSLATOR_TO, value).apply()
}
