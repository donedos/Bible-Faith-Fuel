package com.outofthebox.biblefaithfuelbible.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.outofthebox.biblefaithfuelbible.R
import com.outofthebox.biblefaithfuelbible.data.local.BookmarkRecord
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearchField by remember { mutableStateOf(false) }
    var filterQuery by remember { mutableStateOf("") }
    var isSortAscending by remember { mutableStateOf(false) }
    var selectedOptionsBookmark by remember { mutableStateOf<BookmarkRecord?>(null) }

    var selectedBookmarkRefs by remember { mutableStateOf<Set<String>>(emptySet()) }
    var showMultiDeleteConfirm by remember { mutableStateOf(false) }
    val isMultiSelectMode = selectedBookmarkRefs.isNotEmpty()

    val filteredBookmarks = remember(uiState.bookmarks, filterQuery, isSortAscending) {
        val baseList = if (filterQuery.isBlank()) {
            uiState.bookmarks
        } else {
            uiState.bookmarks.filter {
                it.label.contains(filterQuery, ignoreCase = true) ||
                        it.reference.contains(filterQuery, ignoreCase = true)
            }
        }
        if (isSortAscending) {
            baseList.sortedBy { it.timestamp }
        } else {
            baseList.sortedByDescending { it.timestamp }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Row / Multi-Select Action Bar
        if (isMultiSelectMode) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                text = "${selectedBookmarkRefs.size}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Selected",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        TextButton(
                            onClick = {
                                if (selectedBookmarkRefs.size == filteredBookmarks.size) {
                                    selectedBookmarkRefs = emptySet()
                                } else {
                                    selectedBookmarkRefs = filteredBookmarks.mapTo(HashSet()) { it.reference }
                                }
                            }
                        ) {
                            Text(
                                text = if (selectedBookmarkRefs.size == filteredBookmarks.size) "Deselect All" else "Select All",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }

                        Button(
                            onClick = { showMultiDeleteConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("🗑️ Delete", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                        }

                        IconButton(onClick = { selectedBookmarkRefs = emptySet() }) {
                            Text("✕", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onOpenDrawer) {
                        Text(
                            text = "☰",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Bookmarks (${filteredBookmarks.size})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    TextButton(
                        onClick = { isSortAscending = !isSortAscending },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isSortAscending) "Oldest ⬆" else "Newest ⬇",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(onClick = { showSearchField = !showSearchField }) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = "Search Bookmarks",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

        // Collapsible Search / Filter Input
        if (showSearchField || filterQuery.isNotBlank()) {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("Search bookmarks...", style = MaterialTheme.typography.bodyMedium) },
                singleLine = true,
                trailingIcon = {
                    if (filterQuery.isNotEmpty()) {
                        IconButton(onClick = { filterQuery = "" }) {
                            Text("✕", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }

        if (filteredBookmarks.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filterQuery.isBlank())
                        "No saved bookmarks yet.\nLong-press any verse while reading to bookmark it!"
                    else "No bookmarks found matching '$filterQuery'.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredBookmarks, key = { it.reference }) { bookmark ->
                    val isSelected = selectedBookmarkRefs.contains(bookmark.reference)
                    BookmarkCard(
                        bookmark = bookmark,
                        verses = uiState.bookmarkVerses[bookmark.reference].orEmpty(),
                        isSelected = isSelected,
                        isMultiSelectMode = isMultiSelectMode,
                        onToggleSelect = {
                            selectedBookmarkRefs = if (isSelected) {
                                selectedBookmarkRefs - bookmark.reference
                            } else {
                                selectedBookmarkRefs + bookmark.reference
                            }
                        },
                        viewModel = viewModel,
                        onReadVerses = { ref ->
                            viewModel.navigateToVerse(ref)
                        },
                        onLongPress = { bm ->
                            selectedOptionsBookmark = bm
                        }
                    )
                }
            }
        }
    }

    // Long-Press Options Dialog for Bookmarks (Delete, Deselect All, Rename, Checkbox for multi-select)
    if (selectedOptionsBookmark != null) {
        val targetBm = selectedOptionsBookmark!!
        var isMultiSelectChecked by remember(targetBm) {
            mutableStateOf(selectedBookmarkRefs.contains(targetBm.reference))
        }

        Dialog(onDismissRequest = { selectedOptionsBookmark = null }) {
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
                        text = "🔖 ${targetBm.label}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Verses: ${targetBm.reference}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    // Checkbox to select multiple Bookmarks to delete
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMultiSelectChecked = !isMultiSelectChecked
                                if (isMultiSelectChecked) {
                                    selectedBookmarkRefs = selectedBookmarkRefs + targetBm.reference
                                } else {
                                    selectedBookmarkRefs = selectedBookmarkRefs - targetBm.reference
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isMultiSelectChecked,
                            onCheckedChange = { checked ->
                                isMultiSelectChecked = checked
                                if (checked) {
                                    selectedBookmarkRefs = selectedBookmarkRefs + targetBm.reference
                                } else {
                                    selectedBookmarkRefs = selectedBookmarkRefs - targetBm.reference
                                }
                            }
                        )
                        Text(
                            text = "Select to multi-delete",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // 1. Rename
                    Button(
                        onClick = {
                            val bm = targetBm
                            selectedOptionsBookmark = null
                            viewModel.openEditingBookmarkDialog(bm)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✏️ Rename")
                    }

                    // 2. Deselect All
                    OutlinedButton(
                        onClick = {
                            selectedBookmarkRefs = emptySet()
                            isMultiSelectChecked = false
                            selectedOptionsBookmark = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔄 Deselect All")
                    }

                    // 3. Delete
                    Button(
                        onClick = {
                            if (selectedBookmarkRefs.contains(targetBm.reference) && selectedBookmarkRefs.size > 1) {
                                viewModel.deleteBookmarks(selectedBookmarkRefs)
                                selectedBookmarkRefs = emptySet()
                            } else {
                                viewModel.deleteBookmark(targetBm.reference)
                            }
                            selectedOptionsBookmark = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (selectedBookmarkRefs.size > 1) "🗑️ Delete Selected (${selectedBookmarkRefs.size})" else "🗑️ Delete")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedOptionsBookmark = null }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    // Multi-Delete Confirmation Dialog
    if (showMultiDeleteConfirm) {
        Dialog(onDismissRequest = { showMultiDeleteConfirm = false }) {
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
                        text = "🗑️ Delete ${selectedBookmarkRefs.size} Bookmarks",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Are you sure you want to delete the selected bookmarks? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showMultiDeleteConfirm = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.deleteBookmarks(selectedBookmarkRefs)
                                selectedBookmarkRefs = emptySet()
                                showMultiDeleteConfirm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete All")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BookmarkCard(
    bookmark: BookmarkRecord,
    verses: List<VerseRecord>,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onToggleSelect: () -> Unit,
    onLongPress: (BookmarkRecord) -> Unit,
    viewModel: BibleViewModel,
    onReadVerses: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    var selectedVerseRefs by remember { mutableStateOf<Set<String>>(emptySet()) }
    val isVerseMultiSelectMode = selectedVerseRefs.isNotEmpty()

    val badgeColor = remember(bookmark.color) {
        try {
            Color(android.graphics.Color.parseColor(bookmark.color))
        } catch (e: Exception) {
            Color(0xFFFFD700)
        }
    }

    val formattedDate = remember(bookmark.timestamp) {
        if (bookmark.timestamp > 0) {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            sdf.format(Date(bookmark.timestamp))
        } else {
            ""
        }
    }

    val cardContainerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val cardBorder = if (isSelected) {
        BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (cardBorder != null) Modifier.border(cardBorder, RoundedCornerShape(12.dp)) else Modifier)
            .pointerInput(bookmark.reference, isMultiSelectMode) {
                detectTapGestures(
                    onTap = {
                        if (isMultiSelectMode) {
                            onToggleSelect()
                        } else {
                            isExpanded = !isExpanded
                        }
                    },
                    onLongPress = {
                        if (isMultiSelectMode) {
                            onToggleSelect()
                        } else {
                            onLongPress(bookmark)
                        }
                    }
                )
            },
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (isMultiSelectMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onToggleSelect() }
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(badgeColor)
                    )
                    Text(
                        text = bookmark.label,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (!isMultiSelectMode) {
                    Text(
                        text = if (isExpanded) "▲" else "▼",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Verses: ${bookmark.reference}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (formattedDate.isNotBlank()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expanded List of Bookmarked Verse Text
            if (isExpanded && !isMultiSelectMode) {
                HorizontalDivider()
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Contextual bar if verse multi-select mode is active inside this bookmark
                    if (isVerseMultiSelectMode) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedVerseRefs.size} verses selected",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.removeVersesFromBookmark(bookmark.reference, selectedVerseRefs)
                                            selectedVerseRefs = emptySet()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("🗑️ Delete Selected", style = MaterialTheme.typography.labelSmall)
                                    }
                                    IconButton(
                                        onClick = { selectedVerseRefs = emptySet() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Text("✕", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }

                    if (verses.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bookmark.reference,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.deleteBookmark(bookmark.reference) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("🗑️", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    } else {
                        verses.forEach { verse ->
                            val verseRef = "${verse.bookId} ${verse.chapter}:${verse.verse}"
                            val isVerseSelected = selectedVerseRefs.contains(verseRef)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isVerseSelected) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface,
                                border = if (isVerseSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary) else null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(verseRef) {
                                        detectTapGestures(
                                            onTap = {
                                                if (isVerseMultiSelectMode) {
                                                    selectedVerseRefs = if (isVerseSelected) selectedVerseRefs - verseRef else selectedVerseRefs + verseRef
                                                } else {
                                                    onReadVerses(verseRef)
                                                }
                                            },
                                            onLongPress = {
                                                selectedVerseRefs = if (isVerseSelected) selectedVerseRefs - verseRef else selectedVerseRefs + verseRef
                                            }
                                        )
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (isVerseMultiSelectMode) {
                                            Checkbox(
                                                checked = isVerseSelected,
                                                onCheckedChange = {
                                                    selectedVerseRefs = if (isVerseSelected) selectedVerseRefs - verseRef else selectedVerseRefs + verseRef
                                                }
                                            )
                                        }
                                        Column {
                                            Text(
                                                text = verseRef,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = verse.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }

                                    if (!isVerseMultiSelectMode) {
                                        IconButton(
                                            onClick = { viewModel.removeVerseFromBookmark(bookmark.reference, verseRef) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Text("🗑️", style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (!isMultiSelectMode) {
                Button(
                    onClick = { onReadVerses(bookmark.reference.split(",").first().trim()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("📖 Read in Bible Reader")
                }
            }
        }
    }
}
