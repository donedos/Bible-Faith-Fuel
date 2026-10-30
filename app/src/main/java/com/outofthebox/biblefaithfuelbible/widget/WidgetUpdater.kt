package com.outofthebox.biblefaithfuelbible.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity
import android.widget.RemoteViews
import com.outofthebox.biblefaithfuelbible.MainActivity
import com.outofthebox.biblefaithfuelbible.R
import com.outofthebox.biblefaithfuelbible.data.local.BibleDatabaseHelper
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord

object WidgetUpdater {

    const val PREFS_NAME = "theme_prefs"
    const val ACTION_REFRESH = "com.outofthebox.biblefaithfuelbible.widget.ACTION_REFRESH"
    const val EXTRA_VERSE_REF = "verse_ref"

    private const val KEY_BACKGROUND = "widget_bg"
    private const val KEY_REFRESH_HOURS = "widget_refresh_hours"
    private const val KEY_BOOK_NAME = "widget_book_name"
    private const val KEY_TEXT_ALIGN = "widget_text_align"
    private const val KEY_FONT_OPTION = "widget_font_option"
    private const val KEY_VERSE_TEXT = "widget_verse_text"
    private const val KEY_VERSE_BOOK = "widget_verse_book"
    private const val KEY_VERSE_CHAPTER = "widget_verse_chapter"
    private const val KEY_VERSE_VERSE = "widget_verse_verse"
    private const val KEY_UPDATED_AT = "widget_verse_updated_at"

    private const val REFRESH_REQUEST_CODE = 1001
    private const val OPEN_REQUEST_CODE = 1002

    private data class WidgetStyle(
        val backgroundRes: Int?,
        val backgroundColor: Int,
        val textColor: Int,
        val secondaryTextColor: Int
    )

    fun updateAll(context: Context) {
        Thread {
            runCatching { updateAllNow(context) }
        }.start()
    }

    @Synchronized
    fun updateAllNow(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, VerseWidgetProvider::class.java)
        val widgetIds = manager.getAppWidgetIds(component)
        if (widgetIds.isEmpty()) return

        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val background = sp.getString(KEY_BACKGROUND, "Warm Parchment") ?: "Warm Parchment"
        val refreshHours = sp.getInt(KEY_REFRESH_HOURS, 24).coerceAtLeast(1)
        val bookName = sp.getString(KEY_BOOK_NAME, "All Books") ?: "All Books"
        val alignment = sp.getString(KEY_TEXT_ALIGN, "Center") ?: "Center"
        val translation = sp.getString("bible_translation", "BSB") ?: "BSB"
        val fontOption = sp.getString(KEY_FONT_OPTION, "DEFAULT") ?: "DEFAULT"

        var verse = loadSavedVerse(sp)
        val updatedAt = sp.getLong(KEY_UPDATED_AT, 0L)
        val intervalMs = refreshHours * 60L * 60L * 1000L
        val isExpired = System.currentTimeMillis() - updatedAt >= intervalMs

        if (verse == null || isExpired) {
            val freshVerse = runCatching {
                BibleDatabaseHelper(context).getRandomVerse(bookName)
            }.getOrNull()
            if (freshVerse != null) {
                verse = freshVerse
                saveVerse(sp, freshVerse)
            }
        }

        val currentVerse = verse ?: VerseRecord(
            bookId = "",
            chapter = 0,
            verse = 0,
            text = "Open the app once to load your verse of the day.",
            version = translation
        )

