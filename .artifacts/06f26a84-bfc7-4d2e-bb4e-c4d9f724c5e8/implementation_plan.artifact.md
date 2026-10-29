# Implementation Plan - Integrating scrollmapper/bible_databases with Room & Ktor

This plan outlines the integration of offline Bible databases (inspired by [scrollmapper/bible_databases](https://github.com/scrollmapper/bible_databases)) using **Room** for local storage (notes, bookmarks, highlights, offline public-domain bibles) and **Ktor** for fetching NIV passages via API.Bible.

## User Review Required

> [!IMPORTANT]
> - **Room Database**: We will integrate Room with KSP for robust offline storage of user notes, bookmarks, highlights, and offline Bible chapters/verses.
> - **Hybrid Strategy**: Offline Bibles (public domain translations) stored locally, while NIV passages are fetched dynamically via Ktor from API.Bible.

## Proposed Changes

### [Build & Dependencies]
#### [MODIFY] [libs.versions.toml](file:///Volumes/xSSD/AndroidStudio/BibleFaithFuelBible/gradle/libs.versions.toml) & [app/build.gradle.kts](file:///Volumes/xSSD/AndroidStudio/BibleFaithFuelBible/app/build.gradle.kts)
- Add Room dependencies (`room-runtime`, `room-ktx`, `room-compiler`) and KSP plugin.

### [Data Layer - Room & scrollmapper Schema]
#### [NEW] [BibleDatabase.kt](file:///Volumes/xSSD/AndroidStudio/BibleFaithFuelBible/app/src/main/java/com/outofthebox/biblefaithfuelbible/data/local/BibleDatabase.kt)
- Room Database definition with entities:
  - `VerseEntity` (Book, Chapter, Verse, Text, Version)
  - `NoteEntity` (Id, VerseReference, Content, Timestamp)
  - `BookmarkEntity` (Id, VerseReference, Timestamp)
  - `HighlightEntity` (Id, VerseReference, Color, Timestamp)

#### [NEW] [BibleDao.kt](file:///Volumes/xSSD/AndroidStudio/BibleFaithFuelBible/app/src/main/java/com/outofthebox/biblefaithfuelbible/data/local/BibleDao.kt)
- DAO queries for verses, search, notes, bookmarks, and highlights.

### [Repository & ViewModels]
#### [MODIFY] [BibleRepository.kt](file:///Volumes/xSSD/AndroidStudio/BibleFaithFuelBible/app/src/main/java/com/outofthebox/biblefaithfuelbible/data/repository/BibleRepository.kt)
- Integrate local Room database queries alongside Ktor remote API calls.

### [UI Layer]
#### [MODIFY] [BibleScreen.kt](file:///Volumes/xSSD/AndroidStudio/BibleFaithFuelBible/app/src/main/java/com/outofthebox/biblefaithfuelbible/ui/screens/BibleScreen.kt)
- Enhance UI to support offline verse browsing, saving notes, bookmarks, highlights, and fetching NIV passages online.

## Verification Plan

### Automated Tests
- Gradle build test (`./gradlew assembleDebug`).

### Manual Verification
- Verify database creation, note/bookmark persistence, and Ktor NIV API fetching on device/emulator.
