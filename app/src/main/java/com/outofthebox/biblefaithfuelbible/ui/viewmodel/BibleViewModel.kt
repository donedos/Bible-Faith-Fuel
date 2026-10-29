package com.outofthebox.biblefaithfuelbible.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.outofthebox.biblefaithfuelbible.data.local.BibleDatabaseHelper
import com.outofthebox.biblefaithfuelbible.data.local.BookRecord
import com.outofthebox.biblefaithfuelbible.data.local.BookmarkRecord
import com.outofthebox.biblefaithfuelbible.data.local.HighlightRecord
import com.outofthebox.biblefaithfuelbible.data.local.NoteRecord
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord
import com.outofthebox.biblefaithfuelbible.data.repository.BibleRepository
import com.outofthebox.biblefaithfuelbible.ui.theme.IosAccentColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BibleUiState(
    val selectedIosAccent: IosAccentColor = IosAccentColor.BLUE,
    val isDarkTheme: Boolean = false,
    val selectedTranslation: String = "BSB",
    val isSettingsDialogOpen: Boolean = false,
    val bookmarkDialogVerseRef: String? = null,
    val verseActionDialogRef: String? = null,
    val selectedDrawerTab: String = "reader", // "reader", "bookmarks", "notes"
    val editingBookmark: BookmarkRecord? = null,
    val isOldTestament: Boolean = true,
    val books: List<BookRecord> = emptyList(),
    val selectedBook: BookRecord? = null,
    val chapters: List<Int> = emptyList(),
    val selectedChapter: Int? = null,
    val chapterVerses: List<VerseRecord> = emptyList(),
    val selectedVerseNum: Int? = null,
    val isPickingVerse: Boolean = false,
    val shouldScrollToVerse: Boolean = false,
    val fontSizeSp: Int = 18,
    val searchQuery: String = "",
    val suggestions: List<String> = emptyList(),
    val offlineSearchResults: List<VerseRecord> = emptyList(),
    val notes: List<NoteRecord> = emptyList(),
    val bookmarks: List<BookmarkRecord> = emptyList(),
    val highlights: List<HighlightRecord> = emptyList()
)

class BibleViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = BibleDatabaseHelper(application)
    private val repository = BibleRepository(dbHelper = dbHelper)
    private val sp = application.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(BibleUiState())
    val uiState: StateFlow<BibleUiState> = _uiState.asStateFlow()

    init {
        loadSavedThemeAccent()
        loadLocalData()
        loadBooks(isOldTestament = true)
    }

    private fun loadSavedThemeAccent() {
        val savedName = sp.getString("ios_accent_name", IosAccentColor.BLUE.name)
        val isDark = sp.getBoolean("is_dark_theme", false)
        val translation = sp.getString("bible_translation", "BSB") ?: "BSB"
        val fontSize = sp.getInt("font_size_sp", 18)
        val accent = try {
            IosAccentColor.valueOf(savedName ?: IosAccentColor.BLUE.name)
        } catch (e: Exception) {
            IosAccentColor.BLUE
        }
        dbHelper.setTranslation(translation)
        _uiState.update {
            it.copy(
                selectedIosAccent = accent,
                isDarkTheme = isDark,
                selectedTranslation = translation,
                fontSizeSp = fontSize
            )
        }
    }

    fun getVersesByReferences(referencesStr: String): List<VerseRecord> {
        return repository.getVersesByReferences(referencesStr)
    }

    fun navigateToVerse(verseRef: String) {
        val parsed = parseVerseReference(verseRef)
        if (parsed != null) {
            val (bookName, chapterNum, verseNum) = parsed
            viewModelScope.launch {
                val otBooks = repository.getBooks(true)
                val ntBooks = repository.getBooks(false)
                val allBooks = otBooks + ntBooks
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
                                isPickingVerse = false,
                                shouldScrollToVerse = true,
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
        val regex = Regex("^([1-3]?\\s*[a-zA-Z]+)\\s+(\\d+):(\\d+)$", RegexOption.IGNORE_CASE)
        val match = regex.find(refStr.trim())
        if (match != null) {
            val book = BibleDatabaseHelper.resolveBookName(match.groupValues[1])
            val chapter = match.groupValues[2].toIntOrNull() ?: 1
            val verse = match.groupValues[3].toIntOrNull() ?: 1
            return Triple(book, chapter, verse)
        }
        val regexNoVerse = Regex("^([1-3]?\\s*[a-zA-Z]+)\\s+(\\d+)$", RegexOption.IGNORE_CASE)
        val matchNoVerse = regexNoVerse.find(refStr.trim())
        if (matchNoVerse != null) {
            val book = BibleDatabaseHelper.resolveBookName(matchNoVerse.groupValues[1])
            val chapter = matchNoVerse.groupValues[2].toIntOrNull() ?: 1
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
        dbHelper.setTranslation(translation)
        _uiState.update {
            it.copy(
                selectedTranslation = translation,
                selectedBook = null,
                selectedChapter = null,
                chapters = emptyList(),
                chapterVerses = emptyList(),
                selectedVerseNum = null,
                isPickingVerse = false,
                shouldScrollToVerse = false
            )
        }
        loadBooks(uiState.value.isOldTestament)
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

    fun loadLocalData() {
        viewModelScope.launch {
            val notes = repository.getNotes()
            val bookmarks = repository.getBookmarks()
            val highlights = repository.getHighlights()
            _uiState.update { it.copy(notes = notes, bookmarks = bookmarks, highlights = highlights) }
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
                isPickingVerse = false,
                shouldScrollToVerse = false
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
                    isPickingVerse = false,
                    shouldScrollToVerse = false
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
                    shouldScrollToVerse = false
                )
            }
        }
    }

    fun readFullChapter() {
        _uiState.update { it.copy(isPickingVerse = false, selectedVerseNum = null, shouldScrollToVerse = false) }
    }

    fun selectExactVerse(verseNum: Int) {
        _uiState.update { it.copy(isPickingVerse = false, selectedVerseNum = verseNum, shouldScrollToVerse = true) }
    }

    fun clearScrollToVerseFlag() {
        _uiState.update { it.copy(shouldScrollToVerse = false) }
    }

    fun selectVerseNumber(verseNum: Int?) {
        _uiState.update {
            if (it.selectedVerseNum == verseNum) it.copy(selectedVerseNum = null, shouldScrollToVerse = false)
            else it.copy(selectedVerseNum = verseNum, shouldScrollToVerse = false)
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

    fun nextChapter() {
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: 1
        val maxChapter = _uiState.value.chapters.maxOrNull() ?: 1

        if (currentChapter < maxChapter) {
            selectChapter(currentChapter + 1)
        } else {
            // Move to next book if available
            val books = _uiState.value.books
            val currentIndex = books.indexOfFirst { it.id == currentBook.id }
            if (currentIndex in 0 until books.size - 1) {
                val nextBook = books[currentIndex + 1]
                selectBook(nextBook)
                selectChapter(1)
            }
        }
    }

    fun previousChapter() {
        val currentBook = _uiState.value.selectedBook ?: return
        val currentChapter = _uiState.value.selectedChapter ?: 1

        if (currentChapter > 1) {
            selectChapter(currentChapter - 1)
        } else {
            // Move to previous book's last chapter
            val books = _uiState.value.books
            val currentIndex = books.indexOfFirst { it.id == currentBook.id }
            if (currentIndex > 0) {
                val prevBook = books[currentIndex - 1]
                viewModelScope.launch {
                    val prevChapters = repository.getChapters(prevBook.id)
                    val lastChapter = prevChapters.maxOrNull() ?: 1
                    _uiState.update {
                        it.copy(
                            selectedBook = prevBook,
                            chapters = prevChapters,
                            selectedChapter = lastChapter
                        )
                    }
                    val verses = repository.getVersesForChapter(prevBook.id, lastChapter)
                    _uiState.update { it.copy(chapterVerses = verses, selectedVerseNum = null, isPickingVerse = true, shouldScrollToVerse = false) }
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
                isPickingVerse = false,
                shouldScrollToVerse = false
            )
        }
    }

    fun goBackToChapters() {
        _uiState.update {
            it.copy(
                selectedChapter = null,
                chapterVerses = emptyList(),
                selectedVerseNum = null,
                isPickingVerse = false,
                shouldScrollToVerse = false
            )
        }
    }

    fun updateSearchQuery(query: String) {
        val suggestions = repository.getSearchSuggestions(query)
        _uiState.update { it.copy(searchQuery = query, suggestions = suggestions) }
        if (query.isNotBlank()) {
            searchOffline(query)
        } else {
            _uiState.update { it.copy(offlineSearchResults = emptyList()) }
        }
    }

    fun searchOffline(query: String) {
        viewModelScope.launch {
            val results = repository.searchOfflineVerses(query)
            val suggestions = repository.getSearchSuggestions(query)
            _uiState.update { it.copy(offlineSearchResults = results, suggestions = suggestions) }
        }
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
}
