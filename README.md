# Bible Faith Fuel

A modern, 100% offline Android Holy Bible application built with Jetpack Compose, Material 3, and SQLite. Designed for an immersive, distraction-free reading and study experience.

---

## ✨ Features

- **📖 100% Offline Reading**: Read the Holy Bible anywhere without needing an internet connection.
- **📚 Multiple Translations**: Pre-packaged with the **Berean Standard Bible (BSB)** as default and the **American Standard Version (ASV)**. Easily switch between translations at any time.
- **📜 Continuous Book Reader**:
  - Smooth book-style continuous paragraph flow.
  - Elegant superscript verse numbers.
  - Floating chapter-turning navigation buttons (`‹` / `›`) and horizontal swipe gestures.
  - Quick font size adjustment (`AA`).
- **🎯 Precise Navigation (`Book > Chapter > verses`)**:
  - Structured 3-step navigation flow.
  - Direct exact-verse picker grid (4-column layout) to jump straight to any specific verse.
- **🎨 Verse Highlighter**:
  - Highlight verses in 6 soft pastel colors (Yellow, Green, Blue, Pink, Purple, Orange).
  - High-contrast text readability on any highlight background.
- **🔖 Saved Bookmarks & Notes**:
  - Dedicated standalone screens for managing Bookmarks and Personal Notes.
  - Create new bookmark folders or append verses to existing bookmarks.
  - Edit or delete notes and bookmarks.
  - Tap any saved bookmark or note to jump directly to that exact verse in the Bible Reader.
- **💾 Permanent Data Persistence**:
  - User bookmarks, notes, and highlights are stored in a dedicated local database (`bible_faith_fuel.db`) that is never overwritten, ensuring your personal study data is 100% safe across app restarts and translation switches.
- **🌓 Dark Mode & Custom Accents**:
  - Seamless light and dark theme toggle.
  - iOS-inspired color accent options.

---

## 🛠️ Tech Stack

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: MVVM (Model-View-ViewModel) with Kotlin Coroutines and StateFlow
- **Database**: SQLite (Pre-packaged asset databases + local user storage with `ATTACH DATABASE`)
- **Build System**: Gradle with Android Gradle Plugin (AGP)

---

## 🚀 Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/bible-faith-fuel.git
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
