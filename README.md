<div align="center">
  
<div align="center">
<img src="Screenshots/offlinellm-banner.png" width="600" />
</div>

**A fully offline, private AI chat app for Android**

The only Android LLM app that literally cannot phone home.
All LLM inference runs entirely on-device via llama.cpp.
No internet. No cloud. No tracking. Your conversations stay yours.

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-14%2B-green.svg)]()
[![Offline](https://img.shields.io/badge/Network-Zero%20Permissions-red.svg)]()
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF.svg?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-14%2B-3DDC84.svg?logo=android&logoColor=white)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![llama.cpp](https://img.shields.io/badge/llama.cpp-GGUF-orange.svg)](https://github.com/ggerganov/llama.cpp)
[![Offline](https://img.shields.io/badge/Network-Zero%20Permissions-red.svg)]()
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4.svg?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)

</div>

---

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


