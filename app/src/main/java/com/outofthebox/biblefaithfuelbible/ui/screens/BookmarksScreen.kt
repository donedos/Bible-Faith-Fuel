package com.outofthebox.biblefaithfuelbible.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.outofthebox.biblefaithfuelbible.data.local.BookmarkRecord
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var filterQuery by remember { mutableStateOf("") }
    var selectedOptionsBookmark by remember { mutableStateOf<BookmarkRecord?>(null) }

    val filteredBookmarks = remember(uiState.bookmarks, filterQuery) {
        if (filterQuery.isBlank()) {
            uiState.bookmarks
        } else {
            uiState.bookmarks.filter {
                it.label.contains(filterQuery, ignoreCase = true) ||
                        it.reference.contains(filterQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header Row
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
                    text = "🔖 Saved Bookmarks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = { viewModel.selectDrawerTab("reader") }) {
                Text("← Bible Reader")
            }
        }

        // Search / Filter Input
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("Filter saved bookmarks...", style = MaterialTheme.typography.bodyMedium) },
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
                    BookmarkCard(
                        bookmark = bookmark,
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

    // Long-Press Options Dialog for Bookmarks (Rename / Delete)
    if (selectedOptionsBookmark != null) {
        val targetBm = selectedOptionsBookmark!!

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

                    Button(
                        onClick = {
                            val bm = targetBm
                            selectedOptionsBookmark = null
                            viewModel.openEditingBookmarkDialog(bm)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✏️ Rename Bookmark")
                    }

                    Button(
                        onClick = {
                            val refKey = targetBm.reference
                            selectedOptionsBookmark = null
                            viewModel.deleteBookmark(refKey)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗑️ Delete Bookmark")
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
}

@Composable
fun BookmarkCard(
    bookmark: BookmarkRecord,
    viewModel: BibleViewModel,
    onReadVerses: (String) -> Unit,
    onLongPress: (BookmarkRecord) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val verses = remember(bookmark.reference) {
        viewModel.getVersesByReferences(bookmark.reference)
    }

    val badgeColor = remember(bookmark.color) {
        try {
            Color(android.graphics.Color.parseColor(bookmark.color))
        } catch (e: Exception) {
            Color(0xFFFFD700)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(bookmark.reference) {
                detectTapGestures(
                    onTap = { isExpanded = !isExpanded },
                    onLongPress = { onLongPress(bookmark) }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Badge + Title + Chevron Indicator
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

                Text(
                    text = if (isExpanded) "▲" else "▼",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Text(
                text = "Verses: ${bookmark.reference}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )

            // Expanded List of Bookmarked Verse Text
            if (isExpanded) {
                HorizontalDivider()
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (verses.isEmpty()) {
                        Text(
                            text = bookmark.reference,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        verses.forEach { verse ->
                            val verseRef = "${verse.bookId} ${verse.chapter}:${verse.verse}"
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onReadVerses(verseRef) }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
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
                        }
                    }
                }
            }

            Button(
                onClick = { onReadVerses(bookmark.reference.split(",").first().trim()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📖 Read in Bible Reader")
            }
        }
    }
}
