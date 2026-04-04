package com.jegly.offlineLLM.utils

import android.app.ActivityManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.pow

data class MemoryStatus(
    val usedMemoryGb: Float,
    val totalMemoryGb: Float,
    val usagePercent: Float,
    val isWarning: Boolean,   // > 85%
    val isCritical: Boolean,  // > 95%
)

class MemoryMonitor(context: Context) {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    private val _memoryStatus = MutableStateFlow(getCurrentStatus())
    val memoryStatus: StateFlow<MemoryStatus> = _memoryStatus

    private var monitoring = false

    fun startMonitoring(scope: CoroutineScope, intervalMs: Long = 3000L) {
        if (monitoring) return
        monitoring = true
        scope.launch(Dispatchers.Default) {
            while (monitoring) {
                _memoryStatus.value = getCurrentStatus()
                delay(intervalMs)
            }
        }
    }

    fun stopMonitoring() {
        monitoring = false
    }

    fun getCurrentStatus(): MemoryStatus {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalGb = (memInfo.totalMem / 1024.0.pow(3.0)).toFloat()
        val availGb = (memInfo.availMem / 1024.0.pow(3.0)).toFloat()
        val usedGb = totalGb - availGb
        val usagePercent = if (totalGb > 0f) (usedGb / totalGb) * 100f else 0f

        return MemoryStatus(
            usedMemoryGb = usedGb,
            totalMemoryGb = totalGb,
            usagePercent = usagePercent,
            isWarning = usagePercent >= 85f,
            isCritical = usagePercent >= 95f,
        )
    }
}
