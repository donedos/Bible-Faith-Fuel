package com.outofthebox.biblefaithfuelbible.data.repository

import android.util.LruCache
import com.outofthebox.biblefaithfuelbible.data.local.BibleDatabaseHelper
import com.outofthebox.biblefaithfuelbible.data.local.BookRecord
import com.outofthebox.biblefaithfuelbible.data.local.BookmarkRecord
import com.outofthebox.biblefaithfuelbible.data.local.HighlightRecord
import com.outofthebox.biblefaithfuelbible.data.local.NoteRecord
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BibleRepository(
    private val dbHelper: BibleDatabaseHelper
) {
    private val searchCache = LruCache<String, List<VerseRecord>>(100)
    private val suggestionCache = LruCache<String, List<String>>(100)

    fun clearSearchCache() {
        searchCache.evictAll()
        suggestionCache.evictAll()
    }

    suspend fun getBooks(isOldTestament: Boolean): List<BookRecord> =
        withContext(Dispatchers.IO) { dbHelper.getBooks(isOldTestament) }

    suspend fun getChapters(bookId: Int): List<Int> =
        withContext(Dispatchers.IO) { dbHelper.getChaptersForBook(bookId) }

    suspend fun getVersesForChapter(bookId: Int, chapter: Int): List<VerseRecord> =
        withContext(Dispatchers.IO) { dbHelper.getVersesForChapter(bookId, chapter) }

    suspend fun getVersesByReferences(referencesStr: String): List<VerseRecord> =
        withContext(Dispatchers.IO) { dbHelper.getVersesByReferences(referencesStr) }

    suspend fun getRandomVerse(bookName: String? = null): VerseRecord? =
        withContext(Dispatchers.IO) { dbHelper.getRandomVerse(bookName) }

    suspend fun searchOfflineVerses(query: String, version: String = "BSB"): List<VerseRecord> =
        withContext(Dispatchers.IO) {
            val key = "$version:${query.trim().lowercase()}"
            searchCache.get(key)?.let { return@withContext it }
            val results = dbHelper.searchVersesEnhanced(query, version)
            searchCache.put(key, results)
            results
        }

    suspend fun getSearchSuggestions(query: String): List<String> =
        withContext(Dispatchers.IO) {
            val key = query.trim().lowercase()
            suggestionCache.get(key)?.let { return@withContext it }
            val suggestions = dbHelper.getSuggestions(query)
            suggestionCache.put(key, suggestions)
            suggestions
        }

    suspend fun getNotes(): List<NoteRecord> =
        withContext(Dispatchers.IO) { dbHelper.getNotes() }

    suspend fun saveNote(reference: String, content: String) =
        withContext(Dispatchers.IO) { dbHelper.insertNote(reference, content) }

    suspend fun deleteNote(id: Long) =
        withContext(Dispatchers.IO) { dbHelper.deleteNote(id) }

    suspend fun updateNote(id: Long, content: String) =
        withContext(Dispatchers.IO) { dbHelper.updateNote(id, content) }

    suspend fun getBookmarks(): List<BookmarkRecord> =
        withContext(Dispatchers.IO) { dbHelper.getBookmarks() }

    suspend fun toggleBookmark(reference: String, isBookmarked: Boolean) =
        withContext(Dispatchers.IO) { dbHelper.toggleBookmark(reference, isBookmarked) }

    suspend fun saveCustomBookmark(reference: String, label: String, color: String) =
        withContext(Dispatchers.IO) { dbHelper.saveCustomBookmark(reference, label, color) }

    suspend fun appendVerseToBookmark(bookmarkKey: String, newVerseRef: String) =
        withContext(Dispatchers.IO) { dbHelper.appendVerseToBookmark(bookmarkKey, newVerseRef) }

    suspend fun renameBookmark(referenceKey: String, newLabel: String) =
        withContext(Dispatchers.IO) { dbHelper.renameBookmark(referenceKey, newLabel) }

    suspend fun deleteBookmark(referenceKey: String) =
        withContext(Dispatchers.IO) { dbHelper.deleteBookmark(referenceKey) }

    suspend fun removeVerseFromBookmark(bookmarkKey: String, verseRefToRemove: String) =
        withContext(Dispatchers.IO) { dbHelper.removeVerseFromBookmark(bookmarkKey, verseRefToRemove) }

    suspend fun removeVersesFromBookmark(bookmarkKey: String, verseRefsToRemove: Set<String>) =
        withContext(Dispatchers.IO) { dbHelper.removeVersesFromBookmark(bookmarkKey, verseRefsToRemove) }

    suspend fun getHighlights(): List<HighlightRecord> =
        withContext(Dispatchers.IO) { dbHelper.getHighlights() }

    suspend fun saveHighlight(reference: String, color: String) =
        withContext(Dispatchers.IO) { dbHelper.saveHighlight(reference, color) }
}
