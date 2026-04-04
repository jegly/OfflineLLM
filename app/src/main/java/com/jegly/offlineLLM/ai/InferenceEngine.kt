package com.jegly.offlineLLM.ai

import com.jegly.offlineLLM.smollm.SmolLM
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.measureTime

class InferenceEngine {

    private val instance = SmolLM()
    private val stateLock = ReentrantLock()

    @Volatile
    private var generationJob: Job? = null

    @Volatile
    private var loadJob: Job? = null

    val isModelLoaded = AtomicBoolean(false)

    @Volatile
    var isGenerating = false
        private set

    data class GenerationResult(
        val response: String,
        val tokensPerSecond: Float,
        val durationSeconds: Int,
        val contextLengthUsed: Int,
    )

    fun loadModel(
        modelPath: String,
        params: SmolLM.InferenceParams = SmolLM.InferenceParams(),
        systemPrompt: String = "",
        conversationHistory: List<Pair<String, String>> = emptyList(),
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        stateLock.withLock {
            loadJob?.cancel()

            loadJob = CoroutineScope(Dispatchers.Default).launch {
                try {
                    instance.load(modelPath, params)

                    if (systemPrompt.isNotBlank()) {
                        instance.addSystemPrompt(systemPrompt)
                    }

                    for ((role, content) in conversationHistory) {
                        when (role) {
                            "user" -> instance.addUserMessage(content)
                            "assistant" -> instance.addAssistantMessage(content)
                        }
                    }

                    withContext(Dispatchers.Main) {
                        isModelLoaded.set(true)
                        onSuccess()
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onError(e)
                    }
                }
            }
        }
    }

    fun unloadModel() {
        stateLock.withLock {
            generationJob?.cancel()
            loadJob?.cancel()
            isModelLoaded.set(false)
            isGenerating = false
            try {
                instance.close()
            } catch (_: Exception) {}
        }
    }

    fun generateResponse(
        query: String,
        onToken: (String) -> Unit,
        onComplete: (GenerationResult) -> Unit,
        onCancelled: () -> Unit,
        onError: (Exception) -> Unit,
    ) {
        stateLock.withLock {
            if (!isModelLoaded.get()) {
                onError(IllegalStateException("Model not loaded"))
                return
            }

            generationJob?.cancel()

            generationJob = CoroutineScope(Dispatchers.Default).launch {
                try {
                    isGenerating = true
                    var response = ""

                    val duration = measureTime {
                        instance.getResponseAsFlow(query).collect { piece ->
                            response += piece
                            // Strip think tags from streaming display
                            val cleanPartial = response
                                .replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "")
                                .replace(Regex("<think>.*", RegexOption.DOT_MATCHES_ALL), "")
                                .trim()
                            withContext(Dispatchers.Main) {
                                onToken(cleanPartial)
                            }
                        }
                    }

                    // Strip think tags from final response
                    val cleanResponse = response
                        .replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "")
                        .replace(Regex("<think>.*", RegexOption.DOT_MATCHES_ALL), "")
                        .trim()

                    // Fix empty response: if model returned nothing useful, provide fallback
                    val finalResponse = if (cleanResponse.isBlank()) {
                        if (response.isNotBlank()) {
                            // Had content but it was all thinking tags
                            "(The model only produced internal reasoning with no visible response. Try rephrasing your question.)"
                        } else {
                            "(Empty response from model. The model may need a different prompt or temperature setting.)"
                        }
                    } else {
                        cleanResponse
                    }

                    withContext(Dispatchers.Main) {
                        isGenerating = false
                        onComplete(
                            GenerationResult(
                                response = finalResponse,
                                tokensPerSecond = instance.getResponseGenerationSpeed(),
                                durationSeconds = duration.inWholeSeconds.toInt(),
                                contextLengthUsed = instance.getContextLengthUsed(),
                            )
                        )
                    }
                } catch (e: CancellationException) {
                    isGenerating = false
                    withContext(Dispatchers.Main) { onCancelled() }
                } catch (e: Exception) {
                    isGenerating = false
                    withContext(Dispatchers.Main) { onError(e) }
                }
            }
        }
    }

    fun stopGeneration() {
        stateLock.withLock {
            generationJob?.cancel()
            isGenerating = false
        }
    }

    fun isReady(): Boolean = isModelLoaded.get() && !isGenerating
}
