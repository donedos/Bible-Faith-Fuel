package com.outofthebox.biblefaithfuelbible.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.outofthebox.biblefaithfuelbible.data.local.BibleDatabaseHelper
import com.outofthebox.biblefaithfuelbible.data.local.BookRecord
import com.outofthebox.biblefaithfuelbible.data.local.BookmarkRecord
import com.outofthebox.biblefaithfuelbible.data.local.HighlightRecord
import com.outofthebox.biblefaithfuelbible.data.local.NoteRecord
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord
import com.outofthebox.biblefaithfuelbible.data.repository.BibleRepository
import com.outofthebox.biblefaithfuelbible.ui.theme.AppFontOption
import com.outofthebox.biblefaithfuelbible.ui.theme.IosAccentColor
import com.outofthebox.biblefaithfuelbible.ui.theme.PageTurnAnimation
import com.outofthebox.biblefaithfuelbible.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

data class WidgetState(
    val selectedBackground: String = "Warm Parchment",
    val refreshIntervalHours: Int = 24,
    val selectedBookName: String = "All Books",
    val textAlignment: String = "Center",
    val currentVerse: VerseRecord? = null,
    val fontOption: AppFontOption = AppFontOption.DEFAULT
)

data class BibleUiState(
    val selectedIosAccent: IosAccentColor = IosAccentColor.BLUE,
    val isDarkTheme: Boolean = false,
    val selectedTranslation: String = "BSB",
    val selectedFontOption: AppFontOption = AppFontOption.DEFAULT,
    val pageTurnAnimation: PageTurnAnimation = PageTurnAnimation.DEFAULT,
    val ttsVoiceGender: String = "Female",
    val isSpeaking: Boolean = false,
    val isSettingsDialogOpen: Boolean = false,
    val bookmarkDialogVerseRef: String? = null,
    val verseActionDialogRef: String? = null,
    val selectedDrawerTab: String = "reader", // "reader", "bookmarks", "notes", "settings", "highlights", "widgets"
    val editingBookmark: BookmarkRecord? = null,
    val isOldTestament: Boolean = true,
    val hasShownSplash: Boolean = false,
    val books: List<BookRecord> = emptyList(),
    val allBooks: List<BookRecord> = emptyList(),
    val selectedBook: BookRecord? = null,
    val chapters: List<Int> = emptyList(),
    val selectedChapter: Int? = null,
    val chapterVerses: List<VerseRecord> = emptyList(),
    val selectedVerseNum: Int? = null,
    val selectedVerseNums: Set<Int> = emptySet(),
    val isPickingVerse: Boolean = false,
    val shouldScrollToVerse: Boolean = false,
    val chapterTransitionDirection: Int = 0, // 1 for next, -1 for previous, 0 for none
    val chapterTransitionSerial: Int = 0,
    val fontSizeSp: Int = 18,
    val searchQuery: String = "",
    val suggestions: List<String> = emptyList(),
    val offlineSearchResults: List<VerseRecord> = emptyList(),
    val notes: List<NoteRecord> = emptyList(),
    val bookmarks: List<BookmarkRecord> = emptyList(),
    val highlights: List<HighlightRecord> = emptyList(),
    val bookmarkVerses: Map<String, List<VerseRecord>> = emptyMap(),
    val highlightVerses: Map<String, List<VerseRecord>> = emptyMap(),
    val widgetState: WidgetState = WidgetState()
)

