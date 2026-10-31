package com.outofthebox.biblefaithfuelbible.ui.screens

import android.content.Intent
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleUiState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outofthebox.biblefaithfuelbible.R
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord
import com.outofthebox.biblefaithfuelbible.ui.components.AdBanner
import com.outofthebox.biblefaithfuelbible.ui.theme.PageTurnAnimation
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

private data class ChapterPageState(
    val bookId: Int,
    val bookName: String,
    val chapter: Int,
    val verses: List<VerseRecord>
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FloatingSelectionBar(
    uiState: BibleUiState,
    viewModel: BibleViewModel,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var showColorPicker by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val allVerseNums = uiState.chapterVerses.map { it.verse }.toSet()
    val isAllSelected = allVerseNums.isNotEmpty() && uiState.selectedVerseNums.containsAll(allVerseNums)

    val highlightPresetColors = listOf(
        "#FFF59D" to "Yellow",
        "#A5D6A7" to "Green",
        "#90CAF9" to "Blue",
        "#F48FB1" to "Pink",
        "#CE93D8" to "Purple",
        "#FFCC80" to "Orange"
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = if (isLandscape) {
            modifier.width(135.dp)
        } else {
            modifier.fillMaxWidth()
        }
    ) {
        if (isLandscape) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Header: Selected Verse Count + Close (✕) Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "${uiState.selectedVerseNums.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearVerseSelection() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close selection",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = viewModel.getSelectedVersesReference(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Highlight Color Picker in Landscape
                if (showColorPicker) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        highlightPresetColors.forEach { (hex, _) ->
                            val parseColor = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parseColor)
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                                    .clickable {
                                        viewModel.saveHighlightForSelectedVerses(hex)
                                        showColorPicker = false
                                    }
                            )
                        }
                        TextButton(
                            onClick = {
                                viewModel.saveHighlightForSelectedVerses("")
                                showColorPicker = false
                            }
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                // Vertical scrollable action items
                VerticalActionItem(
                    emoji = "📋",
                    label = "Copy",
                    onClick = {
                        val refStr = viewModel.getSelectedVersesReference()
                        val formattedText = viewModel.getSelectedVersesFormattedText()
                        clipboardManager.setText(AnnotatedString(formattedText))
                        Toast.makeText(context, "Copied $refStr to clipboard", Toast.LENGTH_SHORT).show()
                    }
                )

                VerticalActionItem(
                    emoji = "🔖",
                    label = "Bookmarks",
                    onClick = {
                        val refStr = viewModel.getSelectedVersesReference()
                        viewModel.openBookmarkDialog(refStr)
                    }
                )

                VerticalActionItem(
                    icon = Icons.Default.Edit,
                    label = "Notes",
                    onClick = {
                        val refStr = viewModel.getSelectedVersesReference()
                        viewModel.openVerseActionDialog(refStr)
                    }
                )

                VerticalActionItem(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = {
                        val formattedText = viewModel.getSelectedVersesFormattedText()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, formattedText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Bible Verses")
                        context.startActivity(shareIntent)
                    }
                )

                VerticalActionItem(
                    emoji = "🎨",
                    label = "Highlight",
                    isActive = showColorPicker,
                    onClick = {
                        showColorPicker = !showColorPicker
                        if (showColorPicker && isLandscape) {
                            coroutineScope.launch {
                                scrollState.animateScrollTo(0)
                            }
                        }
                    }
                )

                VerticalActionItem(
                    emoji = if (isAllSelected) "☒️" else "☑️",
                    label = if (isAllSelected) "Deselect All" else "Select All",
                    onClick = {
                        viewModel.toggleSelectAllVerses()
                    }
                )

                VerticalActionItem(
                    emoji = if (uiState.isSpeaking) "⏹️" else "🔊",
                    label = if (uiState.isSpeaking) "Stop" else "Listen",
                    isActive = uiState.isSpeaking,
                    onClick = {
                        if (uiState.isSpeaking) {
                            viewModel.stopSpeech()
                        } else {
                            viewModel.speakSelectedVerses()
                        }
                    }
                )
            }
        } else {
            // Horizontal layout for Portrait mode
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header: Selected Verse Count + Reference Label + Close (✕) Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "${uiState.selectedVerseNums.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = viewModel.getSelectedVersesReference(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = { viewModel.clearVerseSelection() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close selection",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Highlight Preset Colors Row
                if (showColorPicker) {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        highlightPresetColors.forEach { (hex, _) ->
                            val parseColor = Color(android.graphics.Color.parseColor(hex))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(parseColor)
                                    .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
                                    .clickable {
                                        viewModel.saveHighlightForSelectedVerses(hex)
                                        showColorPicker = false
                                    }
                            )
                        }
                        TextButton(
                            onClick = {
                                viewModel.saveHighlightForSelectedVerses("")
                                showColorPicker = false
                            }
                        ) {
                            Text("Clear", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Horizontal Action Icons: Copy, Bookmarks, Notes, Share, Highlight
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // 1. Copy
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val refStr = viewModel.getSelectedVersesReference()
                                val formattedText = viewModel.getSelectedVersesFormattedText()
                                clipboardManager.setText(AnnotatedString(formattedText))
                                Toast.makeText(context, "Copied $refStr to clipboard", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("📋", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Copy",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 2. Bookmarks
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val refStr = viewModel.getSelectedVersesReference()
                                viewModel.openBookmarkDialog(refStr)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🔖", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Bookmarks",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 3. Notes
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val refStr = viewModel.getSelectedVersesReference()
                                viewModel.openVerseActionDialog(refStr)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Notes",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Notes",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 4. Share
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val formattedText = viewModel.getSelectedVersesFormattedText()
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, formattedText)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Share Bible Verses")
                                context.startActivity(shareIntent)
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Share",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 5. Highlight
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                showColorPicker = !showColorPicker
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("🎨", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "Highlight",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (showColorPicker) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 6. Select All / Deselect All
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.toggleSelectAllVerses()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(if (isAllSelected) "☒️" else "☑️", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (isAllSelected) "Deselect All" else "Select All",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 7. TTS / Listen
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (uiState.isSpeaking) {
                                    viewModel.stopSpeech()
                                } else {
                                    viewModel.speakSelectedVerses()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(if (uiState.isSpeaking) "⏹️" else "🔊", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (uiState.isSpeaking) "Stop" else "Listen",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isSpeaking) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalActionItem(
    label: String,
    onClick: () -> Unit,
    emoji: String? = null,
    icon: ImageVector? = null,
    isActive: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    ) {
        if (emoji != null) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun highlightSearchQuery(text: String, query: String): AnnotatedString {
    if (query.isBlank()) return AnnotatedString(text)
    return buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase().trim()
        if (lowerQuery.isEmpty() || !lowerText.contains(lowerQuery)) {
            append(text)
        } else {
            var startIndex = 0
            while (true) {
                val index = lowerText.indexOf(lowerQuery, startIndex)
                if (index < 0) {
                    append(text.substring(startIndex))
                    break
                }
                if (index > startIndex) {
                    append(text.substring(startIndex, index))
                }
                val matchEnd = index + lowerQuery.length
                withStyle(
                    style = SpanStyle(
                        background = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(text.substring(index, matchEnd))
                }
                startIndex = matchEnd
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BibleScreen(
    viewModel: BibleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showSearchField by remember { mutableStateOf(false) }

    val isBackEnabled = drawerState.isOpen ||
            showSearchField ||
            uiState.verseActionDialogRef != null ||
            uiState.bookmarkDialogVerseRef != null ||
            uiState.editingBookmark != null ||
            uiState.isSettingsDialogOpen ||
            uiState.selectedDrawerTab != "reader" ||
            uiState.selectedBook != null

    BackHandler(enabled = isBackEnabled) {
        when {
            drawerState.isOpen -> {
                coroutineScope.launch { drawerState.close() }
            }
            showSearchField -> {
                showSearchField = false
                viewModel.updateSearchQuery("")
            }
            uiState.verseActionDialogRef != null -> {
                viewModel.closeVerseActionDialog()
            }
            uiState.bookmarkDialogVerseRef != null -> {
                viewModel.closeBookmarkDialog()
            }
            uiState.editingBookmark != null -> {
                viewModel.closeEditingBookmarkDialog()
            }
            uiState.isSettingsDialogOpen -> {
                viewModel.closeSettingsDialog()
            }
            uiState.selectedDrawerTab != "reader" -> {
                viewModel.selectDrawerTab("reader")
            }
            uiState.selectedChapter != null -> {
                viewModel.goBackToChapters()
            }
            uiState.selectedBook != null -> {
                viewModel.goBackToBooks()
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = drawerState.isOpen, // Enable drawer gestures when open so user can swipe left to hide it
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(300.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Top Section: Header & Menu Items
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                Text(
                                    text = "Bible Faith Fuel",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Offline Holy Bible (${uiState.selectedTranslation})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            HorizontalDivider()

                            NavigationDrawerItem(
                                label = {
                                    Text(
                                        text = if (uiState.isAdFree) "✨ NO ADS (Active)" else "🚀 NO ADS",
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (uiState.isAdFree) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                selected = uiState.selectedDrawerTab == "no_ads",
                                onClick = {
                                    viewModel.selectDrawerTab("no_ads")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("📖 Bible Reader", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "reader",
                                onClick = {
                                    viewModel.selectDrawerTab("reader")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("🔖 Bookmarks (${uiState.bookmarks.size})", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "bookmarks",
                                onClick = {
                                    viewModel.selectDrawerTab("bookmarks")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("🎨 Highlights (${uiState.highlights.filter { it.color.isNotBlank() }.size})", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "highlights",
                                onClick = {
                                    viewModel.selectDrawerTab("highlights")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("📝 Notes (${uiState.notes.size})", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "notes",
                                onClick = {
                                    viewModel.selectDrawerTab("notes")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("🖼️ Widgets", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "widgets",
                                onClick = {
                                    viewModel.selectDrawerTab("widgets")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("⚙️ Settings", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "settings",
                                onClick = {
                                    viewModel.selectDrawerTab("settings")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )
                        }

                        // Bottom Section: Dark Mode Toggle
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            HorizontalDivider()

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(if (uiState.isDarkTheme) "🌙" else "☀️", style = MaterialTheme.typography.titleLarge)
                                    Text(
                                        text = if (uiState.isDarkTheme) "Dark Mode" else "Light Mode",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                Switch(
                                    checked = uiState.isDarkTheme,
                                    onCheckedChange = { viewModel.updateDarkTheme(it) }
                                )
                            }
                        }
                    }
                }
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                // Route between Standalone Bookmarks Screen, Notes Screen, Settings Screen, and Reader View
                when (uiState.selectedDrawerTab) {
                    "bookmarks" -> {
                        BookmarksScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    "highlights" -> {
                        HighlightsScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    "notes" -> {
                        NotesScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    "widgets" -> {
                        WidgetsScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    "no_ads" -> {
                        NoAdsScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    "settings" -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onOpenDrawer = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                    else -> {
                        // Main App Content Container
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .statusBarsPadding()
                                .padding(
                                    start = 16.dp,
                                    top = 16.dp,
                                    bottom = 16.dp,
                                    end = if (isLandscape && uiState.selectedVerseNums.isNotEmpty() && uiState.selectedDrawerTab == "reader") 150.dp else 16.dp
                                ),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 1. Top Book Header Controls: Fixed Right Action Controls (AA & 🔍)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Upper Left: Hamburger ☰ + One-Liner Location Pill with Marquee for Long Book Names
                                Row(
                                    modifier = Modifier.weight(1f, fill = false),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                        Text(
                                            text = "☰",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(24.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.clickable {
                                            viewModel.selectDrawerTab("reader")
                                            viewModel.goBackToBooks()
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (uiState.selectedBook != null && uiState.selectedChapter != null)
                                                    "${uiState.selectedBook!!.name} ${uiState.selectedChapter}"
                                                else if (uiState.selectedBook != null)
                                                    uiState.selectedBook!!.name
                                                else "Select Book",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .weight(1f, fill = false)
                                                    .basicMarquee()
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = uiState.selectedTranslation,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    maxLines = 1,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Upper Right Action Controls (Permanently Locked on Right Edge)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    if (uiState.selectedDrawerTab == "reader" && uiState.selectedBook != null && uiState.selectedChapter != null && !uiState.isPickingVerse) {
                                        val targetIconSize = when (uiState.fontSizeSp) {
                                            16 -> 20.dp
                                            18 -> 24.dp
                                            22 -> 29.dp
                                            else -> 35.dp
                                        }
                                        val animatedIconSize by animateDpAsState(
                                            targetValue = targetIconSize,
                                            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                                            label = "FontSizeIconSize"
                                        )
                                        IconButton(onClick = { viewModel.cycleFontSize() }) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_fontsize),
                                                contentDescription = "Font Size",
                                                modifier = Modifier.size(animatedIconSize)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { showSearchField = !showSearchField }) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_search),
                                            contentDescription = "Search",
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }
                                }
                            }

                            // Collapsible Search Input Field
                            if (showSearchField || uiState.searchQuery.isNotBlank()) {
                                OutlinedTextField(
                                    value = uiState.searchQuery,
                                    onValueChange = { viewModel.updateSearchQuery(it) },
                                    placeholder = { Text("Search ...", style = MaterialTheme.typography.bodyMedium) },
                                    singleLine = true,
                                    trailingIcon = {
                                        if (uiState.searchQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                                Text("✕", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = Color.Transparent
                                    )
                                )

                                if (uiState.suggestions.isNotEmpty()) {
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(uiState.suggestions) { suggestion ->
                                            SuggestionChip(
                                                onClick = { viewModel.onSuggestionSelected(suggestion) },
                                                label = { Text(suggestion) },
                                                colors = SuggestionChipDefaults.suggestionChipColors(
                                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                    labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Standard Bible Reader View
                            if (uiState.searchQuery.isNotBlank() && uiState.offlineSearchResults.isNotEmpty()) {
                                Text(
                                    text = "Search Results (${uiState.offlineSearchResults.size}):",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                val bookmarkedRefs = remember(uiState.bookmarks) {
                                    uiState.bookmarks.mapTo(HashSet()) { it.reference }
                                }
                                LazyColumn(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = uiState.offlineSearchResults,
                                        key = { verse -> "${verse.bookId}_${verse.chapter}_${verse.verse}_${verse.version}" }
                                    ) { verse ->
                                        val ref = "${verse.bookId} ${verse.chapter}:${verse.verse}"
                                        val isBookmarked = bookmarkedRefs.contains(ref)

                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { viewModel.navigateToVerse(ref) },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(text = ref, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    IconButton(onClick = { viewModel.openBookmarkDialog(ref) }) {
                                                        Text(if (isBookmarked) "🔖" else "📑")
                                                    }
                                                }
                                                Text(
                                                    text = highlightSearchQuery(text = verse.text, query = uiState.searchQuery),
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontFamily = uiState.selectedFontOption.toFontFamily()
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (uiState.selectedBook == null) {
                                    // Step 1: Testament Switcher + Books Grid
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = { viewModel.selectTestament(true) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (uiState.isOldTestament) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                contentColor = if (uiState.isOldTestament) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            elevation = null
                                        ) {
                                            Text("Old Testament", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                        }
                                        Button(
                                            onClick = { viewModel.selectTestament(false) },
                                            modifier = Modifier.weight(1f),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (!uiState.isOldTestament) MaterialTheme.colorScheme.primary else Color.Transparent,
                                                contentColor = if (!uiState.isOldTestament) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            elevation = null
                                        ) {
                                            Text("New Testament", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                                        }
                                    }

                                    Text(
                                        text = if (uiState.isOldTestament) "Select Old Testament Book:" else "Select New Testament Book:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(2),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(uiState.books) { book ->
                                            Button(
                                                onClick = { viewModel.selectBook(book) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                                            ) {
                                                Text(
                                                    text = book.name,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.basicMarquee()
                                                )
                                            }
                                        }
                                    }
                                } else if (uiState.selectedChapter == null) {
                                    // Step 2: Chapter Buttons Grid
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(onClick = { viewModel.goBackToBooks() }) {
                                            Text("← All Books")
                                        }
                                    }

                                    Text(
                                        text = "Select Chapter in ${uiState.selectedBook!!.name}:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(uiState.chapters) { chapter ->
                                            OutlinedButton(
                                                onClick = { viewModel.selectChapter(chapter) },
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp)
                                            ) {
                                                Text(text = "$chapter", fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                    }
                                } else if (uiState.isPickingVerse) {
                                    // Step 3: Verse Picker Grid (Book > Chapter > verses)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        TextButton(onClick = { viewModel.goBackToChapters() }) {
                                            Text("← Chapters")
                                        }
                                        Button(onClick = { viewModel.readFullChapter() }) {
                                            Text("📖 Read Full Chapter")
                                        }
                                    }

                                    Text(
                                        text = "Select Verse in ${uiState.selectedBook!!.name} Chapter ${uiState.selectedChapter}:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(4),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        items(uiState.chapterVerses) { verse ->
                                            OutlinedButton(
                                                onClick = { viewModel.selectExactVerse(verse.verse) },
                                                modifier = Modifier.fillMaxWidth(),
                                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 8.dp)
                                            ) {
                                                Text(text = "${verse.verse}", fontWeight = FontWeight.Bold, maxLines = 1)
                                            }
                                        }
                                    }
                                } else {
                                    // Step 4: CONTINUOUS BOOK READING LAYOUT WITH PAGE FLIP ANIMATION
                                    AnimatedContent(
                                        targetState = ChapterPageState(
                                            bookId = uiState.selectedBook!!.id,
                                            bookName = uiState.selectedBook!!.name,
                                            chapter = uiState.selectedChapter!!,
                                            verses = uiState.chapterVerses
                                        ),
                                        transitionSpec = {
                                            val dir = uiState.chapterTransitionDirection
                                            when (uiState.pageTurnAnimation) {
                                                PageTurnAnimation.NONE -> {
                                                    (EnterTransition.None togetherWith ExitTransition.None) using null
                                                }
                                                PageTurnAnimation.PAPER_FLIP -> {
                                                    if (dir == 0) {
                                                        (fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))) using null
                                                    } else {
                                                        val duration = 450
                                                        val enter = slideInHorizontally(
                                                            animationSpec = tween(duration, easing = FastOutSlowInEasing)
                                                        ) { 0 }
                                                        val exit = slideOutHorizontally(
                                                            animationSpec = tween(duration, easing = FastOutSlowInEasing)
                                                        ) { 0 }
                                                        ((enter togetherWith exit).apply {
                                                            targetContentZIndex = -uiState.chapterTransitionSerial.toFloat()
                                                        }) using null
                                                    }
                                                }
                                                PageTurnAnimation.DEFAULT -> {
                                                    if (dir > 0) {
                                                        (slideInHorizontally(animationSpec = tween(350)) { width -> width } + fadeIn(animationSpec = tween(350)))
                                                            .togetherWith(slideOutHorizontally(animationSpec = tween(350)) { width -> -width } + fadeOut(animationSpec = tween(350)))
                                                    } else if (dir < 0) {
                                                        (slideInHorizontally(animationSpec = tween(350)) { width -> -width } + fadeIn(animationSpec = tween(350)))
                                                            .togetherWith(slideOutHorizontally(animationSpec = tween(350)) { width -> width } + fadeOut(animationSpec = tween(350)))
                                                    } else {
                                                        fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                                                    }
                                                }
                                            }
                                        },
                                        label = "PageFlipAnimation",
                                        modifier = Modifier.weight(1f)
                                    ) { page ->
                                        val isIncoming = transition.targetState == EnterExitState.Visible
                                        val pageListState = rememberLazyListState()
                                        val direction = uiState.chapterTransitionDirection
                                        val isPaperFlip = uiState.pageTurnAnimation == PageTurnAnimation.PAPER_FLIP && direction != 0
                                        val pageVisibility by transition.animateFloat(
                                            transitionSpec = { tween(450, easing = FastOutSlowInEasing) },
                                            label = "paperFlipVisibility"
                                        ) { state -> if (state == EnterExitState.Visible) 1f else 0f }
                                        val startAngle = if (direction > 0) 90f else -90f
                                        val rotationY = when {
                                            !isPaperFlip -> 0f
                                            isIncoming -> startAngle * (1f - pageVisibility)
                                            else -> -startAngle * (1f - pageVisibility)
                                        }
                                        val pivotX = if (direction > 0) 0f else 1f
                                        val shadeAlpha = if (isPaperFlip) (kotlin.math.abs(rotationY) / 90f) * 0.35f else 0f

                                        LaunchedEffect(page.bookId, page.chapter, uiState.shouldScrollToVerse, uiState.selectedVerseNum) {
                                            if (isIncoming && uiState.shouldScrollToVerse && !uiState.isPickingVerse) {
                                                val vNum = uiState.selectedVerseNum
                                                if (vNum != null) {
                                                    val targetIdx = page.verses.indexOfFirst { it.verse == vNum }
                                                    if (targetIdx >= 0) {
                                                        pageListState.scrollToItem(targetIdx + 1)
                                                    }
                                                }
                                                viewModel.clearScrollToVerseFlag()
                                            }
                                        }

                                        LazyColumn(
                                            state = pageListState,
                                            contentPadding = PaddingValues(
                                                bottom = if (uiState.selectedVerseNums.isNotEmpty() && !isLandscape) 280.dp else 40.dp
                                            ),
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    this.rotationY = rotationY
                                                    cameraDistance = 16f * density
                                                    transformOrigin = TransformOrigin(pivotX, 0.5f)
                                                }
                                                .drawWithContent {
                                                    drawContent()
                                                    if (shadeAlpha > 0f) {
                                                        val shade = if (direction > 0) {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(
                                                                    Color.Black.copy(alpha = shadeAlpha),
                                                                    Color.Transparent
                                                                ),
                                                                startX = 0f,
                                                                endX = size.width
                                                            )
                                                        } else {
                                                            Brush.horizontalGradient(
                                                                colors = listOf(
                                                                    Color.Transparent,
                                                                    Color.Black.copy(alpha = shadeAlpha)
                                                                ),
                                                                startX = 0f,
                                                                endX = size.width
                                                            )
                                                        }
                                                        drawRect(brush = shade)
                                                    }
                                                }
                                                .pointerInput(page.bookId to page.chapter) {
                                                    var swipeTotal = 0f
                                                    detectHorizontalDragGestures(
                                                        onDragStart = {
                                                            swipeTotal = 0f
                                                        },
                                                        onDragEnd = {
                                                            if (swipeTotal < -60f) {
                                                                viewModel.nextChapter()
                                                            } else if (swipeTotal > 60f) {
                                                                viewModel.previousChapter()
                                                            }
                                                            swipeTotal = 0f
                                                        },
                                                        onDragCancel = {
                                                            swipeTotal = 0f
                                                        },
                                                        onHorizontalDrag = { change, dragAmount ->
                                                            change.consume()
                                                            swipeTotal += dragAmount
                                                        }
                                                    )
                                                },
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            // Header Item: Book Title
                                            item {
                                                Text(
                                                    text = "${page.bookName} ${page.chapter}",
                                                    style = MaterialTheme.typography.headlineLarge.copy(
                                                        fontStyle = FontStyle.Italic,
                                                        fontSize = 28.sp
                                                    ),
                                                    fontWeight = FontWeight.Bold,
                                                    fontFamily = uiState.selectedFontOption.toFontFamily(),
                                                    modifier = Modifier.padding(vertical = 8.dp)
                                                )
                                            }

                                            // Verse Items (Applying Custom Selected Font)
                                            items(page.verses, key = { "${it.bookId}_${it.chapter}_${it.verse}" }) { verse ->
                                                val isSelected = uiState.selectedVerseNums.contains(verse.verse)
                                                val verseRef = "${verse.bookId} ${verse.chapter}:${verse.verse}"

                                                val highlightRecord = uiState.highlights.find { it.reference == verseRef }
                                                val highlightColor = if (highlightRecord != null && highlightRecord.color.isNotBlank()) {
                                                    try {
                                                        Color(android.graphics.Color.parseColor(highlightRecord.color))
                                                    } catch (e: Exception) {
                                                        Color.Transparent
                                                    }
                                                } else {
                                                    Color.Transparent
                                                }

                                                val hasHighlight = highlightColor != Color.Transparent

                                                val surfaceColor = when {
                                                    isSelected && hasHighlight -> highlightColor.copy(alpha = 0.8f)
                                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                                    hasHighlight -> highlightColor
                                                    else -> Color.Transparent
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = surfaceColor,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp)
                                                        .then(
                                                            if (isSelected && hasHighlight) {
                                                                Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                                            } else {
                                                                Modifier
                                                            }
                                                        )
                                                        .pointerInput(verse.verse) {
                                                            detectTapGestures(
                                                                onTap = { viewModel.toggleVerseSelection(verse.verse) },
                                                                onLongPress = { viewModel.toggleVerseSelection(verse.verse) }
                                                            )
                                                        }
                                                ) {
                                                    Text(
                                                        text = buildAnnotatedString {
                                                            withStyle(
                                                                style = SpanStyle(
                                                                    fontSize = (uiState.fontSizeSp * 0.7).sp,
                                                                    fontWeight = FontWeight.Bold,
                                                                    baselineShift = BaselineShift.Superscript,
                                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else if (hasHighlight) Color(0xFF333333) else MaterialTheme.colorScheme.primary
                                                                )
                                                            ) {
                                                                append("${verse.verse} ")
                                                            }
                                                            append("${verse.text} ")
                                                        },
                                                        style = MaterialTheme.typography.bodyLarge.copy(
                                                            fontSize = uiState.fontSizeSp.sp,
                                                            lineHeight = (uiState.fontSizeSp * 1.5).sp,
                                                            fontFamily = uiState.selectedFontOption.toFontFamily()
                                                        ),
                                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else if (hasHighlight) Color(0xFF1C1C1E) else MaterialTheme.colorScheme.onBackground,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            if (uiState.selectedVerseNums.isNotEmpty() && !isLandscape) {
                                                item {
                                                    Spacer(modifier = Modifier.height(280.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Floating Bottom/Side Container (Google Photos Style Floating Bar + Page Navigation Arrows)
                        if (uiState.selectedDrawerTab == "reader" && uiState.selectedBook != null && uiState.selectedChapter != null && !uiState.isPickingVerse) {
                            if (isLandscape) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .statusBarsPadding()
                                        .padding(16.dp)
                                ) {
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = uiState.selectedVerseNums.isNotEmpty(),
                                        enter = slideInHorizontally { it } + fadeIn(),
                                        exit = slideOutHorizontally { it } + fadeOut(),
                                        modifier = Modifier
                                            .align(Alignment.CenterEnd)
                                            .padding(vertical = 8.dp)
                                    ) {
                                        FloatingSelectionBar(
                                            uiState = uiState,
                                            viewModel = viewModel,
                                            isLandscape = true
                                        )
                                    }

                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .fillMaxWidth(if (uiState.selectedVerseNums.isNotEmpty()) 0.75f else 1f),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FloatingActionButton(
                                            onClick = { viewModel.previousChapter() },
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                                            shape = CircleShape,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Text("‹", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                        }

                                        FloatingActionButton(
                                            onClick = { viewModel.nextChapter() },
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                                            shape = CircleShape,
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Text("›", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AnimatedVisibility(
                                        visible = uiState.selectedVerseNums.isNotEmpty(),
                                        enter = slideInVertically { it / 2 } + fadeIn(),
                                        exit = slideOutVertically { it / 2 } + fadeOut()
                                    ) {
                                        FloatingSelectionBar(
                                            uiState = uiState,
                                            viewModel = viewModel,
                                            isLandscape = false
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FloatingActionButton(
                                            onClick = { viewModel.previousChapter() },
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                                            shape = CircleShape,
                                            modifier = Modifier.size(52.dp)
                                        ) {
                                            Text("‹", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                        }

                                        FloatingActionButton(
                                            onClick = { viewModel.nextChapter() },
                                            containerColor = Color.Transparent,
                                            contentColor = MaterialTheme.colorScheme.onSurface,
                                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 0.dp),
                                            shape = CircleShape,
                                            modifier = Modifier.size(52.dp)
                                ) {
                                            Text("›", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                }
                if (!uiState.isAdFree) {
                    AdBanner()
                }
            }
        }

    // Verse Action Dialog (Bookmarks, Highlights, Notes)
    if (uiState.verseActionDialogRef != null) {
        val verseRef = uiState.verseActionDialogRef!!
        var localNoteInput by remember { mutableStateOf("") }
        val presetColors = listOf(
            "#FFF59D" to "Yellow",
            "#A5D6A7" to "Green",
            "#90CAF9" to "Blue",
            "#F48FB1" to "Pink",
            "#CE93D8" to "Purple",
            "#FFCC80" to "Orange"
        )

        Dialog(onDismissRequest = { viewModel.closeVerseActionDialog() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📖 Verse Options",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = { viewModel.closeVerseActionDialog() }) {
                            Text("✕")
                        }
                    }

                    Text(
                        text = verseRef,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    HorizontalDivider()

                    // 1. Bookmark Action
                    Button(
                        onClick = {
                            viewModel.closeVerseActionDialog()
                            viewModel.openBookmarkDialog(verseRef)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    ) {
                        Text("🔖 Bookmark Verse", fontWeight = FontWeight.Bold)
                    }

                    // 2. Highlight Action
                    Text(text = "🎨 Highlight Verse:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetColors.forEach { (hex, _) ->
                            val parseColor = Color(android.graphics.Color.parseColor(hex))
                            val existingHighlight = uiState.highlights.find { h -> h.reference == verseRef }
                            val isSelected = existingHighlight?.color == hex

                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(parseColor)
                                    .border(
                                        width = if (isSelected) 3.dp else 0.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.saveHighlight(verseRef, hex)
                                    }
                            )
                        }
                    }
                    TextButton(
                        onClick = { viewModel.saveHighlight(verseRef, "") },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text("Clear Highlight", color = MaterialTheme.colorScheme.error)
                    }

                    HorizontalDivider()

                    // 3. Add Notes Action
                    Text(text = "📝 Add Personal Note:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = localNoteInput,
                        onValueChange = { localNoteInput = it },
                        label = { Text("Note content...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = {
                            if (localNoteInput.isNotBlank()) {
                                viewModel.addNote(verseRef, localNoteInput)
                                localNoteInput = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Note")
                    }
                }
            }
        }
    }

    // Save / Append Bookmark Dialog (Option to Create New OR Add to Existing Bookmark)
    if (uiState.bookmarkDialogVerseRef != null) {
        val verseRef = uiState.bookmarkDialogVerseRef!!
        var isCreateNewMode by remember { mutableStateOf(true) }
        var customLabel by remember { mutableStateOf(verseRef) }
        var selectedColorHex by remember { mutableStateOf("#FFD700") }
        var selectedExistingBookmarkKey by remember { mutableStateOf(uiState.bookmarks.firstOrNull()?.reference ?: "") }

        val presetColors = listOf(
            "#FFD700" to "Gold",
            "#FF9500" to "Orange",
            "#FF2D55" to "Pink",
            "#5856D6" to "Purple",
            "#007AFF" to "Blue",
            "#34C759" to "Green"
        )

        Dialog(onDismissRequest = { viewModel.closeBookmarkDialog() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 520.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "🔖 Save Bookmark",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Verse: $verseRef",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Option Selector: Create New vs Add to Existing
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { isCreateNewMode = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCreateNewMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (isCreateNewMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = null
                        ) {
                            Text("Create New", style = MaterialTheme.typography.labelMedium)
                        }
                        Button(
                            onClick = { isCreateNewMode = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (!isCreateNewMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (!isCreateNewMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = null
                        ) {
                            Text("Add to Existing", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (isCreateNewMode) {
                        OutlinedTextField(
                            value = customLabel,
                            onValueChange = { customLabel = it },
                            label = { Text("Bookmark Label / Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Text(
                            text = "Choose Color:",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            presetColors.forEach { (hex, _) ->
                                val parseColor = Color(android.graphics.Color.parseColor(hex))
                                val isSelected = selectedColorHex == hex

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(parseColor)
                                        .border(
                                            width = if (isSelected) 3.dp else 0.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedColorHex = hex }
                                )
                            }
                        }
                    } else {
                        // Add to Existing Bookmark Selector
                        if (uiState.bookmarks.isNotEmpty()) {
                            Text(
                                text = "Select Existing Bookmark:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            var dropdownExpanded by remember { mutableStateOf(false) }
                            val activeBookmark = uiState.bookmarks.find { it.reference == selectedExistingBookmarkKey } ?: uiState.bookmarks.first()

                            ExposedDropdownMenuBox(
                                expanded = dropdownExpanded,
                                onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                            ) {
                                OutlinedTextField(
                                    value = activeBookmark.label,
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                        .fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    uiState.bookmarks.forEach { bm ->
                                        DropdownMenuItem(
                                            text = { Text(bm.label) },
                                            onClick = {
                                                selectedExistingBookmarkKey = bm.reference
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "No existing bookmarks found. Please use 'Create New'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.closeBookmarkDialog() }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (isCreateNewMode) {
                                    viewModel.saveCustomBookmark(verseRef, customLabel, selectedColorHex)
                                } else {
                                    if (selectedExistingBookmarkKey.isNotBlank()) {
                                        viewModel.appendVerseToExistingBookmark(selectedExistingBookmarkKey, verseRef)
                                    }
                                }
                            }
                        ) {
                            Text(if (isCreateNewMode) "Save Bookmark" else "Append Verse")
                        }
                    }
                }
            }
        }
    }

    // Rename Bookmark Dialog
    if (uiState.editingBookmark != null) {
        val targetBookmark = uiState.editingBookmark!!
        var renameInput by remember { mutableStateOf(targetBookmark.label) }

        Dialog(onDismissRequest = { viewModel.closeEditingBookmarkDialog() }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .heightIn(max = 400.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "✏️ Rename Bookmark",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = renameInput,
                        onValueChange = { renameInput = it },
                        label = { Text("Bookmark Title / Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { viewModel.closeEditingBookmarkDialog() }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (renameInput.isNotBlank()) {
                                    viewModel.renameBookmark(targetBookmark.reference, renameInput)
                                }
                            }
                        ) {
                            Text("Save")
                        }
                    }
                }
            }
        }
    }
}

}
