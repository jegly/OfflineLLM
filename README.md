# offlineLLM

A fully offline, private AI chat assistant for Android. All inference runs on-device using [llama.cpp](https://github.com/ggerganov/llama.cpp). Zero cloud dependency, zero tracking, zero network permissions.

## Features

- **100% Offline** — No INTERNET permission. No network calls. Ever.
- **On-Device LLM Inference** — Runs GGUF models via llama.cpp with ARM NEON/SVE/i8mm optimization
- **Streaming Responses** — Token-by-token output with real-time display
- **Multiple Conversations** — Create, switch, and delete chat threads
- **GGUF Model Import** — Bring your own models at runtime via file picker
- **Encrypted Storage** — Settings stored with EncryptedSharedPreferences
- **Biometric Lock** — Optional fingerprint/face gate on app open
- **Material 3 Dynamic Color** — Adapts to your system theme
- **Chat Export/Import** — JSON backup and restore of all conversations
- **Memory Monitoring** — Real-time RAM usage tracking with auto-warnings

## Supported Models

Any GGUF-format model will work. Recommended models for mobile:

| Model | Quantized Size | Filename | Notes |
|---|---|---|---|
| Gemma 3 270M | ~300 MB | `gemma-3-270m-it-q4_k_m.gguf` | Fastest, low RAM |
| **Gemma 3 1B** | **~750 MB** | **`gemma-3-1b-it-q4_k_m.gguf`** | **Recommended balance** |
| Qwen3.5-0.8B | ~530 MB | `qwen3.5-0.8b-q4_k_m.gguf` | Good alternative |
| Gemma 4 1B | ~750 MB | `gemma-4-1b-it-q4_k_m.gguf` | Latest, if available |

Download from [HuggingFace](https://huggingface.co) — search for the model name + "GGUF Q4_K_M".

## Model Setup

### Option A — Bundle in APK (larger APK, instant first launch)

1. Download your chosen `.gguf` file
2. Place it in `app/src/main/assets/model/`
3. Build the APK — the model will be copied to internal storage on first launch

### Option B — Lean APK + Runtime Import (small APK, user imports model)

1. Build without any file in `assets/model/`
2. On first launch, the onboarding will note no bundled model was found
3. Go to **Settings → Import GGUF Model** and select the `.gguf` file from your device

## Build Instructions

### Prerequisites

- **JDK 17** (not 21 — Gradle 8.7 requires JDK 17)
- **Android SDK** with Compile SDK 36 and Build Tools 36.0.0
- **Android NDK r27** (`27.2.12479018`) — install via SDK Manager
- **CMake 3.22.1** — install via SDK Manager
- **Git** with submodule support

### Setup

```bash
# Clone the repo
git clone https://github.com/your-repo/offlineLLM.git
cd offlineLLM

# Initialize llama.cpp submodule
git submodule add https://github.com/ggerganov/llama.cpp.git
# Or if already defined:
git submodule update --init --recursive

# (Optional) Place a model in assets
mkdir -p app/src/main/assets/model
cp /path/to/gemma-3-1b-it-q4_k_m.gguf app/src/main/assets/model/
```

### Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (unsigned)
./gradlew assembleRelease

# APK locations:
# Debug:   app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release-unsigned.apk
```

## NDK Setup

The project uses CMake to build llama.cpp natively. The `smollm` module's `build.gradle.kts` specifies:

```
ndkVersion = "27.2.12479018"
```

Install via Android Studio: **Settings → SDK Manager → SDK Tools → NDK (Side by side)** and select version `27.2.12479018`.

The CMake build automatically compiles **multiple `.so` variants** optimized for different ARM instruction sets:
- `smollm` — Universal fallback
- `smollm_v8` — ARMv8-A baseline
- `smollm_v8_2_fp16` — ARMv8.2 with FP16
- `smollm_v8_2_fp16_dotprod` — ARMv8.2 with FP16 + DotProd
- `smollm_v8_4_fp16_dotprod` — ARMv8.4 with FP16 + DotProd
- `smollm_v8_4_fp16_dotprod_sve` — + SVE
- `smollm_v8_4_fp16_dotprod_i8mm` — + I8MM
- `smollm_v8_4_fp16_dotprod_i8mm_sve` — + I8MM + SVE

The app auto-detects CPU features at runtime and loads the optimal library.

## Release Signing

### Generate a keystore

```bash
keytool -genkey -v \
  -keystore offlinellm-release.jks \
  -keyalg RSA -keysize 2048 \
  -validity 10000 \
  -alias offlinellm
```

### Sign the APK

```bash
# Build unsigned release
./gradlew assembleRelease

# Sign with apksigner
apksigner sign \
  --ks offlinellm-release.jks \
  --ks-key-alias offlinellm \
  --out app-release-signed.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk

# Verify
apksigner verify --verbose app-release-signed.apk
```

### Using Gradle signing (alternative)

Add to `app/build.gradle.kts`:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../offlinellm-release.jks")
        storePassword = System.getenv("KEYSTORE_PASSWORD")
        keyAlias = "offlinellm"
        keyPassword = System.getenv("KEY_PASSWORD")
    }
}
buildTypes {
    getByName("release") {
        signingConfig = signingConfigs.getByName("release")
    }
}
```

## Sideloading

### Enable Unknown Sources

**Android 12+:** Settings → Apps → Special app access → Install unknown apps → (your file manager) → Allow

### Install via ADB

```bash
adb install app-release-signed.apk

# If updating:
adb install -r app-release-signed.apk
```

### Install via File Manager

1. Transfer the signed APK to your device
2. Open it in a file manager
3. Tap "Install" and confirm

## Importing GGUF Models

1. Download a `.gguf` model file to your device (via PC transfer, browser, etc.)
2. Open offlineLLM → **Settings → Import GGUF Model**
3. Select the `.gguf` file from the file picker
4. The app validates the GGUF magic bytes, copies it to internal storage, and reads metadata
5. The model appears in the model list — tap "Use" to activate it

## Performance by Device Tier

| Tier | RAM | Example Devices | Recommended Model | Expected Speed |
|---|---|---|---|---|
| Low-end | 4 GB | Budget phones, older flagships | Gemma 3 270M | 15-25 tok/s |
| Mid-range | 6-8 GB | Pixel 7, Galaxy S23 | Gemma 3 1B | 30-50 tok/s |
| Flagship | 12+ GB | Pixel 8 Pro, S24 Ultra | Gemma 3 1B or larger | 40-60+ tok/s |

**Tips for low-RAM devices:**
- Use a smaller model (270M)
- Reduce context size to 2048
- Close other apps before using offlineLLM
- The app monitors memory and warns at 85% usage

## Swapping the Default Model

To change the bundled model at build time:

1. Remove the old model from `app/src/main/assets/model/`
2. Place the new `.gguf` file in `app/src/main/assets/model/`
3. Rebuild the APK

Only one model should be in the assets folder. The app picks the first `.gguf` file it finds.

## Architecture

```
offlineLLM/
├── smollm/              ← Native llama.cpp JNI module
│   └── src/main/
│       ├── cpp/         ← C++ inference engine + JNI bridge
│       └── java/        ← SmolLM.kt, GGUFReader.kt wrappers
├── app/                 ← Main Android application
│   └── src/main/java/com/jegly/offlineLLM/
│       ├── ai/          ← InferenceEngine, ModelManager, SystemPrompts
│       ├── data/        ← Room database, DAOs, repositories
│       ├── di/          ← Hilt dependency injection modules
│       ├── ui/          ← Compose screens, components, theme, navigation
│       └── utils/       ← BiometricHelper, MemoryMonitor, SecurityUtils
├── llama.cpp/           ← Git submodule (not checked in)
└── gradle/              ← Version catalog, wrapper
```

## Known Limitations

- **Context size vs RAM:** Larger context sizes (8192) require more RAM. On 4 GB devices, stick to 2048.
- **First token latency:** The first response in a conversation may take 1-3s as the model processes the full context.
- **x86/x86_64:** The native build targets ARM (arm64-v8a). x86 Android devices are not supported.
- **Emulator:** The app loads a generic (non-optimized) library on emulators. Performance will be poor.
- **Model size limits:** APK size is limited by the model you bundle. For models >1GB, use the runtime import feature.
- **No GPU acceleration:** Currently CPU-only inference. Vulkan GPU support may be added in a future version.
- **`memtagMode="sync"`:** Memory Tagging Extension only works on ARMv9 devices (Pixel 8+). Silently ignored on older hardware.
- **Chat template:** The app reads the chat template from the GGUF metadata. If the model lacks one, a default ChatML template is used. Some models may need a custom template configured in Settings.

## Security

- No INTERNET permission — verified in merged manifest
- No Google Play Services or Firebase dependencies
- All data in sandboxed `/data/data/com.jegly.offlineLLM/`
- Settings encrypted via Jetpack Security EncryptedSharedPreferences
- Optional biometric lock (BiometricPrompt)
- Memory Tagging Extension enabled (`memtagMode="sync"`)
- Secure file deletion (overwrite before delete)
- Input sanitization before inference
- No sensitive data logged to Logcat

## License

This project uses llama.cpp (MIT License) for the inference backend.
The SmolLM native wrapper is adapted from [SmolChat-Android](https://github.com/nicholasgasior/SmolChat-Android) (Apache 2.0).
