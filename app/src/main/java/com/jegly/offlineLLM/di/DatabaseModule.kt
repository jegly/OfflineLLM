package com.jegly.offlineLLM.di

import android.content.Context
import androidx.room.Room
import com.jegly.offlineLLM.data.local.ChatDatabase
import com.jegly.offlineLLM.data.local.dao.ConversationDao
import com.jegly.offlineLLM.data.local.dao.MessageDao
import com.jegly.offlineLLM.data.local.dao.ModelDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideChatDatabase(@ApplicationContext context: Context): ChatDatabase {
        return Room.databaseBuilder(
            context,
            ChatDatabase::class.java,
            "offlinellm_database"
        ).build()
    }

    @Provides
    fun provideConversationDao(db: ChatDatabase): ConversationDao = db.conversationDao()

    @Provides
    fun provideMessageDao(db: ChatDatabase): MessageDao = db.messageDao()

    @Provides
    fun provideModelDao(db: ChatDatabase): ModelDao = db.modelDao()
}