class BibleViewModel(application: Application) : AndroidViewModel(application) {

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 250L
    }

    private val dbHelper by lazy { BibleDatabaseHelper(application) }
    private val repository by lazy { BibleRepository(dbHelper = dbHelper) }
    private val sp = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(BibleUiState())
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadSavedThemeAccent()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                dbHelper.setTranslation(_uiState.value.selectedTranslation)
            }
            loadLocalDataNow()
            val books = repository.getBooks(_uiState.value.isOldTestament)
            val otBooks = repository.getBooks(true)
            val ntBooks = repository.getBooks(false)
            _uiState.update { it.copy(books = books, allBooks = otBooks + ntBooks) }
            loadWidgetPreferencesNow()
            WidgetUpdater.updateAll(application)
        }
    }

    private fun loadSavedThemeAccent() {
        val savedName = sp.getString("ios_accent_name", IosAccentColor.BLUE.name)
        val isDark = sp.getBoolean("is_dark_theme", false)
        val translation = sp.getString("bible_translation", "BSB") ?: "BSB"
        val fontSize = sp.getInt("font_size_sp", 18)
        val fontName = sp.getString("app_font_option", AppFontOption.DEFAULT.name)
        val pageTurnName = sp.getString("page_turn_animation", PageTurnAnimation.DEFAULT.name)
        val ttsGender = sp.getString("tts_voice_gender", "Female") ?: "Female"
        val accent = try {
            IosAccentColor.valueOf(savedName ?: IosAccentColor.BLUE.name)
        } catch (e: Exception) {
            IosAccentColor.BLUE
        }
        val fontOption = try {
            AppFontOption.valueOf(fontName ?: AppFontOption.DEFAULT.name)
        } catch (e: Exception) {
            AppFontOption.DEFAULT
        }
        val pageTurnAnimation = try {
            PageTurnAnimation.valueOf(pageTurnName ?: PageTurnAnimation.DEFAULT.name)
        } catch (e: Exception) {
            PageTurnAnimation.DEFAULT
        }
        _uiState.update {
            it.copy(
                selectedIosAccent = accent,
                isDarkTheme = isDark,
                selectedTranslation = translation,
                fontSizeSp = fontSize,
                selectedFontOption = fontOption,
                pageTurnAnimation = pageTurnAnimation,
                ttsVoiceGender = ttsGender
            )
        }
    }

    fun markSplashShown() {
        _uiState.update { it.copy(hasShownSplash = true) }
    }

    fun updateFontOption(option: AppFontOption) {
        sp.edit()
            .putString("app_font_option", option.name)
            .putString("widget_font_option", option.name)
            .apply()
        _uiState.update {
            it.copy(
                selectedFontOption = option,
                widgetState = it.widgetState.copy(fontOption = option)
            )
        }
        WidgetUpdater.updateAll(getApplication())
    }

    fun updatePageTurnAnimation(option: PageTurnAnimation) {
        sp.edit().putString("page_turn_animation", option.name).apply()
        _uiState.update { it.copy(pageTurnAnimation = option) }
    }

    fun navigateToVerse(verseRef: String) {
        val parsed = parseVerseReference(verseRef)
        if (parsed != null) {
            val (bookName, chapterNum, verseNum) = parsed
            viewModelScope.launch {
                val allBooks = _uiState.value.allBooks.ifEmpty {
                    repository.getBooks(true) + repository.getBooks(false)
                }
                val matchedBook = allBooks.find { it.name.equals(bookName, ignoreCase = true) || it.name.contains(bookName, true) }
                if (matchedBook != null) {
                    val isOt = matchedBook.id <= 39
                    selectTestament(isOt)
                    val chapters = repository.getChapters(matchedBook.id)
                    if (chapters.contains(chapterNum)) {
                        val verses = repository.getVersesForChapter(matchedBook.id, chapterNum)
                        _uiState.update {
                            it.copy(
                                selectedBook = matchedBook,
                                chapters = chapters,
                                selectedChapter = chapterNum,
                                chapterVerses = verses,
                                selectedVerseNum = verseNum,
                                selectedVerseNums = setOf(verseNum),
                                isPickingVerse = false,
                                shouldScrollToVerse = true,
                                chapterTransitionDirection = 0,
                                selectedDrawerTab = "reader",
                                searchQuery = "",
                                offlineSearchResults = emptyList()
                            )
                        }
                    }
                }
            }
        } else {
            selectDrawerTab("reader")
            updateSearchQuery(verseRef)
        }
    }

    private fun parseVerseReference(refStr: String): Triple<String, Int, Int>? {
        val trimmed = refStr.trim()
        val colonIdx = trimmed.lastIndexOf(':')
        if (colonIdx != -1) {
            val verseStr = trimmed.substring(colonIdx + 1).trim()
            val beforeColon = trimmed.substring(0, colonIdx).trim()
            val spaceIdx = beforeColon.lastIndexOf(' ')
            if (spaceIdx != -1) {
                val rawBook = beforeColon.substring(0, spaceIdx).trim()
                val chapterStr = beforeColon.substring(spaceIdx + 1).trim()
                val book = BibleDatabaseHelper.resolveBookName(rawBook)
                val chapter = chapterStr.toIntOrNull() ?: 1
                val verse = verseStr.toIntOrNull() ?: 1
                return Triple(book, chapter, verse)
            }
        }
        val spaceIdx = trimmed.lastIndexOf(' ')
        if (spaceIdx != -1) {
            val rawBook = trimmed.substring(0, spaceIdx).trim()
            val chapterStr = trimmed.substring(spaceIdx + 1).trim()
            val book = BibleDatabaseHelper.resolveBookName(rawBook)
            val chapter = chapterStr.toIntOrNull() ?: 1
            return Triple(book, chapter, 1)
        }
        return null
    }

    fun selectDrawerTab(tab: String) {
        _uiState.update { it.copy(selectedDrawerTab = tab) }
    }

    fun updateIosAccentColor(accent: IosAccentColor) {
        sp.edit().putString("ios_accent_name", accent.name).apply()
        _uiState.update { it.copy(selectedIosAccent = accent) }
    }

    fun updateTranslation(translation: String) {
        sp.edit().putString("bible_translation", translation).apply()
        _uiState.update {
            it.copy(
                selectedTranslation = translation,
                selectedBook = null,
                selectedChapter = null,
                chapters = emptyList(),
                chapterVerses = emptyList(),
                selectedVerseNum = null,
                selectedVerseNums = emptySet(),
                isPickingVerse = false,
                shouldScrollToVerse = false,
                chapterTransitionDirection = 0
            )
        }
        viewModelScope.launch {
            repository.clearSearchCache()
            withContext(Dispatchers.IO) { dbHelper.setTranslation(translation) }
            loadBooks(uiState.value.isOldTestament)
            loadLocalDataNow()
        }
    }

    fun toggleDarkTheme() {
        val newDark = !_uiState.value.isDarkTheme
        updateDarkTheme(newDark)
    }

    fun updateDarkTheme(isDark: Boolean) {
        sp.edit().putBoolean("is_dark_theme", isDark).apply()
        _uiState.update { it.copy(isDarkTheme = isDark) }
    }

    fun openSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = true) }
    }

    fun closeSettingsDialog() {
        _uiState.update { it.copy(isSettingsDialogOpen = false) }
    }

    fun openBookmarkDialog(verseRef: String) {
        _uiState.update { it.copy(bookmarkDialogVerseRef = verseRef) }
    }

    fun closeBookmarkDialog() {
        _uiState.update { it.copy(bookmarkDialogVerseRef = null) }
    }

    fun openVerseActionDialog(verseRef: String) {
        _uiState.update { it.copy(verseActionDialogRef = verseRef) }
    }

    fun closeVerseActionDialog() {
        _uiState.update { it.copy(verseActionDialogRef = null) }
    }

    fun openEditingBookmarkDialog(bookmark: BookmarkRecord) {
        _uiState.update { it.copy(editingBookmark = bookmark) }
    }

    fun closeEditingBookmarkDialog() {
        _uiState.update { it.copy(editingBookmark = null) }
    }

    fun saveCustomBookmark(reference: String, label: String, colorHex: String) {
        viewModelScope.launch {
            repository.saveCustomBookmark(reference, label, colorHex)
            closeBookmarkDialog()
            closeVerseActionDialog()
            loadLocalData()
        }
    }

    fun appendVerseToExistingBookmark(existingBookmarkKey: String, newVerseRef: String) {
        viewModelScope.launch {
            repository.appendVerseToBookmark(existingBookmarkKey, newVerseRef)
            closeBookmarkDialog()
            closeVerseActionDialog()
            loadLocalData()
        }
    }

    fun renameBookmark(referenceKey: String, newLabel: String) {
        viewModelScope.launch {
            repository.renameBookmark(referenceKey, newLabel)
            closeEditingBookmarkDialog()
            loadLocalData()
        }
    }

    fun deleteBookmark(referenceKey: String) {
        viewModelScope.launch {
            repository.deleteBookmark(referenceKey)
            loadLocalData()
        }
    }

    fun deleteBookmarks(referenceKeys: Set<String>) {
        viewModelScope.launch {
            referenceKeys.forEach { refKey ->
                repository.deleteBookmark(refKey)
            }
            loadLocalData()
        }
    }

    fun removeVerseFromBookmark(bookmarkKey: String, verseRefToRemove: String) {
        viewModelScope.launch {
            repository.removeVerseFromBookmark(bookmarkKey, verseRefToRemove)
            loadLocalData()
        }
    }

    fun removeVersesFromBookmark(bookmarkKey: String, verseRefsToRemove: Set<String>) {
        viewModelScope.launch {
            repository.removeVersesFromBookmark(bookmarkKey, verseRefsToRemove)
            loadLocalData()
        }
    }

    fun loadLocalData() {
        viewModelScope.launch { loadLocalDataNow() }
    }

    private suspend fun loadLocalDataNow() = withContext(Dispatchers.IO) {
        val notesDeferred = async { repository.getNotes() }
        val bookmarksDeferred = async { repository.getBookmarks() }
        val highlightsDeferred = async { repository.getHighlights() }

        val notes = notesDeferred.await()
        val bookmarks = bookmarksDeferred.await()
        val highlights = highlightsDeferred.await()

        val bookmarkVerseDeferreds = bookmarks.map { bookmark ->
            async { bookmark.reference to repository.getVersesByReferences(bookmark.reference) }
        }
        val highlightVerseDeferreds = highlights
            .filter { it.color.isNotBlank() }
            .map { highlight ->
                async { highlight.reference to repository.getVersesByReferences(highlight.reference) }
            }

        val bookmarkVerses = bookmarkVerseDeferreds.awaitAll().toMap()
        val highlightVerses = highlightVerseDeferreds.awaitAll().toMap()

        _uiState.update {
            it.copy(
                notes = notes,
                bookmarks = bookmarks,
                highlights = highlights,
                bookmarkVerses = bookmarkVerses,
                highlightVerses = highlightVerses
            )
        }
    }

    fun selectTestament(isOldTestament: Boolean) {
        _uiState.update {
            it.copy(
                isOldTestament = isOldTestament,
                selectedBook = null,
                selectedChapter = null,
                chapters = emptyList(),
                chapterVerses = emptyList(),
                selectedVerseNum = null,
                selectedVerseNums = emptySet(),
                isPickingVerse = false,
                shouldScrollToVerse = false,
                chapterTransitionDirection = 0
            )
        }
        loadBooks(isOldTestament)
    }

    private fun loadBooks(isOldTestament: Boolean) {
        viewModelScope.launch {
            val books = repository.getBooks(isOldTestament)
            _uiState.update { it.copy(books = books) }
        }
    }

    fun selectBook(book: BookRecord) {
        viewModelScope.launch {
            val chapters = repository.getChapters(book.id)
            _uiState.update {
                it.copy(
                    selectedBook = book,
                    selectedChapter = null,
                    chapters = chapters,
                    chapterVerses = emptyList(),
                    selectedVerseNum = null,
                    selectedVerseNums = emptySet(),
                    isPickingVerse = false,
                    shouldScrollToVerse = false,
                    chapterTransitionDirection = 0
                )
            }
        }
    }

    fun selectChapter(chapter: Int) {
        val book = _uiState.value.selectedBook ?: return
        viewModelScope.launch {
            val verses = repository.getVersesForChapter(book.id, chapter)
            _uiState.update {
                it.copy(
                    selectedChapter = chapter,
                    chapterVerses = verses,
                    isPickingVerse = true,
                    selectedVerseNum = null,
                    selectedVerseNums = emptySet(),
                    shouldScrollToVerse = false,
                    chapterTransitionDirection = 0
                )
            }
        }
    }

    fun readFullChapter() {
        _uiState.update { it.copy(isPickingVerse = false, selectedVerseNum = null, selectedVerseNums = emptySet(), shouldScrollToVerse = false, chapterTransitionDirection = 0) }
    }

    fun selectExactVerse(verseNum: Int) {
        _uiState.update { it.copy(isPickingVerse = false, selectedVerseNum = verseNum, selectedVerseNums = setOf(verseNum), shouldScrollToVerse = true, chapterTransitionDirection = 0) }
    }

    fun clearScrollToVerseFlag() {
        _uiState.update { it.copy(shouldScrollToVerse = false, chapterTransitionDirection = 0) }
    }

    fun toggleVerseSelection(verseNum: Int) {
        _uiState.update { state ->
            val current = state.selectedVerseNums
            val updated = if (current.contains(verseNum)) {
                current - verseNum
            } else {
                current + verseNum
            }
            state.copy(
                selectedVerseNums = updated,
                selectedVerseNum = updated.minOrNull(),
                shouldScrollToVerse = false
            )
        }
    }

    fun clearVerseSelection() {
        _uiState.update {
            it.copy(
                selectedVerseNums = emptySet(),
                selectedVerseNum = null,
                shouldScrollToVerse = false
            )
        }
    }

    fun selectAllVersesInChapter() {
        val allNums = _uiState.value.chapterVerses.map { it.verse }.toSet()
        _uiState.update {
            it.copy(
                selectedVerseNums = allNums,
                selectedVerseNum = allNums.minOrNull(),
                shouldScrollToVerse = false
            )
        }
    }

    fun selectVerseNumber(verseNum: Int?) {
        if (verseNum == null) {
            clearVerseSelection()
        } else {
            toggleVerseSelection(verseNum)
        }
    }

    fun getSelectedVersesReference(): String {
        val state = _uiState.value
        val bookName = state.selectedBook?.name ?: ""
        val chapter = state.selectedChapter ?: 1
        val selectedNums = state.selectedVerseNums.sorted()
        if (selectedNums.isEmpty()) return ""

        val ranges = mutableListOf<String>()
        var start = selectedNums.first()
        var prev = start

        for (i in 1 until selectedNums.size) {
            val curr = selectedNums[i]
            if (curr == prev + 1) {
                prev = curr
            } else {
                if (start == prev) {
                    ranges.add("$start")
                } else {
                    ranges.add("$start-$prev")
                }
                start = curr
                prev = curr
            }
        }
        if (start == prev) {
            ranges.add("$start")
        } else {
            ranges.add("$start-$prev")
        }

        val verseRangesStr = ranges.joinToString(", ")
        return "$bookName $chapter:$verseRangesStr"
    }

    fun getSelectedVersesFormattedText(): String {
        val reference = getSelectedVersesReference()
        val translation = _uiState.value.selectedTranslation
        val selectedNums = _uiState.value.selectedVerseNums
        val selectedVerses = _uiState.value.chapterVerses
            .filter { it.verse in selectedNums }
            .sortedBy { it.verse }

        if (selectedVerses.isEmpty()) return ""

        val versesText = selectedVerses.joinToString("\n") { "${it.verse} ${it.text}" }
        return "$reference ($translation)\n$versesText"
    }

    fun saveHighlightForSelectedVerses(colorHex: String) {
        val bookName = _uiState.value.selectedBook?.name ?: return
        val chapter = _uiState.value.selectedChapter ?: return
        val selectedNums = _uiState.value.selectedVerseNums
        if (selectedNums.isEmpty()) return

        viewModelScope.launch {
            selectedNums.forEach { vNum ->
                val ref = "$bookName $chapter:$vNum"
                repository.saveHighlight(ref, colorHex)
            }
            clearVerseSelection()
            loadLocalData()
        }
    }

    fun cycleFontSize() {
        _uiState.update {
            val nextSize = when (it.fontSizeSp) {
                16 -> 18
                18 -> 22
                22 -> 26
                else -> 16
            }
            sp.edit().putInt("font_size_sp", nextSize).apply()
            it.copy(fontSizeSp = nextSize)
        }
    }

    private fun openChapterDirectly(chapter: Int, direction: Int) {
        val book = _uiState.value.selectedBook ?: return
        viewModelScope.launch {
            val verses = repository.getVersesForChapter(book.id, chapter)
            _uiState.update {
                it.copy(
                    selectedChapter = chapter,
                    chapterVerses = verses,
                    isPickingVerse = false,
                    selectedVerseNum = null,
                    selectedVerseNums = emptySet(),
                    shouldScrollToVerse = false,
                    chapterTransitionDirection = direction,
                    chapterTransitionSerial = it.chapterTransitionSerial + 1
                )
            }
        }
    }

    fun nextChapter() {
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: 1
        val maxChapter = _uiState.value.chapters.maxOrNull() ?: 1

        if (currentChapter < maxChapter) {
            openChapterDirectly(currentChapter + 1, 1)
        } else {
            // Move to next book if available
            val books = _uiState.value.books
            val currentIndex = books.indexOfFirst { it.id == currentBook.id }
            if (currentIndex in 0 until books.size - 1) {
                val nextBook = books[currentIndex + 1]
                viewModelScope.launch {
                    val nextChapters = repository.getChapters(nextBook.id)
                    val verses = repository.getVersesForChapter(nextBook.id, 1)
                    _uiState.update {
                        it.copy(
                            selectedBook = nextBook,
                            chapters = nextChapters,
                            selectedChapter = 1,
                            chapterVerses = verses,
                            isPickingVerse = false,
                            selectedVerseNum = null,
                            selectedVerseNums = emptySet(),
                            shouldScrollToVerse = false,
                            chapterTransitionDirection = 1,
                            chapterTransitionSerial = it.chapterTransitionSerial + 1
                        )
                    }
                }
            }
        }
    }

    fun previousChapter() {
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: 1

        if (currentChapter > 1) {
            openChapterDirectly(currentChapter - 1, -1)
        } else {
            // Move to previous book's last chapter
            val books = _uiState.value.books
            val currentIndex = books.indexOfFirst { it.id == currentBook.id }
            if (currentIndex > 0) {
                val prevBook = books[currentIndex - 1]
                viewModelScope.launch {
                    val prevChapters = repository.getChapters(prevBook.id)
                    val lastChapter = prevChapters.maxOrNull() ?: 1
                    val verses = repository.getVersesForChapter(prevBook.id, lastChapter)
                    _uiState.update {
                        it.copy(
                            selectedBook = prevBook,
                            chapters = prevChapters,
                            selectedChapter = lastChapter,
                            chapterVerses = verses,
                            isPickingVerse = false,
                            selectedVerseNum = null,
                            selectedVerseNums = emptySet(),
                            shouldScrollToVerse = false,
                            chapterTransitionDirection = -1,
                            chapterTransitionSerial = it.chapterTransitionSerial + 1
                        )
                    }
                }
            }
        }
    }

    fun goBackToBooks() {
        _uiState.update {
            it.copy(
                selectedBook = null,
                selectedChapter = null,
                chapters = emptyList(),
                chapterVerses = emptyList(),
                selectedVerseNum = null,
                selectedVerseNums = emptySet(),
                isPickingVerse = false,
                shouldScrollToVerse = false,
                chapterTransitionDirection = 0
            )
        }
    }

    fun goBackToChapters() {
        _uiState.update {
            it.copy(
                selectedChapter = null,
                chapterVerses = emptyList(),
                selectedVerseNum = null,
                selectedVerseNums = emptySet(),
                isPickingVerse = false,
                shouldScrollToVerse = false,
                chapterTransitionDirection = 0
            )
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query, suggestions = buildSuggestions(query)) }

        searchJob?.cancel()
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            _uiState.update { it.copy(offlineSearchResults = emptyList()) }
            return
        }

        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MS)
            val results = repository.searchOfflineVerses(trimmed, _uiState.value.selectedTranslation)
            if (_uiState.value.searchQuery.trim() == trimmed) {
                _uiState.update { it.copy(offlineSearchResults = results) }
            }
        }
    }

    private fun buildSuggestions(query: String): List<String> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return BibleDatabaseHelper.TOPIC_SUGGESTIONS.take(8)
        val lower = trimmed.lowercase()
        val topics = BibleDatabaseHelper.TOPIC_SUGGESTIONS.filter { it.lowercase().contains(lower) }
        val books = _uiState.value.allBooks
            .filter { it.name.lowercase().contains(lower) }
            .map { "${it.name} 1" }
        return (topics + books).distinct().take(10)
    }

    fun onSuggestionSelected(suggestion: String) {
        updateSearchQuery(suggestion)
    }

    fun addNote(reference: String, content: String) {
        viewModelScope.launch {
            repository.saveNote(reference, content)
            closeVerseActionDialog()
            loadLocalData()
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
            loadLocalData()
        }
    }

    fun updateNote(id: Long, content: String) {
        viewModelScope.launch {
            repository.updateNote(id, content)
            loadLocalData()
        }
    }

    fun toggleBookmark(reference: String) {
        viewModelScope.launch {
            val currentBookmarks = _uiState.value.bookmarks
            val isBookmarked = currentBookmarks.any { it.reference == reference }
            repository.toggleBookmark(reference, !isBookmarked)
            loadLocalData()
        }
    }

    fun saveHighlight(reference: String, color: String) {
        viewModelScope.launch {
            repository.saveHighlight(reference, color)
            closeVerseActionDialog()
            selectVerseNumber(null)
            loadLocalData()
        }
    }

    fun deleteHighlight(reference: String) {
        viewModelScope.launch {
            repository.saveHighlight(reference, "")
            loadLocalData()
        }
    }

    private suspend fun loadWidgetPreferencesNow() {
        val bg = sp.getString("widget_bg", "Warm Parchment") ?: "Warm Parchment"
        val refreshHours = sp.getInt("widget_refresh_hours", 24)
        val bookName = sp.getString("widget_book_name", "All Books") ?: "All Books"
        val textAlign = sp.getString("widget_text_align", "Center") ?: "Center"
        val fontName = sp.getString("widget_font_option", AppFontOption.DEFAULT.name)
        val fontOption = try {
            AppFontOption.valueOf(fontName ?: AppFontOption.DEFAULT.name)
        } catch (e: Exception) {
            AppFontOption.DEFAULT
        }

        val allBooks = _uiState.value.allBooks.ifEmpty {
            repository.getBooks(true) + repository.getBooks(false)
        }
        val randomVerse = repository.getRandomVerse(bookName)

        _uiState.update {
            it.copy(
                allBooks = allBooks,
                widgetState = WidgetState(
                    selectedBackground = bg,
                    refreshIntervalHours = refreshHours,
                    selectedBookName = bookName,
                    textAlignment = textAlign,
                    currentVerse = randomVerse,
                    fontOption = fontOption
                )
            )
        }
    }

    fun updateWidgetBackground(bg: String) {
        sp.edit().putString("widget_bg", bg).apply()
        _uiState.update { it.copy(widgetState = it.widgetState.copy(selectedBackground = bg)) }
        WidgetUpdater.updateAll(getApplication())
    }

    fun updateWidgetRefreshInterval(hours: Int) {
        sp.edit().putInt("widget_refresh_hours", hours).apply()
        _uiState.update { it.copy(widgetState = it.widgetState.copy(refreshIntervalHours = hours)) }
        WidgetUpdater.updateAll(getApplication())
    }

    fun updateWidgetBookName(bookName: String) {
        sp.edit().putString("widget_book_name", bookName).apply()
        viewModelScope.launch {
            val verse = repository.getRandomVerse(bookName)
            _uiState.update {
                it.copy(widgetState = it.widgetState.copy(selectedBookName = bookName, currentVerse = verse))
            }
            if (verse != null) {
                WidgetUpdater.pushVerse(getApplication(), verse)
            } else {
                WidgetUpdater.updateAll(getApplication())
            }
        }
    }

    fun updateWidgetTextAlignment(alignment: String) {
        sp.edit().putString("widget_text_align", alignment).apply()
        _uiState.update { it.copy(widgetState = it.widgetState.copy(textAlignment = alignment)) }
        WidgetUpdater.updateAll(getApplication())
    }

    fun generateNewRandomVerse() {
        val currentBookName = _uiState.value.widgetState.selectedBookName
        viewModelScope.launch {
            val verse = repository.getRandomVerse(currentBookName)
            _uiState.update {
                it.copy(widgetState = it.widgetState.copy(currentVerse = verse))
            }
            if (verse != null) {
                WidgetUpdater.pushVerse(getApplication(), verse)
            }
        }
    }

    private var tts: TextToSpeech? = null
    private var isTtsInitialized = false

    fun updateTtsVoiceGender(gender: String) {
        sp.edit().putString("tts_voice_gender", gender).apply()
        _uiState.update { it.copy(ttsVoiceGender = gender) }
    }

    private fun initTts() {
        if (tts == null) {
            tts = TextToSpeech(getApplication()) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    tts?.language = Locale.getDefault()
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _uiState.update { it.copy(isSpeaking = true) }
                        }

                        override fun onDone(utteranceId: String?) {
                            _uiState.update { it.copy(isSpeaking = false) }
                        }

                        @Deprecated("Legacy error reporting")
                        override fun onError(utteranceId: String?) {
                            _uiState.update { it.copy(isSpeaking = false) }
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _uiState.update { it.copy(isSpeaking = false) }
                        }
                    })
                    isTtsInitialized = true
                }
            }
        }
    }

    fun speakSelectedVerses() {
        val selectedNums = _uiState.value.selectedVerseNums
        val selectedVerses = _uiState.value.chapterVerses
            .filter { it.verse in selectedNums }
            .sortedBy { it.verse }
        if (selectedVerses.isEmpty()) return
        initTts()

        val speakAction = {
            applyTtsVoiceGender(_uiState.value.ttsVoiceGender)
            val reference = getSelectedVersesReference()
            val maxLength = TextToSpeech.getMaxSpeechInputLength().coerceAtMost(3900)

            // Speak reference first
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_utterance_id_ref")
            }
            tts?.speak("$reference.", TextToSpeech.QUEUE_FLUSH, params, "tts_utterance_id_ref")

            // Chunk and queue verses
            var currentChunk = StringBuilder()
            selectedVerses.forEachIndexed { index, verse ->
                val verseText = "Verse ${verse.verse}. ${verse.text}"
                if (currentChunk.length + verseText.length + 1 > maxLength) {
                    val chunkParams = Bundle().apply {
                        putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_utterance_id_$index")
                    }
                    tts?.speak(currentChunk.toString(), TextToSpeech.QUEUE_ADD, chunkParams, "tts_utterance_id_$index")
                    currentChunk = StringBuilder()
                }
                if (currentChunk.isNotEmpty()) currentChunk.append(" ")
                currentChunk.append(verseText)
            }

            if (currentChunk.isNotEmpty()) {
                val finalParams = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "tts_utterance_id_final")
                }
                tts?.speak(currentChunk.toString(), TextToSpeech.QUEUE_ADD, finalParams, "tts_utterance_id_final")
            }

            _uiState.update { it.copy(isSpeaking = true) }
        }

        if (isTtsInitialized) {
            speakAction()
        } else {
            viewModelScope.launch {
                delay(400)
                if (isTtsInitialized) {
                    speakAction()
                }
            }
        }
    }

    fun stopSpeech() {
        tts?.stop()
        _uiState.update { it.copy(isSpeaking = false) }
    }

    private fun applyTtsVoiceGender(gender: String) {
        val voices = tts?.voices ?: return
        val isFemale = gender.equals("Female", ignoreCase = true)

        // Filter strictly for English voices so it never speaks Spanish or other languages
        val englishVoices = voices.filter { it.locale.language.equals("en", ignoreCase = true) }
        if (englishVoices.isEmpty()) {
            tts?.language = Locale.US
            return
        }

        val matched = englishVoices.find { v ->
            val name = v.name.lowercase()
            if (isFemale) {
                name.contains("female") || name.contains("woman") || name.contains("sfg") || name.contains("tpf")
            } else {
                (name.contains("male") && !name.contains("female")) || name.contains("iog") || name.contains("sfb")
            }
        } ?: englishVoices.firstOrNull()

        if (matched != null) {
            tts?.voice = matched
        } else {
            tts?.language = Locale.US
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
    }
}