        val style = resolveStyle(context, background)
        widgetIds.forEach { id ->
            val options = runCatching { manager.getAppWidgetOptions(id) }.getOrNull()
            val widthDp = options
                ?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0)
                ?.takeIf { it > 0 }
                ?: 250
            val heightDp = options
                ?.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 0)
                ?.takeIf { it > 0 }
                ?: 110
            val views = buildRemoteViews(
                context = context,
                verse = currentVerse,
                translation = translation,
                alignment = alignment,
                style = style,
                fontOption = fontOption,
                widgetWidthDp = widthDp,
                widgetHeightDp = heightDp
            )
            manager.updateAppWidget(id, views)
        }
    }

    fun pushVerse(context: Context, verse: VerseRecord) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        saveVerse(sp, verse)
        updateAll(context)
    }

    fun clearRefreshTimestamp(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_UPDATED_AT)
            .apply()
    }

    private fun loadSavedVerse(sp: android.content.SharedPreferences): VerseRecord? {
        val text = sp.getString(KEY_VERSE_TEXT, null) ?: return null
        val book = sp.getString(KEY_VERSE_BOOK, "") ?: ""
        val chapter = sp.getInt(KEY_VERSE_CHAPTER, 0)
        val verseNum = sp.getInt(KEY_VERSE_VERSE, 0)
        if (text.isBlank()) return null
        return VerseRecord(book, chapter, verseNum, text, "")
    }

    private fun saveVerse(sp: android.content.SharedPreferences, verse: VerseRecord) {
        sp.edit()
            .putString(KEY_VERSE_TEXT, verse.text)
            .putString(KEY_VERSE_BOOK, verse.bookId)
            .putInt(KEY_VERSE_CHAPTER, verse.chapter)
            .putInt(KEY_VERSE_VERSE, verse.verse)
            .putLong(KEY_UPDATED_AT, System.currentTimeMillis())
            .apply()
    }

    private fun buildRemoteViews(
        context: Context,
        verse: VerseRecord,
        translation: String,
        alignment: String,
        style: WidgetStyle,
        fontOption: String,
        widgetWidthDp: Int,
        widgetHeightDp: Int
    ): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.verse_widget)

        if (style.backgroundRes != null) {
            views.setInt(R.id.widget_root, "setBackgroundResource", style.backgroundRes)
        } else {
            views.setInt(R.id.widget_root, "setBackgroundColor", style.backgroundColor)
        }

        views.setTextColor(R.id.widget_header, style.secondaryTextColor)
        views.setTextColor(R.id.widget_new_verse, style.textColor)
        views.setTextColor(R.id.widget_read, style.textColor)

        val gravity = if (alignment == "Left") {
            Gravity.START or Gravity.CENTER_VERTICAL
        } else {
            Gravity.CENTER
        }
        views.setInt(R.id.widget_header, "setGravity", gravity)

        val verseText = "“${verse.text}”"
        val reference = if (verse.bookId.isBlank()) {
            ""
        } else {
            "— ${verse.bookId} ${verse.chapter}:${verse.verse} ($translation)"
        }

        renderVerseBitmap(
            context = context,
            verseText = verseText,
            reference = reference,
            alignment = alignment,
            textColor = style.textColor,
            fontOption = fontOption,
            widgetWidthDp = widgetWidthDp,
            widgetHeightDp = widgetHeightDp
        )?.let { bitmap ->
            views.setImageViewBitmap(R.id.widget_verse_image, bitmap)
        }
        views.setContentDescription(
            R.id.widget_verse_image,
            listOf(verseText, reference).filter { it.isNotBlank() }.joinToString(" ")
        )

        val refreshIntent = Intent(context, VerseWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            REFRESH_REQUEST_CODE,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val verseReference = if (verse.bookId.isBlank()) {
            null
        } else {
            "${verse.bookId} ${verse.chapter}:${verse.verse}"
        }
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (verseReference != null) {
                putExtra(EXTRA_VERSE_REF, verseReference)
            }
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            OPEN_REQUEST_CODE,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        views.setOnClickPendingIntent(R.id.widget_new_verse, refreshPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_read, openPendingIntent)
        views.setOnClickPendingIntent(R.id.widget_root, openPendingIntent)

        return views
    }

    private fun renderVerseBitmap(
        context: Context,
        verseText: String,
        reference: String,
        alignment: String,
        textColor: Int,
        fontOption: String,
        widgetWidthDp: Int,
        widgetHeightDp: Int
    ): Bitmap? {
        val metrics = context.resources.displayMetrics
        val density = metrics.density
        val scaledDensity = metrics.scaledDensity

        val contentWidthDp = (widgetWidthDp - 28).coerceAtLeast(120)
        val contentHeightDp = (widgetHeightDp - 74).coerceAtLeast(36)
        val widthPx = (contentWidthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (contentHeightDp * density).toInt().coerceAtLeast(1)
        val horizontalInsetPx = (2f * density).toInt()
        val layoutWidthPx = (widthPx - horizontalInsetPx * 2).coerceAtLeast(1)
        val gapPx = if (reference.isBlank()) 0 else (2f * scaledDensity).toInt()
        val lineSpacingExtraPx = 2f * scaledDensity
        val layoutAlignment = if (alignment == "Left") {
            Layout.Alignment.ALIGN_NORMAL
        } else {
            Layout.Alignment.ALIGN_CENTER
        }

        val versePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 14f * scaledDensity
            typeface = widgetTypeface(context, fontOption, bold = false)
            isSubpixelText = true
        }
        val referencePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 11f * scaledDensity
            typeface = widgetTypeface(context, fontOption, bold = true)
            isSubpixelText = true
        }

        val referenceLayout = if (reference.isBlank()) {
            null
        } else {
            buildStaticLayout(
                text = reference,
                paint = referencePaint,
                width = layoutWidthPx,
                alignment = layoutAlignment,
                maxLines = 1,
                lineSpacingExtra = 0f
            )
        }

        val verseMetrics = versePaint.fontMetrics
        val verseLineHeight = (
            verseMetrics.descent - verseMetrics.ascent + lineSpacingExtraPx
            ).coerceAtLeast(1f)
        val availableVerseHeight = (
            heightPx - (referenceLayout?.height ?: 0) - gapPx
            ).toFloat().coerceAtLeast(verseLineHeight)
        val maxVerseLines = (availableVerseHeight / verseLineHeight)
            .toInt()
            .coerceIn(1, 5)

        val verseLayout = buildStaticLayout(
            text = verseText,
            paint = versePaint,
            width = layoutWidthPx,
            alignment = layoutAlignment,
            maxLines = maxVerseLines,
            lineSpacingExtra = lineSpacingExtraPx
        )

        val totalHeight = verseLayout.height +
            gapPx +
            (referenceLayout?.height ?: 0)
        val verticalInsetPx = (2f * density).toInt()
        val bitmapHeight = maxOf(heightPx, totalHeight + verticalInsetPx * 2)
        val bitmap = Bitmap.createBitmap(widthPx, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val startY = ((bitmapHeight - totalHeight) / 2).coerceAtLeast(verticalInsetPx)
        canvas.save()
        canvas.translate(horizontalInsetPx.toFloat(), startY.toFloat())
        verseLayout.draw(canvas)
        referenceLayout?.let {
            canvas.translate(0f, (verseLayout.height + gapPx).toFloat())
            it.draw(canvas)
        }
        canvas.restore()

        return bitmap
    }

    private fun buildStaticLayout(
        text: String,
        paint: TextPaint,
        width: Int,
        alignment: Layout.Alignment,
        maxLines: Int,
        lineSpacingExtra: Float
    ): StaticLayout {
        return StaticLayout.Builder
            .obtain(text, 0, text.length, paint, width)
            .setAlignment(alignment)
            .setIncludePad(false)
            .setLineSpacing(lineSpacingExtra, 1f)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
    }

    private fun widgetTypeface(context: Context, fontOption: String, bold: Boolean): Typeface {
        val resId = when (fontOption) {
            "INTER" -> if (bold) R.font.inter_bold else R.font.inter_italic
            "LIBRE_BASKERVILLE" -> if (bold) {
                R.font.libre_baskerville_bold
            } else {
                R.font.libre_baskerville_italic
            }
            "LORA" -> if (bold) R.font.lora_bold else R.font.lora_italic
            else -> 0
        }
        if (resId != 0) {
            runCatching { context.resources.getFont(resId) }.getOrNull()?.let { return it }
        }
        return Typeface.create(
            Typeface.SANS_SERIF,
            if (bold) Typeface.BOLD else Typeface.ITALIC
        )
    }

    private fun resolveStyle(context: Context, background: String): WidgetStyle {
        return when (background) {
            "Card Surface" -> WidgetStyle(
                backgroundRes = R.drawable.widget_bg_surface,
                backgroundColor = 0,
                textColor = 0xFF1C1C1E.toInt(),
                secondaryTextColor = 0x991C1C1E.toInt()
            )
            "Dark Glass" -> WidgetStyle(
                backgroundRes = R.drawable.widget_bg_dark_glass,
                backgroundColor = 0,
                textColor = 0xFFE2E2E9.toInt(),
                secondaryTextColor = 0xB3E2E2E9.toInt()
            )
            "Sunset Gradient" -> WidgetStyle(
                backgroundRes = R.drawable.widget_bg_sunset,
                backgroundColor = 0,
                textColor = Color.WHITE,
                secondaryTextColor = 0xCCFFFFFF.toInt()
            )
            "Ocean Blue" -> WidgetStyle(
                backgroundRes = R.drawable.widget_bg_ocean,
                backgroundColor = 0,
                textColor = Color.WHITE,
                secondaryTextColor = 0xCCFFFFFF.toInt()
            )
            "Accent Theme" -> {
                val accent = resolveAccentColor(context)
                WidgetStyle(
                    backgroundRes = null,
                    backgroundColor = accent,
                    textColor = contrastingTextColor(accent),
                    secondaryTextColor = withAlpha(contrastingTextColor(accent), 0xCC)
                )
            }
            "Warm Parchment" -> WidgetStyle(
                backgroundRes = R.drawable.widget_bg_parchment,
                backgroundColor = 0,
                textColor = 0xFF3E2723.toInt(),
                secondaryTextColor = 0xB33E2723.toInt()
            )
            else -> WidgetStyle(
                backgroundRes = R.drawable.widget_bg_surface,
                backgroundColor = 0,
                textColor = 0xFF1C1C1E.toInt(),
                secondaryTextColor = 0x991C1C1E.toInt()
            )
        }
    }

    private fun resolveAccentColor(context: Context): Int {
        val accentName = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("ios_accent_name", "BLUE")
        return when (accentName) {
            "PURPLE" -> 0xFF5856D6.toInt()
            "INDIGO" -> 0xFF5E5CE6.toInt()
            "TEAL" -> 0xFF30B0C7.toInt()
            "GREEN" -> 0xFF34C759.toInt()
            "ORANGE" -> 0xFFFF9500.toInt()
            "PINK" -> 0xFFFF2D55.toInt()
            else -> 0xFF007AFF.toInt()
        }
    }

    private fun contrastingTextColor(background: Int): Int {
        val luminance = (0.299 * Color.red(background) +
            0.587 * Color.green(background) +
            0.114 * Color.blue(background)) / 255.0
        return if (luminance > 0.6) 0xFF1C1C1E.toInt() else Color.WHITE
    }

    private fun withAlpha(color: Int, alpha: Int): Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}
