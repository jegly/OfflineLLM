package com.jegly.offlineLLM.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.offlineLLM.ai.ModelManager
import com.jegly.offlineLLM.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val modelManager: ModelManager,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _copyProgress = MutableStateFlow(0f)
    val copyProgress: StateFlow<Float> = _copyProgress

    private val _copyDone = MutableStateFlow(false)
    val copyDone: StateFlow<Boolean> = _copyDone

    private val _copyError = MutableStateFlow<String?>(null)
    val copyError: StateFlow<String?> = _copyError

    fun startModelCopy() {
        viewModelScope.launch {
            // Collect progress from ModelManager
            launch {
                modelManager.copyProgress.collect { progress ->
                    _copyProgress.value = progress
                }
            }

            val result = modelManager.copyBundledModelIfNeeded()
            result.fold(
                onSuccess = {
                    _copyDone.value = true
                },
                onFailure = { e ->
                    _copyError.value = e.message ?: "Failed to set up model"
                    // Still allow continuing - user can import model later
                    _copyDone.value = true
                }
            )
        }
    }

    fun completeOnboarding(systemPromptKey: String) {
        settingsRepository.systemPromptKey = systemPromptKey
        settingsRepository.onboardingComplete = true
    }
}
