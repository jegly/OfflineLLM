package com.jegly.offlineLLM.ui.screens

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.offlineLLM.ai.ModelManager
import com.jegly.offlineLLM.data.local.entities.ModelInfo
import com.jegly.offlineLLM.data.repository.ChatRepository
import com.jegly.offlineLLM.data.repository.ExportData
import com.jegly.offlineLLM.data.repository.ExportedChat
import com.jegly.offlineLLM.data.repository.ExportedMessage
import com.jegly.offlineLLM.data.repository.SettingsRepository
import com.jegly.offlineLLM.ui.theme.ThemeMode
import com.jegly.offlineLLM.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class SettingsUiState(
    val models: List<ModelInfo> = emptyList(),
    val activeModel: ModelInfo? = null,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 512,
    val contextSize: Int = 4096,
    val topP: Float = 0.9f,
    val topK: Int = 40,
    val minP: Float = 0.1f,
    val repeatPenalty: Float = 1.1f,
    val biometricLock: Boolean = false,
    val autoLockOnBackground: Boolean = false,
    val screenshotProtectionEnabled: Boolean = true,
    val tapjackingProtectionEnabled: Boolean = true,
    val sensitiveDataAccessibilityEnabled: Boolean = true,
    val secureStorageBackend: String = "Unknown",
    val systemPromptKey: String = "default",
    val themeMode: String = "SYSTEM",
    val accentColor: String = "dynamic",
    val disableThinking: Boolean = true,
    val isImportingModel: Boolean = false,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val application: Application,
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository,
    private val modelManager: ModelManager,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(loadState())
    val uiState: StateFlow<SettingsUiState> = _uiState

    init {
        viewModelScope.launch {
            chatRepository.getAllModels().collect { models ->
                val activeId = settingsRepository.activeModelId
                _uiState.update {
                    it.copy(
                        models = models,
                        activeModel = models.find { m -> m.id == activeId }
                    )
                }
            }
        }
    }

    private fun loadState(): SettingsUiState {
        return SettingsUiState(
            temperature = settingsRepository.temperature,
            maxTokens = settingsRepository.maxTokens,
            contextSize = settingsRepository.contextSize,
            topP = settingsRepository.topP,
            topK = settingsRepository.topK,
            minP = settingsRepository.minP,
            repeatPenalty = settingsRepository.repeatPenalty,
            biometricLock = settingsRepository.biometricLock,
            autoLockOnBackground = settingsRepository.autoLockOnBackgroundEnabled,
            screenshotProtectionEnabled = settingsRepository.screenshotProtectionEnabled,
            tapjackingProtectionEnabled = settingsRepository.tapjackingProtectionEnabled,
            sensitiveDataAccessibilityEnabled = settingsRepository.sensitiveDataAccessibilityEnabled,
            secureStorageBackend = settingsRepository.secureStorageBackend,
            systemPromptKey = settingsRepository.systemPromptKey,
            themeMode = settingsRepository.themeMode,
            accentColor = settingsRepository.accentColor,
            disableThinking = settingsRepository.disableThinking,
        )
    }

    fun setTemperature(value: Float) {
        settingsRepository.temperature = value
        _uiState.update { it.copy(temperature = value) }
    }

    fun setMaxTokens(value: Int) {
        settingsRepository.maxTokens = value
        _uiState.update { it.copy(maxTokens = value) }
    }

    fun setContextSize(value: Int) {
        settingsRepository.contextSize = value
        _uiState.update { it.copy(contextSize = value) }
    }

    fun setTopP(value: Float) {
        settingsRepository.topP = value
        _uiState.update { it.copy(topP = value) }
    }

    fun setTopK(value: Int) {
        settingsRepository.topK = value
        _uiState.update { it.copy(topK = value) }
    }

    fun setMinP(value: Float) {
        settingsRepository.minP = value
        _uiState.update { it.copy(minP = value) }
    }

    fun setRepeatPenalty(value: Float) {
        settingsRepository.repeatPenalty = value
        _uiState.update { it.copy(repeatPenalty = value) }
    }

    fun setBiometricLock(enabled: Boolean) {
        settingsRepository.biometricLock = enabled
        _uiState.update { it.copy(biometricLock = enabled) }
    }

    fun setAutoLockOnBackground(enabled: Boolean) {
        settingsRepository.autoLockOnBackgroundEnabled = enabled
        _uiState.update { it.copy(autoLockOnBackground = enabled) }
    }

    fun setScreenshotProtectionEnabled(enabled: Boolean) {
        settingsRepository.screenshotProtectionEnabled = enabled
        _uiState.update { it.copy(screenshotProtectionEnabled = enabled) }
    }

    fun setTapjackingProtectionEnabled(enabled: Boolean) {
        settingsRepository.tapjackingProtectionEnabled = enabled
        _uiState.update { it.copy(tapjackingProtectionEnabled = enabled) }
    }

    fun setSensitiveDataAccessibilityEnabled(enabled: Boolean) {
        settingsRepository.sensitiveDataAccessibilityEnabled = enabled
        _uiState.update { it.copy(sensitiveDataAccessibilityEnabled = enabled) }
    }

    fun setSystemPrompt(key: String) {
        settingsRepository.systemPromptKey = key
        _uiState.update { it.copy(systemPromptKey = key) }
    }

    fun setTheme(mode: ThemeMode) {
        settingsRepository.themeMode = mode.name
        _uiState.update { it.copy(themeMode = mode.name) }
    }

    fun setAccentColor(key: String) {
        settingsRepository.accentColor = key
        _uiState.update { it.copy(accentColor = key) }
    }

    fun setDisableThinking(disabled: Boolean) {
        settingsRepository.disableThinking = disabled
        _uiState.update { it.copy(disableThinking = disabled) }
    }

    fun selectModel(modelId: Long) {
        settingsRepository.activeModelId = modelId
        _uiState.update { state ->
            state.copy(activeModel = state.models.find { it.id == modelId })
        }
    }

    fun deleteModel(modelId: Long) {
        viewModelScope.launch {
            modelManager.deleteModel(modelId)
        }
    }

    fun importModel(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImportingModel = true) }
            val result = modelManager.importModel(uri)
            _uiState.update { it.copy(isImportingModel = false) }
            result.fold(
                onSuccess = { model ->
                    Toast.makeText(application, "Model imported: ${model.name}", Toast.LENGTH_SHORT).show()
                },
                onFailure = { e ->
                    Toast.makeText(application, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    fun exportChats(uri: Uri) {
        viewModelScope.launch {
            try {
                val json = Json { prettyPrint = true }
                val exportData = ExportData(chats = emptyList())
                val jsonString = json.encodeToString(exportData)
                FileUtils.writeTextToUri(application, uri, jsonString)
                Toast.makeText(application, "Chats exported", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(application, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun importChats(uri: Uri) {
        viewModelScope.launch {
            try {
                val jsonString = FileUtils.readTextFromUri(application, uri)
                if (jsonString != null) {
                    val result = chatRepository.importChatsFromJson(jsonString)
                    result.fold(
                        onSuccess = { count ->
                            Toast.makeText(application, "Imported $count chats", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(application, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(application, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun clearAllChats() {
        viewModelScope.launch {
            chatRepository.deleteAllConversations()
            Toast.makeText(application, "All chats cleared", Toast.LENGTH_SHORT).show()
        }
    }
}
