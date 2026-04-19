<div align="center">

<img src="Screenshots/offlinellm-banner.png" width="600" />

**The first of its kind,A fully offline, private AI chat app for Android**

The only Android LLM app that literally cannot phone home.
All LLM inference runs entirely on-device via llama.cpp.
No internet. No cloud. No tracking. Your conversations stay yours.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.3-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-14%2B-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![llama.cpp](https://img.shields.io/badge/llama.cpp-GGUF-orange.svg)](https://github.com/ggerganov/llama.cpp)
[![Offline](https://img.shields.io/badge/Network-Zero%20Permissions-red.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
![GitHub all releases](https://img.shields.io/github/downloads/jegly/OfflineLLM/total)
</div>

---
If this project helped you, please ⭐️ star it to help others find it 
# Access Your Data Anytime, Anywhere

**OfflineLLM** is designed with users who need reliable, offline access to their AI assistant, especially in scenarios where internet access is limited or unavailable. Whether you're off-grid, in a remote location, or simply need a way to interact with your data without relying on the cloud, **OfflineLLM** provides a solution that works entirely offline.

## Why It's Useful:

- **No Need for Constant Internet**: With **OfflineLLM**, all processing and inference run entirely on-device. You don’t need to worry about internet connectivity to access your AI assistant. Whether you're traveling through areas with poor signal or simply want to preserve your privacy, you have full access to the app's capabilities at all times.
  
- **Complete Data Privacy**: Your conversations and data are never sent to the cloud. **OfflineLLM** ensures that everything stays on your device, making it an ideal choice for users who prioritize privacy and security.

- **Use Anytime, Anywhere**: Even without an internet connection, you can run complex language models on your device. This is particularly useful for people living in areas with unreliable networks or those who prefer to minimize their exposure to online services.

- **Perfect for Off-Grid Living**: If you're off-grid or in remote locations with no data access, **OfflineLLM** ensures you're not left without access to AI-powered tools. The app doesn’t require any data plans or connectivity to operate.

## Features That Make It Stand Out:

- **100% Offline** — No INTERNET permissions required. No need to phone home for processing.
- **On-Device Inference** — Runs all AI models locally with no external calls or data exchanges.
- **Secure and Private** — Your data stays private, with encrypted settings and optional biometric locks.
- **No Cloud, No Tracking** — Access your AI assistant securely with no need for cloud connectivity or tracking, making it perfect for privacy-conscious individuals.

Whether you’re an adventurer, living in an area with limited internet access, or just prefer offline tools, **OfflineLLM** ensures you can always have access to powerful AI wherever you go.

## Screenshots

<p align="center">
<img src="Screenshots/Welcome_to_OfflineLLM.jpg" width="200" />
<img src="Screenshots/Choose_assistant.jpg" width="200" />
<img src="Screenshots/Conversation_preview.jpg" width="200" />
<img src="Screenshots/Settings_preview.jpg" width="200" />
</p>

<p align="center">
<img src="Screenshots/Settings_preview_2.jpg" width="200" />
<img src="Screenshots/Application_about_section.jpg" width="200" />
</p>

---

## Features

- **100% Offline** — No INTERNET permission in the manifest. Cannot phone home.
- **On-Device Inference** — Runs GGUF models via llama.cpp with optimized ARM NEON/SVE/i8mm native libraries
- **Streaming Responses** — Token-by-token output (~25 tok/s on budget devices, 40-60+ on flagships)
- **Import Any Model** — Bring your own GGUF models at runtime via file picker
- **Translator** - 75+ languages now supported !!
- **Multiple Conversations** — Auto-titled from your first message, renameable, searchable
- **Advanced Sampling** — Temperature, Top-P, Top-K, Min-P, Repeat Penalty with explanations
- **Theming** — System/Light/Dark/AMOLED Black + 9 accent colour options
- **System Prompts** — General, Coder, Creative Writer, Tutor, Translator (75+ languages)
- **Markdown Rendering** — Assistant responses render bold, italic, code blocks, and lists
- **Text-to-Speech** — Read AI responses aloud using your device's TTS engine
- **Thinking Tag Stripping** — Hides `<think>` blocks from reasoning models like Qwen
- **Security** — Encrypted settings, optional biometric lock, secure file deletion
- **Chat Backup** — Export/import all conversations as JSON
- **Built-in Help** — Guide for downloading models from HuggingFace
- **Gemma 4** — Supported with automatic prompt template detection
- **RAG** - Persistent memory feature coming soon !
## Recommended Models

| Model | Size | Best For |
|---|---|---|
| Model (Q4_K_M) | Approx. Size | RAM Required / Best For |
| :--- | :--- | :--- |
| **gemma-3-270m-it-qat-Q4_K_M.gguf** | ~300 MB | 2-4GB RAM devices, fast responses |
| **Qwen3.5 0.8B Q4_K_M** | ~530 MB | Good balance for 4-6GB RAM |
| **gemma-4-E2B-it-GGUF** (2.3B effective) | **~1.3 GB** | **Recommended for 6-8GB RAM** |
| **Qwen3.5 4B Q4_K_M** | ~2.5 GB |  8GB+ RAM | **Recommended for 6-8GB RAM** |
| **gemma-4-E4B-it-GGUF** (4.5B effective) | Flagship **~2.5 GB** | **Recommended for 8GB RAM** |
| **Qwen3.5 4B Q4_K_M** | ~2.5 GB | Flagship (12 GB+ RAM),  |

Search for the model name + "GGUF" on [HuggingFace](https://huggingface.co). Choose `Q4_K_M` quantization for best quality/speed balance.

---

## Install

v5.0.0 now ships in three flavours — pick the one that matches your device:

| Release | Bundled Model | APK Size | Best For |
|---|---|---|---|
| **Vanilla** | None (bring your own) | Small | Users with their own GGUF model |
| **Qwen3.5 0.8B** | Qwen3.5 0.8B Q4_K_M | ~600 MB | Everyday use, 4–6 GB RAM |
| **Gemma4-E2B** | Gemma4-E2B-it Q4_K_M | ~1.4 GB | Best quality, 6–8 GB RAM |

All releases are identical in features — the only difference is whether a model comes pre-loaded.

1. Download the APK from [Releases](https://github.com/jegly/OfflineLLM/releases)
2. On your device: **Settings → Apps → Install unknown apps** → allow your file manager
3. Open the APK and tap Install
4. Complete onboarding and import a GGUF model from Settings

Or via ADB:
```bash
adb install OfflineLLM_V5_Variant_of_will.apk
    
    

```
**SHA256SUM:** 84a0b3eb267eb878a2858cab2d5cc972409951aefad46eb6ab20c003162ab016

---

## Build from Source

### Prerequisites

- JDK 17, Android SDK (compileSdk 37), NDK r27, CMake 3.22.1

```bash
git clone --recurse-submodules https://github.com/jegly/OfflineLLM.git
cd OfflineLLM

# Optional: bundle a model in the APK
cp /path/to/model.gguf app/src/main/assets/model/

# Build
./gradlew assembleDebug
```

First build compiles llama.cpp from source (~15-20 min). Subsequent builds are fast.

---

## Architecture

```
OfflineLLM/
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
│       └── utils/       ← BiometricHelper, MemoryMonitor, SecurityUtils, TTS
└── llama.cpp/           ← Git submodule
```

---

## Performance

| Device Tier | RAM | Expected Speed |
|---|---|---|
| Budget (ZTE, etc.) | 4 GB | ~25 tok/s with 270M model |
| Mid-range (Pixel 7) | 6-8 GB | 30-50 tok/s with 1B model |
| Flagship (Pixel 10 Pro) | 12-16 GB | 40-60+ tok/s with 4B model |

---

## Sampling Parameters

OfflineLLM gives you full control over how the model generates text:

| Parameter | Default | What It Does |
|---|---|---|
| Temperature | 0.7 | Controls randomness. Lower = focused. Higher = creative. |
| Top-P | 0.9 | Nucleus sampling. Only considers tokens above this cumulative probability. |
| Top-K | 40 | Limits selection to the K most likely tokens. |
| Min-P | 0.1 | Filters tokens below this fraction of the top token's probability. |
| Repeat Penalty | 1.1 | Penalises repeated tokens. 1.0 = no penalty. |
| Context Size | 4096 | How many tokens of conversation history the model can see. |

---

## Security & Privacy

- **Zero network permissions** — no INTERNET, no ACCESS_NETWORK_STATE
- **No Google Play Services** or Firebase dependencies
- **Encrypted settings** via Jetpack Security
- **Optional biometric lock**
- **Memory Tagging Extension** enabled (`memtagMode="sync"`)
- **Secure deletion** — files overwritten before removal
- **No logging** of prompts or responses

---

## License

Apache License 2.0

llama.cpp backend: MIT License. Native wrapper adapted from [SmolChat-Android](https://github.com/shubham0204/SmolChat-Android) (Apache 2.0).

---

<div align="center">

**[www.jegly.xyz](https://www.jegly.xyz)**

</div>
