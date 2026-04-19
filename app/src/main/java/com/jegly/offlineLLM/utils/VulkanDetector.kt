package com.jegly.offlineLLM.utils

import android.content.Context
import android.content.pm.PackageManager

object VulkanDetector {
    data class VulkanSupport(val supported: Boolean, val version: String?)

    fun check(context: Context): VulkanSupport {
        val pm = context.packageManager
        val supported = pm.hasSystemFeature(PackageManager.FEATURE_VULKAN_HARDWARE_LEVEL)
        if (!supported) return VulkanSupport(false, null)

        val versionFeature = pm.systemAvailableFeatures
            .firstOrNull { it.name == "android.hardware.vulkan.version" }
        val version = versionFeature?.version?.let { v ->
            val major = (v shr 22) and 0x3FF
            val minor = (v shr 12) and 0x3FF
            val patch = v and 0xFFF
            "$major.$minor.$patch"
        }
        return VulkanSupport(true, version)
    }
}
