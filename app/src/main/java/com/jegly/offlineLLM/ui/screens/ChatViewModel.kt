package com.jegly.offlineLLM.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.jegly.offlineLLM.ai.InferenceEngine
import com.jegly.offlineLLM.ai.ModelManager
import com.jegly.offlineLLM.ai.PromptFormatter
import com.jegly.offlineLLM.ai.SystemPrompts
import com.jegly.offlineLLM.data.local.entities.Conversation
import com.jegly.offlineLLM.data.local.entities.Message
import com.jegly.offlineLLM.data.repository.ChatRepository
import com.jegly.offlineLLM.data.repository.ExportData
import com.jegly.offlineLLM.data.repository.ExportedChat
import com.jegly.offlineLLM.data.repository.ExportedMessage
import com.jegly.offlineLLM.data.repository.SettingsRepository
import com.jegly.offlineLLM.utils.MemoryMonitor
import com.jegly.offlineLLM.utils.TtsHelper
import com.jegly.offlineLLM.utils.SecurityUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val currentConversation: Conversation? = null,
    val messages: List<Message> = emptyList(),
    val partialResponse: String = "",
    val isGenerating: Boolean = false,
    val modelState: ModelManager.ModelState = ModelManager.ModelState.NotLoaded,
    val tokensPerSecond: Float? = null,
    val memoryStatus: com.jegly.offlineLLM.utils.MemoryStatus? = null,
    val showConversationDrawer: Boolean = false,
    val errorMessage: String? = null,
    val speakingMessageId: String? = null,
    val sensitiveDataAccessibilityEnabled: Boolean = false,
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    application: Application,
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository,
    private val modelManager: ModelManager,
    private val inferenceEngine: InferenceEngine,
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    private val memoryMonitor = MemoryMonitor(application)
    private val ttsHelper = TtsHelper(application)

    init {
        setupCollectors()
        memoryMonitor.startMonitoring(viewModelScope)
        viewModelScope.launch {
            memoryMonitor.memoryStatus.collect { status ->
                _uiState.update { it.copy(memoryStatus = status) }
            }
        }

        // Poll settings so the accessibility marking can toggle without restarting the Activity.
        viewModelScope.launch {
            while (true) {
                _uiState.update {
                    it.copy(
                        sensitiveDataAccessibilityEnabled = settingsRepository.sensitiveDataAccessibilityEnabled
                    )
                }
                delay(500)
            }
        }
    }

    private fun setupCollectors() {
        // Collect conversations
        viewModelScope.launch {
            chatRepository.getAllConversations().collect { conversations ->
                _uiState.update { it.copy(conversations = conversations) }
            }
        }

        // Collect model state
        viewModelScope.launch {
            modelManager.modelState.collect { state ->
                _uiState.update { it.copy(modelState = state) }
            }
        }

        // Collect messages for current conversation
        viewModelScope.launch {
            _uiState.map { it.currentConversation?.id }
                .distinctUntilChanged()
                .collectLatest { conversationId ->
                    if (conversationId != null) {
                        chatRepository.getMessagesForConversation(conversationId)
                            .collect { messages ->
                                _uiState.update { it.copy(messages = messages) }
                            }
                    } else {
                        _uiState.update { it.copy(messages = emptyList()) }
                    }
                }
        }
    }

    fun initialize() {
        viewModelScope.launch {
            // Load or create default conversation
            val existing = chatRepository.getMostRecentConversation()
            val conversation = existing ?: chatRepository.createConversation()
            _uiState.update { it.copy(currentConversation = conversation) }

            // Load model
            val modelId = settingsRepository.activeModelId
            if (modelId != -1L) {
                loadModelForConversation(conversation)
            }
        }
    }

    private suspend fun loadModelForConversation(conversation: Conversation) {
        val modelId = settingsRepository.activeModelId
        if (modelId == -1L) return

        val systemPrompt = SystemPrompts.getPrompt(
            settingsRepository.systemPromptKey
        )

        // Get existing messages for context
        val messages = chatRepository.getMessagesSync(conversation.id)
        val history = messages.map { it.role to it.content }

        modelManager.loadModel(
            modelId = modelId,
            systemPrompt = systemPrompt,
            conversationHistory = history,
            onSuccess = {},
            onError = { e ->
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        )
    }

    fun sendMessage(text: String) {
        val sanitized = SecurityUtils.sanitizePrompt(text)
        if (sanitized.isBlank()) return

        val conversation = _uiState.value.currentConversation ?: return

        viewModelScope.launch {
            // Save user message
            chatRepository.addMessage(conversation.id, "user", sanitized)

            // Auto-title on first message
            if (conversation.title.startsWith("Chat ") || conversation.title == "New Chat") {
                val autoTitle = sanitized.take(40).let { if (sanitized.length > 40) "$it..." else it }
                val updated = conversation.copy(title = autoTitle)
                chatRepository.updateConversation(updated)
                _uiState.update { it.copy(currentConversation = updated) }
            }

            _uiState.update {
                it.copy(
                    isGenerating = true,
                    partialResponse = "",
                    tokensPerSecond = null,
                    errorMessage = null,
                )
            }

            val activeModelId = settingsRepository.activeModelId
            val model = chatRepository.getModel(activeModelId)
            
            val query = if (model != null && PromptFormatter.isGemma4(model.name, model.chatTemplate)) {
                // Manually format for Gemma 4 if detected
                val systemPrompt = SystemPrompts.getPrompt(settingsRepository.systemPromptKey)
                val historyMessages = chatRepository.getMessagesSync(conversation.id).dropLast(1)
                val history = historyMessages.map { it.role to it.content }
                PromptFormatter.formatGemma4Prompt(systemPrompt, history, sanitized)
            } else {
                sanitized
            }

            inferenceEngine.generateResponse(
                query = query,
                onToken = { partial ->
                    _uiState.update { it.copy(partialResponse = partial) }
                },
                onComplete = { result ->
                    viewModelScope.launch {
                        chatRepository.addMessage(
                            conversation.id, "assistant", result.response
                        )
                        _uiState.update {
                            it.copy(
                                isGenerating = false,
                                partialResponse = "",
                                tokensPerSecond = result.tokensPerSecond,
                            )
                        }
                    }
                },
                onCancelled = {
                    val partial = _uiState.value.partialResponse
                    if (partial.isNotBlank()) {
                        viewModelScope.launch {
                            chatRepository.addMessage(
                                conversation.id, "assistant", partial
                            )
                        }
                    }
                    _uiState.update {
                        it.copy(isGenerating = false, partialResponse = "")
                    }
                },
                onError = { e ->
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            partialResponse = "",
                            errorMessage = e.message,
                        )
                    }
                }
            )
        }
    }

    fun stopGeneration() {
        inferenceEngine.stopGeneration()
        _uiState.update { it.copy(isGenerating = false) }
    }

    fun newConversation() {
        viewModelScope.launch {
            modelManager.unloadModel()
            val conversation = chatRepository.createConversation(
                title = "Chat ${chatRepository.getConversationCount() + 1}"
            )
            _uiState.update {
                it.copy(
                    currentConversation = conversation,
                    showConversationDrawer = false,
                )
            }
            loadModelForConversation(conversation)
        }
    }

    fun switchConversation(conversation: Conversation) {
        if (conversation.id == _uiState.value.currentConversation?.id) {
            _uiState.update { it.copy(showConversationDrawer = false) }
            return
        }
        viewModelScope.launch {
            modelManager.unloadModel()
            _uiState.update {
                it.copy(
                    currentConversation = conversation,
                    showConversationDrawer = false,
                )
            }
            loadModelForConversation(conversation)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            chatRepository.deleteConversation(conversationId)
            if (_uiState.value.currentConversation?.id == conversationId) {
                val next = chatRepository.getMostRecentConversation()
                    ?: chatRepository.createConversation()
                _uiState.update { it.copy(currentConversation = next) }
                loadModelForConversation(next)
            }
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            chatRepository.deleteMessage(messageId)
        }
    }

    fun renameConversation(conversationId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val conversation = chatRepository.getConversation(conversationId) ?: return@launch
            val updated = conversation.copy(title = newTitle.trim(), updatedAt = System.currentTimeMillis())
            chatRepository.updateConversation(updated)
            if (_uiState.value.currentConversation?.id == conversationId) {
                _uiState.update { it.copy(currentConversation = updated) }
            }
        }
    }

    fun toggleDrawer() {
        _uiState.update { it.copy(showConversationDrawer = !it.showConversationDrawer) }
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    suspend fun exportChats(): String {
        val conversations = _uiState.value.conversations
        val exportedChats = conversations.map { conv ->
            val messages = chatRepository.getMessagesSync(conv.id)
            ExportedChat(
                title = conv.title,
                systemPromptKey = conv.systemPromptKey,
                createdAt = conv.createdAt,
                messages = messages.map { msg ->
                    ExportedMessage(
                        role = msg.role,
                        content = msg.content,
                        timestamp = msg.timestamp,
                    )
                }
            )
        }
        val data = ExportData(chats = exportedChats)
        return Json { prettyPrint = true }.encodeToString(data)
    }

    fun speakMessage(messageId: String, text: String) {
        _uiState.update { it.copy(speakingMessageId = messageId) }
        ttsHelper.speak(text) {
            _uiState.update { it.copy(speakingMessageId = null) }
        }
    }

    fun stopSpeaking() {
        ttsHelper.stop()
        _uiState.update { it.copy(speakingMessageId = null) }
    }



    override fun onCleared() {
        super.onCleared()
        memoryMonitor.stopMonitoring()
        ttsHelper.shutdown()
    }
}
