package com.jegly.offlineLLM.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ModelInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val path: String,
    val sizeBytes: Long = 0,
    val contextSize: Int = 2048,
    val chatTemplate: String = "",
    val addedAt: Long = System.currentTimeMillis(),
    val isBundled: Boolean = false
)
