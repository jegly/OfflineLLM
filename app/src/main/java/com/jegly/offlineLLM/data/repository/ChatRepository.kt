package com.jegly.offlineLLM.data.repository

import com.jegly.offlineLLM.data.local.dao.ConversationDao
import com.jegly.offlineLLM.data.local.dao.MessageDao
import com.jegly.offlineLLM.data.local.dao.ModelDao
import com.jegly.offlineLLM.data.local.entities.Conversation
import com.jegly.offlineLLM.data.local.entities.Message
import com.jegly.offlineLLM.data.local.entities.ModelInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ExportedChat(
    val title: String,
    val systemPromptKey: String,
    val createdAt: Long,
    val messages: List<ExportedMessage>
)

@Serializable
data class ExportedMessage(
    val role: String,
    val content: String,
    val timestamp: Long
)

@Serializable
data class ExportData(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val chats: List<ExportedChat>
)

@Singleton
class ChatRepository @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: MessageDao,
    private val modelDao: ModelDao,
) {
    // Conversations
    fun getAllConversations(): Flow<List<Conversation>> = conversationDao.getAllConversations()

    suspend fun getConversation(id: String): Conversation? = conversationDao.getConversation(id)

    suspend fun getMostRecentConversation(): Conversation? = conversationDao.getMostRecent()

    suspend fun createConversation(title: String = "New Chat", systemPromptKey: String = "default"): Conversation {
        val conversation = Conversation(title = title, systemPromptKey = systemPromptKey)
        conversationDao.insert(conversation)
        return conversation
    }

    suspend fun updateConversation(conversation: Conversation) = conversationDao.update(conversation)

    suspend fun deleteConversation(id: String) = conversationDao.delete(id)

    suspend fun deleteAllConversations() {
        messageDao.deleteAll()
        conversationDao.deleteAll()
    }

    suspend fun getConversationCount(): Int = conversationDao.getCount()

    fun searchConversations(query: String): Flow<List<Conversation>> = conversationDao.search(query)

    // Messages
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun getMessagesSync(conversationId: String): List<Message> =
        messageDao.getMessagesForConversationSync(conversationId)

    suspend fun addMessage(conversationId: String, role: String, content: String, tokenCount: Int = 0): Message {
        val message = Message(
            conversationId = conversationId,
            role = role,
            content = content,
            tokenCount = tokenCount
        )
        messageDao.insert(message)
        conversationDao.incrementMessageCount(conversationId)
        return message
    }

    suspend fun deleteMessage(id: String) = messageDao.delete(id)

    suspend fun deleteAllMessagesForConversation(conversationId: String) =
        messageDao.deleteAllForConversation(conversationId)

    fun searchMessages(query: String): Flow<List<Message>> = messageDao.search(query)

    // Models Meow
    fun getAllModels(): Flow<List<ModelInfo>> = modelDao.getAllModels()

    suspend fun getAllModelsSync(): List<ModelInfo> = modelDao.getAllModelsSync()

    suspend fun getModel(id: Long): ModelInfo? = modelDao.getModel(id)

    suspend fun getModelByPath(path: String): ModelInfo? = modelDao.getModelByPath(path)

    suspend fun addModel(model: ModelInfo): Long = modelDao.insert(model)

    suspend fun deleteModel(id: Long) = modelDao.delete(id)

    // Export / Import Meow
    suspend fun exportChatsToJson(): String {
        val json = Json { prettyPrint = true }
        val conversations = conversationDao.getAllConversations() // we need sync version
        val allConvs = mutableListOf<Conversation>()
        // Use a simple collect approach
        val count = conversationDao.getCount()
        if (count == 0) return json.encodeToString(ExportData(chats = emptyList()))

        val exportedChats = mutableListOf<ExportedChat>()
        // We need sync access for export, get all conversations
        val convList = getMostRecentConversation()?.let { recent ->
            // Workaround: get all messages for each conversation
            listOf(recent) // This will be handled properly in the ViewModel
        } ?: emptyList()

        return json.encodeToString(ExportData(chats = exportedChats))
    }

    suspend fun exportAllChats(): String {
        val json = Json { prettyPrint = true }
        val exportedChats = mutableListOf<ExportedChat>()

        // We can't easily collect Flow here, so we use a different approach
        // The ViewModel will handle the full export logic Meow
        return json.encodeToString(ExportData(chats = exportedChats))
    }

    suspend fun importChatsFromJson(jsonString: String): Result<Int> {
        return try {
            val json = Json { ignoreUnknownKeys = true }
            val data = json.decodeFromString<ExportData>(jsonString)
            var count = 0
            for (chat in data.chats) {
                val conversation = Conversation(
                    title = chat.title,
                    systemPromptKey = chat.systemPromptKey,
                    createdAt = chat.createdAt,
                    updatedAt = System.currentTimeMillis(),
                    messageCount = chat.messages.size
                )
                conversationDao.insert(conversation)
                for (msg in chat.messages) {
                    messageDao.insert(
                        Message(
                            conversationId = conversation.id,
                            role = msg.role,
                            content = msg.content,
                            timestamp = msg.timestamp
                        )
                    )
                }
                count++
            }
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
