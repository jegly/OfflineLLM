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
                        instance.addChatMessage(role, content)
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
                    var fullResponse = ""
                    var stopRequested = false

                    val duration = measureTime {
                        // Use collect but check for stop sequences to break manually
                        instance.getResponseAsFlow(query).collect { piece ->
                            if (stopRequested) return@collect
                            
                            fullResponse += piece
                            
                            // Check if we hit a stop sequence
                            if (containsStopSequence(fullResponse)) {
                                stopRequested = true
                                // We don't cancel the job, we just stop collecting pieces
                                return@collect
                            }

                            // Emission logic: don't emit if we're currently typing a potential tag
                            val displayResponse = cleanModelOutput(fullResponse, isFinal = false)
                            if (displayResponse.isNotBlank() || fullResponse.isEmpty()) {
                                withContext(Dispatchers.Main) {
                                    onToken(displayResponse)
                                }
                            }
                        }
                    }

                    // Final cleanup
                    val finalResponse = cleanModelOutput(fullResponse, isFinal = true).let {
                        if (it.isBlank()) {
                            if (fullResponse.isNotBlank()) "(No visible content produced)" else "(Empty response)"
                        } else it
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

    private fun containsStopSequence(text: String): Boolean {
        val stops = listOf("<turn|", "<|turn_end|>", "<turn_end|>", "<start_of_turn>", "<end_of_turn>")
        return stops.any { text.contains(it, ignoreCase = true) }
    }

    private fun cleanModelOutput(raw: String, isFinal: Boolean): String {
        var cleaned = raw
            .replace(Regex("<think>.*?</think>", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<think>.*", RegexOption.DOT_MATCHES_ALL), "")
            .replace(Regex("<turn\\|.*?\\|>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<\\|turn_end\\|>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<turn_end\\|>", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<start_of_turn>.*?\\n", RegexOption.IGNORE_CASE), "")
            .replace(Regex("<end_of_turn>", RegexOption.IGNORE_CASE), "")
            .replace("System instruction:", "")

        if (!isFinal) {
            // Buffer to prevent tag fragments from flickering
            val potentialTagStart = listOf("<turn", "<|turn", "<|", "<start", "<")
            for (p in potentialTagStart) {
                if (cleaned.endsWith(p, ignoreCase = true)) {
                    return cleaned.substring(0, cleaned.length - p.length).trim()
                }
            }
        } else {
            // Final pass to remove any trailing tag fragments
            cleaned = cleaned.replace(Regex("<.*$", RegexOption.IGNORE_CASE), "")
        }

        return cleaned.trim()
    }

    fun stopGeneration() {
        stateLock.withLock {
            generationJob?.cancel()
            isGenerating = false
        }
    }

    fun isReady(): Boolean = isModelLoaded.get() && !isGenerating
}
