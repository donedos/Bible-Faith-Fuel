package com.outofthebox.biblefaithfuelbible.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

data class BookRecord(val id: Int, val name: String)
data class VerseRecord(val bookId: String, val chapter: Int, val verse: Int, val text: String, val version: String)
data class NoteRecord(val id: Long, val reference: String, val content: String, val timestamp: Long)
data class BookmarkRecord(val reference: String, val label: String = reference, val color: String = "#FFD700", val timestamp: Long)
data class HighlightRecord(val reference: String, val color: String, val timestamp: Long)

class BibleDatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "bible_faith_fuel.db"
        private const val DATABASE_VERSION = 12

        private const val TABLE_NOTES = "notes"
        private const val TABLE_BOOKMARKS = "bookmarks"
        private const val TABLE_HIGHLIGHTS = "highlights"
        private const val BASE_TEXT_MARKER = "basetext.marker"

        val TOPIC_SUGGESTIONS = listOf(
            "Genesis 1:1", "Genesis 1:3", "John 3:16", "Psalm 23:1", "1 Corinthians 1:3",
            "Philippians 4:13", "Jeremiah 29:11", "Romans 8:28", "Proverbs 3:5", "Isaiah 40:31",
            "Matthew 28:19", "1 Corinthians 13:4", "Joshua 1:9", "Romans 12:2", "Galatians 5:22",
            "Ephesians 2:8", "Hebrews 11:1",
            "Love", "Faith", "Hope", "Peace", "Grace", "Salvation", "Forgiveness",
            "Prayer", "Healing", "Strength", "Wisdom", "Trust", "Comfort", "Joy",
            "Patience", "Protection", "Anxiety", "Fear", "Holy Spirit", "Creation",
            "Jesus", "God", "Light", "Shepherd", "Messiah", "Eternal Life"
        )

        private val BOOK_ALIASES = mapOf(
            "genesis" to "Genesis", "gen" to "Genesis", "ge" to "Genesis", "gn" to "Genesis",
            "exodus" to "Exodus", "exod" to "Exodus", "ex" to "Exodus", "exo" to "Exodus",
            "leviticus" to "Leviticus", "lev" to "Leviticus", "le" to "Leviticus", "lv" to "Leviticus",
            "numbers" to "Numbers", "num" to "Numbers", "nu" to "Numbers", "nm" to "Numbers",
            "deuteronomy" to "Deuteronomy", "deut" to "Deuteronomy", "dt" to "Deuteronomy", "deu" to "Deuteronomy",
            "joshua" to "Joshua", "josh" to "Joshua", "jos" to "Joshua", "jsh" to "Joshua",
            "judges" to "Judges", "judg" to "Judges", "jdg" to "Judges", "jg" to "Judges",
            "ruth" to "Ruth", "rut" to "Ruth", "ru" to "Ruth",
            "1 samuel" to "1 Samuel", "1samuel" to "1 Samuel", "1 sam" to "1 Samuel", "1sam" to "1 Samuel", "1sa" to "1 Samuel", "1s" to "1 Samuel", "first samuel" to "1 Samuel",
            "2 samuel" to "2 Samuel", "2samuel" to "2 Samuel", "2 sam" to "2 Samuel", "2sam" to "2 Samuel", "2sa" to "2 Samuel", "2s" to "2 Samuel", "second samuel" to "2 Samuel",
            "1 kings" to "1 Kings", "1kings" to "1 Kings", "1 kgs" to "1 Kings", "1kgs" to "1 Kings", "1ki" to "1 Kings", "1k" to "1 Kings", "first kings" to "1 Kings",
            "2 kings" to "2 Kings", "2kings" to "2 Kings", "2 kgs" to "2 Kings", "2kgs" to "2 Kings", "2ki" to "2 Kings", "2k" to "2 Kings", "second kings" to "2 Kings",
            "1 chronicles" to "1 Chronicles", "1chronicles" to "1 Chronicles", "1 chron" to "1 Chronicles", "1chron" to "1 Chronicles", "1chr" to "1 Chronicles", "1ch" to "1 Chronicles", "first chronicles" to "1 Chronicles",
            "2 chronicles" to "2 Chronicles", "2chronicles" to "2 Chronicles", "2 chron" to "2 Chronicles", "2chron" to "2 Chronicles", "2chr" to "2 Chronicles", "2ch" to "2 Chronicles", "second chronicles" to "2 Chronicles",
            "ezra" to "Ezra", "ezr" to "Ezra", "ez" to "Ezra",
            "nehemiah" to "Nehemiah", "neh" to "Nehemiah", "ne" to "Nehemiah",
            "esther" to "Esther", "esth" to "Esther", "est" to "Esther", "es" to "Esther",
            "job" to "Job", "jb" to "Job",
            "psalms" to "Psalms", "psalm" to "Psalms", "ps" to "Psalms", "psa" to "Psalms", "pslm" to "Psalms",
            "proverbs" to "Proverbs", "proverb" to "Proverbs", "prov" to "Proverbs", "pro" to "Proverbs", "pr" to "Proverbs",
            "ecclesiastes" to "Ecclesiastes", "eccl" to "Ecclesiastes", "ecc" to "Ecclesiastes", "ec" to "Ecclesiastes",
            "song of solomon" to "Song of Solomon", "song of songs" to "Song of Solomon", "song" to "Song of Solomon", "songs" to "Song of Solomon", "sos" to "Song of Solomon", "sng" to "Song of Solomon",
            "isaiah" to "Isaiah", "isa" to "Isaiah", "is" to "Isaiah",
            "jeremiah" to "Jeremiah", "jer" to "Jeremiah", "je" to "Jeremiah",
            "lamentations" to "Lamentations", "lam" to "Lamentations", "la" to "Lamentations",
            "ezekiel" to "Ezekiel", "ezek" to "Ezekiel", "ezk" to "Ezekiel", "eze" to "Ezekiel",
            "daniel" to "Daniel", "dan" to "Daniel", "da" to "Daniel", "dn" to "Daniel",
            "hosea" to "Hosea", "hos" to "Hosea", "ho" to "Hosea",
            "joel" to "Joel", "jol" to "Joel", "jl" to "Joel",
            "amos" to "Amos", "amo" to "Amos", "am" to "Amos",
            "obadiah" to "Obadiah", "obad" to "Obadiah", "oba" to "Obadiah", "ob" to "Obadiah",
            "jonah" to "Jonah", "jon" to "Jonah", "jnh" to "Jonah",
            "micah" to "Micah", "mic" to "Micah", "mc" to "Micah",
            "nahum" to "Nahum", "nah" to "Nahum", "nam" to "Nahum", "na" to "Nahum",
            "habakkuk" to "Habakkuk", "hab" to "Habakkuk", "hb" to "Habakkuk",
            "zephaniah" to "Zephaniah", "zeph" to "Zephaniah", "zep" to "Zephaniah", "zp" to "Zephaniah",
            "haggai" to "Haggai", "hag" to "Haggai", "hg" to "Haggai",
            "zechariah" to "Zechariah", "zech" to "Zechariah", "zec" to "Zechariah", "zc" to "Zechariah",
            "malachi" to "Malachi", "mal" to "Malachi", "ml" to "Malachi",
            "matthew" to "Matthew", "matt" to "Matthew", "mat" to "Matthew", "mt" to "Matthew",
            "mark" to "Mark", "mrk" to "Mark", "mk" to "Mark",
            "luke" to "Luke", "luk" to "Luke", "lk" to "Luke",
            "john" to "John", "jhn" to "John", "jn" to "John",
            "acts" to "Acts", "act" to "Acts", "ac" to "Acts",
            "romans" to "Romans", "rom" to "Romans", "ro" to "Romans", "rm" to "Romans",
            "1 corinthians" to "1 Corinthians", "1corinthians" to "1 Corinthians", "1 cor" to "1 Corinthians", "1cor" to "1 Corinthians", "1co" to "1 Corinthians", "1corinth" to "1 Corinthians",
            "2 corinthians" to "2 Corinthians", "2corinthians" to "2 Corinthians", "2 cor" to "2 Corinthians", "2cor" to "2 Corinthians", "2co" to "2 Corinthians",
            "galatians" to "Galatians", "gal" to "Galatians", "ga" to "Galatians",
            "ephesians" to "Ephesians", "eph" to "Ephesians", "ep" to "Ephesians",
            "philippians" to "Philippians", "phil" to "Philippians", "php" to "Philippians", "pp" to "Philippians",
            "colossians" to "Colossians", "col" to "Colossians", "cl" to "Colossians",
            "1 thessalonians" to "1 Thessalonians", "1thessalonians" to "1 Thessalonians", "1 thess" to "1 Thessalonians", "1thess" to "1 Thessalonians", "1th" to "1 Thessalonians",
            "2 thessalonians" to "2 Thessalonians", "2thessalonians" to "2 Thessalonians", "2 thess" to "2 Thessalonians", "2thess" to "2 Thessalonians", "2th" to "2 Thessalonians",
            "1 timothy" to "1 Timothy", "1timothy" to "1 Timothy", "1 tim" to "1 Timothy", "1tim" to "1 Timothy", "1ti" to "1 Timothy",
            "2 timothy" to "2 Timothy", "2timothy" to "2 Timothy", "2 tim" to "2 Timothy", "2tim" to "2 Timothy", "2ti" to "2 Timothy",
            "titus" to "Titus", "tit" to "Titus", "ti" to "Titus",
            "philemon" to "Philemon", "philem" to "Philemon", "phm" to "Philemon", "pm" to "Philemon",
            "hebrews" to "Hebrews", "heb" to "Hebrews", "he" to "Hebrews",
            "james" to "James", "jas" to "James", "jm" to "James",
            "1 peter" to "1 Peter", "1peter" to "1 Peter", "1 pet" to "1 Peter", "1pet" to "1 Peter", "1pe" to "1 Peter",
            "2 peter" to "2 Peter", "2peter" to "2 Peter", "2 pet" to "2 Peter", "2pet" to "2 Peter", "2pe" to "2 Peter",
            "1 john" to "1 John", "1john" to "1 John", "1 jhn" to "1 John", "1jhn" to "1 John", "1 jn" to "1 John",
            "2 john" to "2 John", "2john" to "2 John", "2 jhn" to "2 John", "2jhn" to "2 John", "2 jn" to "2 John",
            "3 john" to "3 John", "3john" to "3 John", "3 jhn" to "3 John", "3jhn" to "3 John", "3 jn" to "3 John",
            "jude" to "Jude", "jud" to "Jude", "jd" to "Jude",
            "revelation" to "Revelation", "revelations" to "Revelation", "rev" to "Revelation", "re" to "Revelation"
        )

        fun resolveBookName(token: String): String {
            val clean = token.lowercase().trim()
            return BOOK_ALIASES[clean] ?: token
        }
    }

    @Volatile
    private var cachedTablePrefix: String? = null

    @Volatile
    private var cachedLastUpdateTime: Long = -1L

    init {
        copyBaseTextFromAssetsIfMissing("bsb.db")
    }

    fun setTranslation(translation: String) {
        val assetName = when (translation.uppercase()) {
            "ASV" -> "asv.db"
            "KJV" -> "kjv.db"
            else -> "bsb.db"
        }
        val baseFile = context.getDatabasePath("basetext.db")
        val db = writableDatabase
        if (isBaseTextCurrent(assetName)) {
            attachBaseTextDb(db)
            cachedTablePrefix = null
            ensureIndexesExist(db)
            return
        }
        try {
            baseFile.parentFile?.mkdirs()
            context.assets.open(assetName).use { input ->
                FileOutputStream(baseFile).use { output ->
                    input.copyTo(output)
                }
            }
            writeBaseTextMarker(assetName)
            try {
                db.execSQL("DETACH DATABASE basetext")
            } catch (e: Exception) {}
            db.execSQL("ATTACH DATABASE '${baseFile.absolutePath}' AS basetext")
        } catch (e: Exception) {
            // Ignore
        }
        cachedTablePrefix = null
        ensureIndexesExist(db)
    }

    private fun copyBaseTextFromAssetsIfMissing(assetName: String = "bsb.db") {
        if (isBaseTextCurrent(assetName)) return
        val baseFile = context.getDatabasePath("basetext.db")
        if (!baseFile.exists()) {
            try {
                baseFile.parentFile?.mkdirs()
                context.assets.open(assetName).use { input ->
                    FileOutputStream(baseFile).use { output ->
                        input.copyTo(output)
                    }
                }
                writeBaseTextMarker(assetName)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    private fun markerValue(assetName: String): String {
        if (cachedLastUpdateTime == -1L) {
            cachedLastUpdateTime = try {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
            } catch (e: Exception) {
                0L
            }
        }
        return "$assetName:$cachedLastUpdateTime"
    }

    private fun isBaseTextCurrent(assetName: String): Boolean {
        val baseFile = context.getDatabasePath("basetext.db")
        if (!baseFile.exists() || baseFile.length() == 0L) return false
        val marker = File(baseFile.parentFile, BASE_TEXT_MARKER)
        if (!marker.exists()) return false
        return try {
            marker.readText().trim() == markerValue(assetName)
        } catch (e: Exception) {
            false
        }
    }

    private fun writeBaseTextMarker(assetName: String) {
        try {
            val baseFile = context.getDatabasePath("basetext.db")
            File(baseFile.parentFile, BASE_TEXT_MARKER).writeText(markerValue(assetName))
        } catch (e: Exception) {
            // Ignore
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        createUserTables(db)
        attachBaseTextDb(db)
        ensureIndexesExist(db)
    }

    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        createUserTables(db)
        attachBaseTextDb(db)
        ensureIndexesExist(db)
    }

    private fun attachBaseTextDb(db: SQLiteDatabase) {
        val baseFile = context.getDatabasePath("basetext.db")
        if (baseFile.exists()) {
            try {
                db.execSQL("ATTACH DATABASE '${baseFile.absolutePath}' AS basetext")
            } catch (e: Exception) {
                // Already attached
            }
        }
    }

    private fun ensureIndexesExist(db: SQLiteDatabase) {
        val prefix = getTablePrefix()
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS basetext.idx_${prefix}_verses_bcv ON ${prefix}_verses(book_id, chapter, verse)")
            db.execSQL("CREATE INDEX IF NOT EXISTS basetext.idx_${prefix}_verses_bc ON ${prefix}_verses(book_id, chapter)")
            db.execSQL("CREATE INDEX IF NOT EXISTS basetext.idx_${prefix}_books_name ON ${prefix}_books(name)")
        } catch (e: Exception) {
            // Ignore if basetext is attached read-only
        }
        try {
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_ref ON $TABLE_NOTES(reference)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_notes_ts ON $TABLE_NOTES(timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_bookmarks_ts ON $TABLE_BOOKMARKS(timestamp)")
            db.execSQL("CREATE INDEX IF NOT EXISTS idx_highlights_ref ON $TABLE_HIGHLIGHTS(reference)")
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun createUserTables(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NOTES (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                reference TEXT,
                content TEXT,
                timestamp INTEGER
            )
            """
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_BOOKMARKS (
                reference TEXT PRIMARY KEY,
                label TEXT,
                color TEXT,
                timestamp INTEGER
            )
            """
        )
        try { db.execSQL("ALTER TABLE $TABLE_BOOKMARKS ADD COLUMN label TEXT") } catch (e: Exception) {}
        try { db.execSQL("ALTER TABLE $TABLE_BOOKMARKS ADD COLUMN color TEXT") } catch (e: Exception) {}

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_HIGHLIGHTS (
                reference TEXT PRIMARY KEY,
                color TEXT,
                timestamp INTEGER
            )
            """
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        createUserTables(db)
    }

    private fun getTablePrefix(): String {
        cachedTablePrefix?.let { return it }
        val prefix = try {
            val db = readableDatabase
            val c = db.rawQuery("SELECT name FROM basetext.sqlite_master WHERE type='table' AND name LIKE '%_books'", null)
            var value = "BSB"
            c.use {
                if (it.moveToFirst()) {
                    value = it.getString(0).substringBefore("_books")
                }
            }
            value
        } catch (e: Exception) {
            "BSB"
        }
        cachedTablePrefix = prefix
        return prefix
    }

    // Books navigation
    fun getBooks(isOldTestament: Boolean): List<BookRecord> {
        val list = mutableListOf<BookRecord>()
        val db = readableDatabase
        val prefix = getTablePrefix()
        val sql = if (isOldTestament) {
            "SELECT id, name FROM basetext.${prefix}_books WHERE id BETWEEN 1 AND 39 ORDER BY id ASC"
        } else {
            "SELECT id, name FROM basetext.${prefix}_books WHERE id BETWEEN 40 AND 66 ORDER BY id ASC"
        }
        val cursor = db.rawQuery(sql, null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(BookRecord(it.getInt(0), it.getString(1)))
            }
        }
        return list
    }

    fun getChaptersForBook(bookId: Int): List<Int> {
        val list = mutableListOf<Int>()
        val db = readableDatabase
        val prefix = getTablePrefix()
        val cursor = db.rawQuery("SELECT DISTINCT chapter FROM basetext.${prefix}_verses WHERE book_id = ? ORDER BY chapter ASC", arrayOf(bookId.toString()))
        cursor.use {
            while (it.moveToNext()) {
                list.add(it.getInt(0))
            }
        }
        return list
    }

    fun getVersesForChapter(bookId: Int, chapter: Int): List<VerseRecord> {
        val list = mutableListOf<VerseRecord>()
        val db = readableDatabase
        val prefix = getTablePrefix()
        val cursor = db.rawQuery(
            "SELECT b.name, v.chapter, v.verse, v.text FROM basetext.${prefix}_verses v JOIN basetext.${prefix}_books b ON v.book_id = b.id WHERE v.book_id = ? AND v.chapter = ? ORDER BY v.verse ASC",
            arrayOf(bookId.toString(), chapter.toString())
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(VerseRecord(it.getString(0), it.getInt(1), it.getInt(2), it.getString(3).trim(), prefix))
            }
        }
        return list
    }

    fun getVersesByReferences(referencesStr: String): List<VerseRecord> {
        val refs = referencesStr.split(",").map { it.trim() }.filter { it.isNotBlank() }
        val results = mutableListOf<VerseRecord>()
        for (ref in refs) {
            val verses = searchVersesEnhanced(ref)
            results.addAll(verses)
        }
        return results.distinctBy { "${it.bookId}_${it.chapter}_${it.verse}" }
    }

    fun getRandomVerse(bookName: String? = null): VerseRecord? {
        val db = readableDatabase
        val prefix = getTablePrefix()
        val query: String
        val args: Array<String>?
        if (bookName.isNullOrBlank() || bookName.equals("All Books", ignoreCase = true)) {
            query = "SELECT b.name, v.chapter, v.verse, v.text FROM basetext.${prefix}_verses v JOIN basetext.${prefix}_books b ON v.book_id = b.id ORDER BY RANDOM() LIMIT 1"
            args = null
        } else {
            query = "SELECT b.name, v.chapter, v.verse, v.text FROM basetext.${prefix}_verses v JOIN basetext.${prefix}_books b ON v.book_id = b.id WHERE LOWER(b.name) = LOWER(?) ORDER BY RANDOM() LIMIT 1"
            args = arrayOf(bookName.trim())
        }
        val cursor = db.rawQuery(query, args)
        cursor.use {
            if (it.moveToNext()) {
                return VerseRecord(it.getString(0), it.getInt(1), it.getInt(2), it.getString(3).trim(), prefix)
            }
        }
        return null
    }

    // Notes
    fun insertNote(reference: String, content: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("reference", reference)
            put("content", content)
            put("timestamp", System.currentTimeMillis())
        }
        db.insert(TABLE_NOTES, null, values)
    }

    fun getNotes(): List<NoteRecord> {
        val list = mutableListOf<NoteRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, reference, content, timestamp FROM $TABLE_NOTES ORDER BY timestamp DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(NoteRecord(it.getLong(0), it.getString(1), it.getString(2), it.getLong(3)))
            }
        }
        return list
    }

    fun deleteNote(id: Long) {
        val db = writableDatabase
        db.delete(TABLE_NOTES, "id = ?", arrayOf(id.toString()))
    }

    fun updateNote(id: Long, content: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("content", content)
            put("timestamp", System.currentTimeMillis())
        }
        db.update(TABLE_NOTES, values, "id = ?", arrayOf(id.toString()))
    }

    // Bookmarks
    fun saveCustomBookmark(reference: String, label: String, color: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("reference", reference)
            put("label", label.ifBlank { reference })
            put("color", color)
            put("timestamp", System.currentTimeMillis())
        }
        db.insertWithOnConflict(TABLE_BOOKMARKS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    fun appendVerseToBookmark(bookmarkKey: String, newVerseRef: String) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT reference FROM $TABLE_BOOKMARKS WHERE reference = ?", arrayOf(bookmarkKey))
        cursor.use {
            if (it.moveToFirst()) {
                val currentRef = it.getString(0)
                if (!currentRef.contains(newVerseRef)) {
                    val updatedRef = "$currentRef, $newVerseRef"
                    val values = ContentValues().apply {
                        put("reference", updatedRef)
                        put("timestamp", System.currentTimeMillis())
                    }
                    db.update(TABLE_BOOKMARKS, values, "reference = ?", arrayOf(bookmarkKey))
                }
            }
        }
    }

    fun renameBookmark(referenceKey: String, newLabel: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("label", newLabel)
            put("timestamp", System.currentTimeMillis())
        }
        db.update(TABLE_BOOKMARKS, values, "reference = ?", arrayOf(referenceKey))
    }

    fun deleteBookmark(referenceKey: String) {
        val db = writableDatabase
        db.delete(TABLE_BOOKMARKS, "reference = ?", arrayOf(referenceKey))
    }

    fun removeVerseFromBookmark(bookmarkKey: String, verseRefToRemove: String) {
        removeVersesFromBookmark(bookmarkKey, setOf(verseRefToRemove))
    }

    fun removeVersesFromBookmark(bookmarkKey: String, verseRefsToRemove: Set<String>) {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT reference, label, color FROM $TABLE_BOOKMARKS WHERE reference = ?", arrayOf(bookmarkKey))
        cursor.use {
            if (it.moveToFirst()) {
                val currentRef = cursor.getString(0)
                val label = if (!cursor.isNull(1)) cursor.getString(1) else currentRef
                val color = if (!cursor.isNull(2)) cursor.getString(2) else "#FFD700"

                val verses = getVersesByReferences(currentRef)
                val remainingVerses = verses.filter { v ->
                    val vRef = "${v.bookId} ${v.chapter}:${v.verse}"
                    !verseRefsToRemove.any { rem -> rem.equals(vRef.trim(), ignoreCase = true) }
                }

                db.delete(TABLE_BOOKMARKS, "reference = ?", arrayOf(bookmarkKey))

                if (remainingVerses.isNotEmpty()) {
                    val newRef = formatVersesReference(remainingVerses)
                    val values = ContentValues().apply {
                        put("reference", newRef)
                        put("label", if (label == currentRef) newRef else label)
                        put("color", color)
                        put("timestamp", System.currentTimeMillis())
                    }
                    db.insertWithOnConflict(TABLE_BOOKMARKS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
                }
            }
        }
    }

    private fun formatVersesReference(verses: List<VerseRecord>): String {
        if (verses.isEmpty()) return ""
        val grouped = verses.groupBy { "${it.bookId} ${it.chapter}" }
        val parts = mutableListOf<String>()
        for ((bookChapter, vList) in grouped) {
            val sortedNums = vList.map { it.verse }.sorted()
            val ranges = mutableListOf<String>()
            var start = sortedNums.first()
            var prev = start
            for (i in 1 until sortedNums.size) {
                val curr = sortedNums[i]
                if (curr == prev + 1) {
                    prev = curr
                } else {
                    if (start == prev) ranges.add("$start") else ranges.add("$start-$prev")
                    start = curr
                    prev = curr
                }
            }
            if (start == prev) ranges.add("$start") else ranges.add("$start-$prev")
            parts.add("$bookChapter:${ranges.joinToString(", ")}")
        }
        return parts.joinToString(", ")
    }

    fun toggleBookmark(reference: String, isBookmarked: Boolean) {
        val db = writableDatabase
        if (isBookmarked) {
            val values = ContentValues().apply {
                put("reference", reference)
                put("label", reference)
                put("color", "#FFD700")
                put("timestamp", System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_BOOKMARKS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        } else {
            db.delete(TABLE_BOOKMARKS, "reference = ?", arrayOf(reference))
        }
    }

    fun getBookmarks(): List<BookmarkRecord> {
        val list = mutableListOf<BookmarkRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT reference, label, color, timestamp FROM $TABLE_BOOKMARKS ORDER BY timestamp DESC", null)
        cursor.use {
            while (it.moveToNext()) {
                val ref = it.getString(0)
                val label = if (!it.isNull(1)) it.getString(1) else ref
                val color = if (!it.isNull(2)) it.getString(2) else "#FFD700"
                val ts = it.getLong(3)
                list.add(BookmarkRecord(ref, label, color, ts))
            }
        }
        return list
    }

    // Highlights
    fun saveHighlight(reference: String, color: String) {
        val db = writableDatabase
        if (color.isBlank()) {
            db.delete(TABLE_HIGHLIGHTS, "reference = ?", arrayOf(reference))
        } else {
            val values = ContentValues().apply {
                put("reference", reference)
                put("color", color)
                put("timestamp", System.currentTimeMillis())
            }
            db.insertWithOnConflict(TABLE_HIGHLIGHTS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        }
    }

    fun getHighlights(): List<HighlightRecord> {
        val list = mutableListOf<HighlightRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT reference, color, timestamp FROM $TABLE_HIGHLIGHTS", null)
        cursor.use {
            while (it.moveToNext()) {
                list.add(HighlightRecord(it.getString(0), it.getString(1), it.getLong(2)))
            }
        }
        return list
    }

    // Suggestions
    fun getSuggestions(query: String): List<String> {
        if (query.trim().isEmpty()) return TOPIC_SUGGESTIONS.take(8)

        val trimmed = query.trim().lowercase()
        val suggestions = mutableListOf<String>()

        for (topic in TOPIC_SUGGESTIONS) {
            if (topic.lowercase().contains(trimmed)) {
                suggestions.add(topic)
            }
        }

        val db = readableDatabase
        val prefix = getTablePrefix()
        try {
            val cursor = db.rawQuery("SELECT name FROM basetext.${prefix}_books WHERE name LIKE ? LIMIT 5", arrayOf("%$query%"))
            cursor.use {
                while (it.moveToNext()) {
                    suggestions.add("${it.getString(0)} 1")
                }
            }
        } catch (e: Exception) {
            // fallback
        }

        return suggestions.distinct().take(10)
    }

    private fun fetchVerseRange(bookName: String, chapter: Int, startVerse: Int, endVerse: Int): List<VerseRecord> {
        val list = mutableListOf<VerseRecord>()
        val db = readableDatabase
        val prefix = getTablePrefix()
        val sql = """
            SELECT b.name, v.chapter, v.verse, v.text 
            FROM basetext.${prefix}_verses v 
            JOIN basetext.${prefix}_books b ON v.book_id = b.id 
            WHERE (b.name LIKE ? OR b.name = ?) AND v.chapter = ? AND v.verse BETWEEN ? AND ?
            ORDER BY v.verse ASC
        """
        val cursor = db.rawQuery(sql, arrayOf("%$bookName%", bookName, chapter.toString(), startVerse.toString(), endVerse.toString()))
        cursor.use {
            while (it.moveToNext()) {
                list.add(VerseRecord(it.getString(0), it.getInt(1), it.getInt(2), it.getString(3).trim(), prefix))
            }
        }
        return list
    }

    // Search Verses
    fun searchVersesEnhanced(query: String, version: String = "BSB"): List<VerseRecord> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) return emptyList()

        val db = readableDatabase
        val prefix = getTablePrefix()
        val list = mutableListOf<VerseRecord>()

        val rangeRegex = Regex("^([1-3]?\\s*[a-zA-Z]+)\\s+(\\d+):(\\d+)\\s*(?:-|–)\\s*(\\d+)$", RegexOption.IGNORE_CASE)
        val rangeMatch = rangeRegex.find(trimmedQuery)
        if (rangeMatch != null) {
            val rawBook = rangeMatch.groupValues[1]
            val chapterStr = rangeMatch.groupValues[2]
            val startVerseStr = rangeMatch.groupValues[3]
            val endVerseStr = rangeMatch.groupValues[4]

            val bookName = resolveBookName(rawBook)
            val chapterNum = chapterStr.toIntOrNull()
            val startV = startVerseStr.toIntOrNull()
            val endV = endVerseStr.toIntOrNull()

            if (chapterNum != null && startV != null && endV != null) {
                try {
                    return fetchVerseRange(bookName, chapterNum, startV, endV)
                } catch (e: Exception) {
                    // fallthrough
                }
            }
        }

        val refRegex = Regex("^([1-3]?\\s*[a-zA-Z]+)\\s+(\\d+)(?:[:\\s]+(\\d+))?$", RegexOption.IGNORE_CASE)
        val match = refRegex.find(trimmedQuery)

        if (match != null) {
            val rawBook = match.groupValues[1]
            val chapterStr = match.groupValues[2]
            val verseStr = match.groupValues[3]

            val bookName = resolveBookName(rawBook)
            val chapterNum = chapterStr.toIntOrNull()
            val verseNum = verseStr.toIntOrNull()

            if (chapterNum != null) {
                try {
                    val sql: String
                    val args: Array<String>

                    if (verseNum != null) {
                        sql = """
                            SELECT b.name, v.chapter, v.verse, v.text 
                            FROM basetext.${prefix}_verses v 
                            JOIN basetext.${prefix}_books b ON v.book_id = b.id 
                            WHERE (b.name LIKE ? OR b.name = ?) AND v.chapter = ? AND v.verse = ?
                        """
                        args = arrayOf("%$rawBook%", bookName, chapterNum.toString(), verseNum.toString())
                    } else {
                        sql = """
                            SELECT b.name, v.chapter, v.verse, v.text 
                            FROM basetext.${prefix}_verses v 
                            JOIN basetext.${prefix}_books b ON v.book_id = b.id 
                            WHERE (b.name LIKE ? OR b.name = ?) AND v.chapter = ? 
                            ORDER BY v.verse ASC
                        """
                        args = arrayOf("%$rawBook%", bookName, chapterNum.toString())
                    }

                    val refCursor = db.rawQuery(sql, args)
                    refCursor.use {
                        while (it.moveToNext()) {
                            list.add(VerseRecord(it.getString(0), it.getInt(1), it.getInt(2), it.getString(3), prefix))
                        }
                    }

                    if (list.isNotEmpty()) return list
                } catch (e: Exception) {
                    // fallthrough
                }
            }
        }

        val tokens = trimmedQuery.split("\\s+".toRegex()).filter { it.isNotBlank() }
        val whereClause = StringBuilder("1=1")
        val selectionArgs = mutableListOf<String>()

        for (token in tokens) {
            val resolvedBook = resolveBookName(token)
            whereClause.append(" AND (v.text LIKE ? OR b.name LIKE ? OR b.name LIKE ?)")
            val param = "%$token%"
            val resolvedParam = "%$resolvedBook%"
            selectionArgs.add(param)
            selectionArgs.add(param)
            selectionArgs.add(resolvedParam)
        }

        try {
            val sql = """
                SELECT b.name, v.chapter, v.verse, v.text 
                FROM basetext.${prefix}_verses v 
                JOIN basetext.${prefix}_books b ON v.book_id = b.id 
                WHERE $whereClause 
                LIMIT 50
            """
            val cursor = db.rawQuery(sql, selectionArgs.toTypedArray())

            cursor.use {
                while (it.moveToNext()) {
                    list.add(VerseRecord(it.getString(0), it.getInt(1), it.getInt(2), it.getString(3), prefix))
                }
            }
        } catch (e: Exception) {
            // fallthrough
        }

        if (list.isEmpty() && tokens.size > 1) {
            val orWhereClause = StringBuilder("(")
            val orArgs = mutableListOf<String>()
            tokens.forEachIndexed { index, token ->
                if (index > 0) orWhereClause.append(" OR ")
                orWhereClause.append("(v.text LIKE ? OR b.name LIKE ?)")
                val p = "%$token%"
                orArgs.add(p)
                orArgs.add(p)
            }
            orWhereClause.append(")")

            try {
                val orSql = """
                    SELECT b.name, v.chapter, v.verse, v.text 
                    FROM basetext.${prefix}_verses v 
                    JOIN basetext.${prefix}_books b ON v.book_id = b.id 
                    WHERE $orWhereClause 
                    LIMIT 50
                """
                val orCursor = db.rawQuery(orSql, orArgs.toTypedArray())
                orCursor.use {
                    while (it.moveToNext()) {
                        list.add(VerseRecord(it.getString(0), it.getInt(1), it.getInt(2), it.getString(3), prefix))
                    }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }

        return list
    }
}
