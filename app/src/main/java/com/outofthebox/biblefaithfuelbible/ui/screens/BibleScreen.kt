package com.outofthebox.biblefaithfuelbible.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.outofthebox.biblefaithfuelbible.R
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BibleScreen(
    viewModel: BibleViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val readerListState = rememberLazyListState()

    var showSearchField by remember { mutableStateOf(false) }

    // Scroll selected verse to top ONLY when explicitly requested (e.g. from Verse Picker)
    LaunchedEffect(uiState.selectedVerseNum, uiState.selectedChapter, uiState.shouldScrollToVerse) {
        if (uiState.shouldScrollToVerse && !uiState.isPickingVerse && uiState.selectedBook != null && uiState.selectedChapter != null) {
            val vNum = uiState.selectedVerseNum
            if (vNum != null) {
                val targetIdx = uiState.chapterVerses.indexOfFirst { it.verse == vNum }
                if (targetIdx >= 0) {
                    readerListState.scrollToItem(targetIdx + 1) // +1 for book title header item
                }
            }
            viewModel.clearScrollToVerseFlag()
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
                                label = { Text("📖 Bible Reader", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "reader",
                                onClick = {
                                    viewModel.selectDrawerTab("reader")
                                    coroutineScope.launch { drawerState.close() }
                                }
                            )

                            NavigationDrawerItem(
                                label = { Text("🔖 Saved (${uiState.bookmarks.size})", fontWeight = FontWeight.SemiBold) },
                                selected = uiState.selectedDrawerTab == "bookmarks",
                                onClick = {
                                    viewModel.selectDrawerTab("bookmarks")
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
            Box(modifier = Modifier.fillMaxSize()) {
                // Route between Standalone Bookmarks Screen, Notes Screen, Settings Screen, and Reader View
                when (uiState.selectedDrawerTab) {
                    "bookmarks" -> {
                        BookmarksScreen(
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
                                .padding(16.dp),
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
                                        IconButton(onClick = { viewModel.cycleFontSize() }) {
                                            Image(
                                                painter = painterResource(id = R.drawable.ic_fontsize),
                                                contentDescription = "Font Size",
                                                modifier = Modifier.size(38.dp)
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
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .verticalScroll(scrollState),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    uiState.offlineSearchResults.forEach { verse ->
                                        val ref = "${verse.bookId} ${verse.chapter}:${verse.verse}"
                                        val isBookmarked = uiState.bookmarks.any { it.reference == ref }

                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
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
                                                Text(text = verse.text, style = MaterialTheme.typography.bodyMedium, fontFamily = uiState.selectedFontOption.toFontFamily())
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
                                        targetState = uiState.selectedChapter to uiState.selectedBook?.id,
                                        transitionSpec = {
                                            val dir = uiState.chapterTransitionDirection
                                            if (dir > 0) {
                                                (slideInHorizontally(animationSpec = tween(350)) { width -> width } + fadeIn(animationSpec = tween(350)))
                                                    .togetherWith(slideOutHorizontally(animationSpec = tween(350)) { width -> -width } + fadeOut(animationSpec = tween(350)))
                                            } else if (dir < 0) {
                                                (slideInHorizontally(animationSpec = tween(350)) { width -> -width } + fadeIn(animationSpec = tween(350)))
                                                    .togetherWith(slideOutHorizontally(animationSpec = tween(350)) { width -> width } + fadeOut(animationSpec = tween(350)))
                                            } else {
                                                fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(250))
                                            }
                                        },
                                        label = "PageFlipAnimation",
                                        modifier = Modifier.weight(1f)
                                    ) { _ ->
                                        LazyColumn(
                                            state = readerListState,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .pointerInput(uiState.selectedBook?.id to uiState.selectedChapter) {
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
                                                    text = "${uiState.selectedBook!!.name} ${uiState.selectedChapter}",
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
                                            items(uiState.chapterVerses, key = { "${it.bookId}_${it.chapter}_${it.verse}" }) { verse ->
                                                val isHighlighted = uiState.selectedVerseNum == verse.verse
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

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isHighlighted)
                                                        MaterialTheme.colorScheme.primaryContainer
                                                    else if (hasHighlight)
                                                        highlightColor
                                                    else
                                                        Color.Transparent,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 2.dp)
                                                        .pointerInput(verse.verse) {
                                                            detectTapGestures(
                                                                onTap = { viewModel.selectVerseNumber(verse.verse) },
                                                                onLongPress = { viewModel.openVerseActionDialog(verseRef) }
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
                                                                    color = if (isHighlighted) MaterialTheme.colorScheme.primary else if (hasHighlight) Color(0xFF333333) else MaterialTheme.colorScheme.primary
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
                                                        color = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer else if (hasHighlight) Color(0xFF1C1C1E) else MaterialTheme.colorScheme.onBackground,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            // Action Card Item if Verse Selected
                                            item {
                                                uiState.selectedVerseNum?.let { vNum ->
                                                    val verseObj = uiState.chapterVerses.find { v -> v.verse == vNum }
                                                    if (verseObj != null) {
                                                        val ref = "${verseObj.bookId} ${verseObj.chapter}:${verseObj.verse}"
                                                        var cardNoteInput by remember { mutableStateOf("") }

                                                        Card(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(vertical = 8.dp),
                                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                                        ) {
                                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Text(text = ref, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                                    Button(onClick = { viewModel.openBookmarkDialog(ref) }) {
                                                                        Text("🔖 Save")
                                                                    }
                                                                }

                                                                Text(text = "🎨 Highlight Verse:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                                                val highlightPresetColors = listOf(
                                                                    "#FFF59D" to "Yellow",
                                                                    "#A5D6A7" to "Green",
                                                                    "#90CAF9" to "Blue",
                                                                    "#F48FB1" to "Pink",
                                                                    "#CE93D8" to "Purple",
                                                                    "#FFCC80" to "Orange"
                                                                )
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    highlightPresetColors.forEach { (hex, _) ->
                                                                        val parseColor = Color(android.graphics.Color.parseColor(hex))
                                                                        val existingHighlight = uiState.highlights.find { h -> h.reference == ref }
                                                                        val isSelected = existingHighlight?.color == hex

                                                                        Box(
                                                                            modifier = Modifier
                                                                                .size(32.dp)
                                                                                .clip(CircleShape)
                                                                                .background(parseColor)
                                                                                .border(
                                                                                    width = if (isSelected) 2.dp else 0.dp,
                                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                                    shape = CircleShape
                                                                                )
                                                                                .clickable { viewModel.saveHighlight(ref, hex) }
                                                                        )
                                                                    }
                                                                    TextButton(onClick = { viewModel.saveHighlight(ref, "") }) {
                                                                        Text("Clear", color = MaterialTheme.colorScheme.error)
                                                                    }
                                                                }

                                                                OutlinedTextField(
                                                                    value = cardNoteInput,
                                                                    onValueChange = { cardNoteInput = it },
                                                                    label = { Text("Add personal note for $ref") },
                                                                    modifier = Modifier.fillMaxWidth()
                                                                )
                                                                Button(
                                                                    onClick = {
                                                                        if (cardNoteInput.isNotBlank()) {
                                                                            viewModel.addNote(ref, cardNoteInput)
                                                                            cardNoteInput = ""
                                                                        }
                                                                    }
                                                                ) {
                                                                    Text("Save Note")
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Floating Bottom Book Page Turning Navigation Arrows (< >)
                        if (uiState.selectedDrawerTab == "reader" && uiState.selectedBook != null && uiState.selectedChapter != null && !uiState.isPickingVerse) {
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FloatingActionButton(
                                    onClick = { viewModel.previousChapter() },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    shape = CircleShape,
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Text("‹", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                                }

                                FloatingActionButton(
                                    onClick = { viewModel.nextChapter() },
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
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
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
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
