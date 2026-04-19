# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Room entities and DAOs
-keep class com.jegly.offlineLLM.data.local.entities.** { *; }
-keep class com.jegly.offlineLLM.data.local.dao.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# Keep SmolLM native bridge
-keep class com.jegly.offlineLLM.smollm.** { *; }

# Keep serialization
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep data classes used for JSON export/import
-keep class com.jegly.offlineLLM.data.repository.ExportData { *; }
-keep class com.jegly.offlineLLM.data.repository.ExportedChat { *; }
-keep class com.jegly.offlineLLM.data.repository.ExportedMessage { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# Kotlin
-dontwarn kotlin.**
-keepattributes SourceFile,LineNumberTable

# Navigation
-keep class androidx.navigation.** { *; }

# Compose — keep lambdas and composable function metadata
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Markdown renderer
-keep class com.mikepenz.markdown.** { *; }
-dontwarn com.mikepenz.markdown.**

# Tink crypto (used by EncryptedSharedPreferences)
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.api.client.**
-dontwarn javax.annotation.**
-dontwarn com.google.auto.value.**
-dontwarn org.joda.time.**
