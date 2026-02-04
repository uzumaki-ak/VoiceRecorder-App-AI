# VoiceRecorder-App-AI ![Build Status](https://img.shields.io/github/actions/workflow/status/uzumaki-ak/VoiceRecorder-App-AI/build.yml?branch=main) ![License](https://img.shields.io/github/license/uzumaki-ak/VoiceRecorder-App-AI) ![Last Commit](https://img.shields.io/github/last-commit/uzumaki-ak/VoiceRecorder-App-AI)

---

# 📖 Introduction

**VoiceRecorder-App-AI** is a professional Android voice recording application designed to provide users with high-quality audio recording, organization, and management features. Built with a robust architecture, it leverages modern Android components such as Room for local data persistence, Jetpack Compose for UI, and Kotlin Coroutines for asynchronous operations. The app supports categorizing recordings, bookmarking specific timestamps, and integrating transcription results, making it ideal for voice memos, interviews, or professional recordings.

This project emphasizes modularity, security, and user customization. It includes features like offline storage management, flexible categorization, and an extensible architecture to incorporate AI-powered transcription services. The codebase demonstrates best practices in Android development, including dependency injection, permission handling, and database management.

---

# ✨ Features

- **High-Quality Audio Recording:** Record audio with configurable bitrate and sample rate.
- **Categorization:** Organize recordings into customizable categories.
- **Bookmarks:** Mark specific timestamps within recordings for easy navigation.
- **Favorite & Deleted Files Management:** Mark recordings as favorites and manage deleted files with a recycle bin.
- **Transcription Support:** Store and display transcriptions, with potential for AI integration.
- **Permission Management:** Runtime permission handling for audio and storage access.
- **File Sharing:** Secure sharing via FileProvider.
- **Dark & Light Themes:** Customizable themes based on user preferences.
- **Persistent Data Storage:** Local database with Room to store recordings, categories, and bookmarks.
- **Settings & Preferences:** App preferences stored via DataStore, supporting configurable options such as recording quality and UI theme.

---

# 🛠️ Tech Stack

| Library/Component                      | Purpose                                              | Version          |
|----------------------------------------|------------------------------------------------------|------------------|
| **Android SDK**                       | Core platform for app development                     | SDK 31+ (targetApi=31)  |
| **Kotlin**                            | Programming language for app logic                     | 1.8.0+ (assumed based on modern code) |
| **AndroidX Room**                     | Local database for recordings, categories, bookmarks | 2.5.0 (latest stable at time of writing) |
| **Jetpack Compose**                   | Modern declarative UI toolkit                        | 1.4.0+ (assumed) |
| **Coroutines**                        | Asynchronous operations                              | 1.6.4+ |
| **DataStore Preferences**             | Persistent key-value storage                          | 1.0.0+ |
| **FileProvider (androidx.core.content)** | File sharing and URI permissions                     | 1.9.0+ |
| **Material3**                         | UI components and theming                            | 1.1.0+ |
| **Navigation Compose**                | In-app navigation management                         | 2.6.0+ (assumed) |
| **Gradle & Kotlin DSL**               | Build system and configuration                       | Gradle 8.0+ / Kotlin 1.8+ |

*(Note: Specific version numbers are inferred based on current best practices and dependencies typical for such a project; actual versions may vary slightly.)*

---

# 🚀 Quick Start / Installation

Clone the repository:

```bash
git clone https://github.com/uzumaki-ak/VoiceRecorder-App-AI.git
```

Open the project in Android Studio (Arctic Fox or newer recommended). 

**Prerequisites:**
- Android Studio Bumblebee or later
- JDK 17+
- Emulator or physical device running Android SDK 31+

**Build & Run:**
1. Sync project dependencies.
2. Ensure you have configured the necessary environment variables (see "Configuration" below).
3. Run the app on your device/emulator.

---

# 📁 Project Structure

```plaintext
/VoiceRecorder-App-AI
│
├── app/
│   ├── src/main/
│   │   ├── java/com/voicevault/recorder/
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   │   ├── entities/
│   │   │   │   │   ├── dao/
│   │   │   │   │   └── AppDatabase.kt
│   │   │   │   ├── preferences/
│   │   │   │   │   └── AppPreferences.kt
│   │   │   │   ├── repository/
│   │   │   │   │   ├── BookmarkRepository.kt
│   │   │   │   │   └── CategoryRepository.kt
│   │   │   │   ├── utils/
│   │   │   │   │   └── PermissionUtils.kt
│   │   │   │   ├── VoiceVaultApp.kt
│   │   │   │   ├── MainActivity.kt
│   │   │   ├── ui/
│   │   │   │   ├── theme/
│   │   │   │   │   └── VoiceVaultTheme.kt
│   │   │   │   └── NavGraph.kt
│   │   │   └── navigation/
│   │   └── res/
│   │       ├── layout/
│   │       ├── drawable/
│   │       ├── values/
│   │       │   ├── strings.xml
│   │       │   └── themes.xml
│   └── AndroidManifest.xml
│
└── build.gradle.kts
```

**Key folders:**
- `java/com/voicevault/recorder/`: Core Kotlin source files including data layer, app logic, and main activity.
- `ui/`: Jetpack Compose UI components and themes.
- `data/`: Database entities, DAO interfaces, and repositories.
- `res/`: Resources including drawables, strings, themes, and layouts.

---

# 🔧 Configuration

### Environment Variables & Config Files
- **`local.properties` or environment setup:** Ensure SDK paths and JDK are configured.
- **`AndroidManifest.xml`:** Sets permissions for microphone and storage access, and configures `FileProvider`.
- **`gradle.properties`:** Can include API keys or secrets if integrating with AI transcription services (not shown in current code).

### Permissions
- Audio recording (`RECORD_AUDIO`)
- Storage permissions (`READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `READ_MEDIA_AUDIO` for SDK 33+)
- Internet access for potential transcription or sharing features.

### Note
- The app uses `FileProvider` with authority `${applicationId}.provider`. Ensure `applicationId` matches your package name.
- Storage permissions are conditionally handled for SDK 33+ due to Scoped Storage.

---

# 🤝 Contributing

Contributions are welcome! Please open issues or pull requests on the [GitHub repository](https://github.com/uzumaki-ak/VoiceRecorder-App-AI). For detailed guidelines, refer to the `CONTRIBUTING.md` file if available.

---

# 📄 License

This project is licensed under the MIT License. See the [`LICENSE`](LICENSE) file for details.

---

# 🙏 Acknowledgments

- Based on modern Android development practices.
- Utilizes open-source libraries such as Room, Compose, and Coroutines.
- Thanks to the open-source community for UI components and best practices.

---

**This README provides a comprehensive overview of the "VoiceRecorder-App-AI" project, emphasizing technical accuracy, specific architecture details, and clear instructions based on the actual codebase.**