package com.jegly.offlineLLM.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            "offlinellm_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    companion object {
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_MAX_TOKENS = "max_tokens"
        const val KEY_CONTEXT_SIZE = "context_size"
        const val KEY_BIOMETRIC_LOCK = "biometric_lock"
        const val KEY_ACTIVE_MODEL_ID = "active_model_id"
        const val KEY_SYSTEM_PROMPT_KEY = "system_prompt_key"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        const val KEY_NUM_THREADS = "num_threads"
        const val KEY_THEME_MODE = "theme_mode"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_DISABLE_THINKING = "disable_thinking"

        const val DEFAULT_TEMPERATURE = 0.7f
        const val DEFAULT_MAX_TOKENS = 512
        const val DEFAULT_CONTEXT_SIZE = 4096
        const val DEFAULT_NUM_THREADS = 4
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

    var biometricLock: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_LOCK, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_LOCK, value).apply()

    var activeModelId: Long
        get() = prefs.getLong(KEY_ACTIVE_MODEL_ID, -1L)
        set(value) = prefs.edit().putLong(KEY_ACTIVE_MODEL_ID, value).apply()

    var systemPromptKey: String
        get() = prefs.getString(KEY_SYSTEM_PROMPT_KEY, "default") ?: "default"
        set(value) = prefs.edit().putString(KEY_SYSTEM_PROMPT_KEY, value).apply()

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
}
