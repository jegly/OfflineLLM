package com.jegly.offlineLLM.di

import android.content.Context
import com.jegly.offlineLLM.ai.InferenceEngine
import com.jegly.offlineLLM.ai.ModelManager
import com.jegly.offlineLLM.data.repository.ChatRepository
import com.jegly.offlineLLM.data.repository.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object InferenceModule {

    @Provides
    @Singleton
    fun provideInferenceEngine(): InferenceEngine = InferenceEngine()

    @Provides
    @Singleton
    fun provideModelManager(
        @ApplicationContext context: Context,
        chatRepository: ChatRepository,
        settingsRepository: SettingsRepository,
        inferenceEngine: InferenceEngine,
    ): ModelManager = ModelManager(context, chatRepository, settingsRepository, inferenceEngine)
}
