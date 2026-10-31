# Bible Faith Fuel

A modern, 100% offline Android Holy Bible application built with Jetpack Compose, Material 3, and SQLite. Designed for an immersive, distraction-free reading and study experience.

---

## ✨ Features

- **📖 100% Offline Reading & Multi-Translation**: Read the Holy Bible anywhere offline. Pre-packaged with **Berean Standard Bible (BSB)**, **American Standard Version (ASV)**, and **King James Version (KJV)** with instant translation switching.
- **🔍 Enhanced Search & Smart Suggestions**: Lightning-fast offline verse search with debouncing, in-memory caching, lookup indexes ($O(\log N)$ performance), and instant topic/book suggestions.
- **📜 Continuous Book Reader**:
  - Smooth book-style continuous paragraph flow.
  - Floating chapter-turning navigation buttons and page-turn animations.
  - Quick font size adjustment (`AA`).
- **🎯 Precise Navigation (`Book > Chapter > verses`)**:
  - Structured 3-step navigation flow and exact-verse picker grid.
- **🗣️ Native Offline Text-to-Speech (TTS)**:
  - Zero-cost, lightning-fast offline audio playback for selected verses with chapter chunking support.
- **📱 Home Screen Verse Widgets**:
  - Customizable Verse of the Day widgets with multiple background themes (Warm Parchment, Dark Glass, Sunset, Ocean, etc.), text alignment, and refresh intervals (1–24 hours).
- **🎨 Typography & Custom Fonts**:
  - Choose between Inter, Libre Baskerville, Lora, EB Garamond, Gentium Book Plus, and Roboto Flex.
- **🎨 Verse Highlighter & Bookmarks/Notes**:
  - Highlight verses in soft pastel colors.
  - Standalone Bookmarks and Notes screens with timestamp tracking, sorting (Newest/Oldest), multi-item batch deletion, and individual verse-level removal inside bookmarks.
- **🌓 Dark Mode & Custom Accents**:
  - Seamless light/dark theme toggle and iOS-inspired color accent options.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: MVVM (Model-View-ViewModel) with Kotlin Coroutines and StateFlow
- **Database**: SQLite (Pre-packaged asset databases + local user storage with `ATTACH DATABASE` and optimized lookup indexes)
- **Text-to-Speech**: Native Android `TextToSpeech`
- **Build System**: Gradle with Android Gradle Plugin (AGP)

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/donedos/Bible-Faith-Fuel.git
   ```
   
2. Open the project in **Android Studio**.

3. Run Gradle sync and build the project:
   ```bash
   ./gradlew assembleDebug
   ```

4. Install and run on an Android emulator or physical device.

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
