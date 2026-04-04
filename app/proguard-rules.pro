# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Room entities
-keep class com.jegly.offlineLLM.data.local.entities.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep SmolLM native bridge
-keep class com.jegly.offlineLLM.smollm.** { *; }

# Keep serialization
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }

# Tink crypto (used by EncryptedSharedPreferences)
-dontwarn com.google.errorprone.annotations.**
-dontwarn javax.annotation.**
-dontwarn com.google.auto.value.**
-keep class com.google.crypto.tink.** { *; }

# Google API client (transitive from Tink)
-dontwarn com.google.api.client.**
-dontwarn org.joda.time.**
-dontwarn javax.annotation.**
-dontwarn com.google.auto.value.**
