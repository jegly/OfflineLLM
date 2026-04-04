package com.jegly.offlineLLM.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jegly.offlineLLM.data.local.entities.ModelInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface ModelDao {

    @Query("SELECT * FROM models ORDER BY addedAt DESC")
    fun getAllModels(): Flow<List<ModelInfo>>

    @Query("SELECT * FROM models ORDER BY addedAt DESC")
    suspend fun getAllModelsSync(): List<ModelInfo>

    @Query("SELECT * FROM models WHERE id = :id")
    suspend fun getModel(id: Long): ModelInfo?

    @Query("SELECT * FROM models WHERE path = :path LIMIT 1")
    suspend fun getModelByPath(path: String): ModelInfo?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: ModelInfo): Long

    @Query("DELETE FROM models WHERE id = :id")
    suspend fun delete(id: Long)
}
