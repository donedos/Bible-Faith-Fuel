package com.outofthebox.biblefaithfuelbible.data.repository

import com.outofthebox.biblefaithfuelbible.data.local.BibleDatabaseHelper
import com.outofthebox.biblefaithfuelbible.data.local.BookRecord
import com.outofthebox.biblefaithfuelbible.data.local.BookmarkRecord
import com.outofthebox.biblefaithfuelbible.data.local.HighlightRecord
import com.outofthebox.biblefaithfuelbible.data.local.NoteRecord
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord

class BibleRepository(
    private val dbHelper: BibleDatabaseHelper
) {
    fun getBooks(isOldTestament: Boolean): List<BookRecord> {
        return dbHelper.getBooks(isOldTestament)
    }

    fun getChapters(bookId: Int): List<Int> {
        return dbHelper.getChaptersForBook(bookId)
    }

    fun getVersesForChapter(bookId: Int, chapter: Int): List<VerseRecord> {
        return dbHelper.getVersesForChapter(bookId, chapter)
    }

    fun getVersesByReferences(referencesStr: String): List<VerseRecord> {
        return dbHelper.getVersesByReferences(referencesStr)
    }

    fun searchOfflineVerses(query: String, version: String = "ASV"): List<VerseRecord> {
        return dbHelper.searchVersesEnhanced(query, version)
    }

    fun getSearchSuggestions(query: String): List<String> {
        return dbHelper.getSuggestions(query)
    }

    fun getNotes(): List<NoteRecord> {
        return dbHelper.getNotes()
    }

    fun saveNote(reference: String, content: String) {
        dbHelper.insertNote(reference, content)
    }

    fun deleteNote(id: Long) {
        dbHelper.deleteNote(id)
    }

    fun updateNote(id: Long, content: String) {
        dbHelper.updateNote(id, content)
    }

    fun getBookmarks(): List<BookmarkRecord> {
        return dbHelper.getBookmarks()
    }

    fun toggleBookmark(reference: String, isBookmarked: Boolean) {
        dbHelper.toggleBookmark(reference, isBookmarked)
    }

    fun saveCustomBookmark(reference: String, label: String, color: String) {
        dbHelper.saveCustomBookmark(reference, label, color)
    }

    fun appendVerseToBookmark(bookmarkKey: String, newVerseRef: String) {
        dbHelper.appendVerseToBookmark(bookmarkKey, newVerseRef)
    }

    fun renameBookmark(referenceKey: String, newLabel: String) {
        dbHelper.renameBookmark(referenceKey, newLabel)
    }

    fun deleteBookmark(referenceKey: String) {
        dbHelper.deleteBookmark(referenceKey)
    }

    fun getHighlights(): List<HighlightRecord> {
        return dbHelper.getHighlights()
    }

    fun saveHighlight(reference: String, color: String) {
        dbHelper.saveHighlight(reference, color)
    }
}
