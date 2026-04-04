package com.jegly.offlineLLM.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jegly.offlineLLM.data.local.dao.ConversationDao
import com.jegly.offlineLLM.data.local.dao.MessageDao
import com.jegly.offlineLLM.data.local.dao.ModelDao
import com.jegly.offlineLLM.data.local.entities.Conversation
import com.jegly.offlineLLM.data.local.entities.Message
import com.jegly.offlineLLM.data.local.entities.ModelInfo

@Database(
    entities = [Conversation::class, Message::class, ModelInfo::class],
    version = 1,
    exportSchema = false
)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun modelDao(): ModelDao
}
