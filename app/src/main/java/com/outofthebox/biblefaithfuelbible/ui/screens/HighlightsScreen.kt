package com.outofthebox.biblefaithfuelbible.ui.screens

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
import com.outofthebox.biblefaithfuelbible.data.local.HighlightRecord
import com.outofthebox.biblefaithfuelbible.data.local.VerseRecord
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighlightsScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSearchField by remember { mutableStateOf(false) }
    var filterQuery by remember { mutableStateOf("") }
    var isSortAscending by remember { mutableStateOf(false) }
    var selectedOptionsHighlight by remember { mutableStateOf<HighlightRecord?>(null) }

    val filteredHighlights = remember(uiState.highlights, uiState.highlightVerses, filterQuery, isSortAscending) {
        val nonBlank = uiState.highlights.filter { it.color.isNotBlank() }
        val baseList = if (filterQuery.isBlank()) {
            nonBlank
        } else {
            nonBlank.filter { highlight ->
                highlight.reference.contains(filterQuery, ignoreCase = true) ||
                        uiState.highlightVerses[highlight.reference].orEmpty()
                            .any { v -> v.text.contains(filterQuery, ignoreCase = true) }
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
        // Top Header Row with Search Toggle Icon
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
                    text = "Highlights (${filteredHighlights.size})",
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
                        contentDescription = "Search Highlights",
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }

        // Collapsible Search / Filter Input
        if (showSearchField || filterQuery.isNotBlank()) {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                placeholder = { Text("Search highlights...", style = MaterialTheme.typography.bodyMedium) },
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

        if (filteredHighlights.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filterQuery.isBlank())
                        "No highlights saved yet.\nTap or select verses while reading to highlight them!"
                    else "No highlights found matching '$filterQuery'.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredHighlights, key = { it.reference }) { highlight ->
                    HighlightCard(
                        highlight = highlight,
                        verses = uiState.highlightVerses[highlight.reference].orEmpty(),
                        viewModel = viewModel,
                        onReadVerse = { ref ->
                            viewModel.navigateToVerse(ref)
                        },
                        onLongPress = { hl ->
                            selectedOptionsHighlight = hl
                        }
                    )
                }
            }
        }
    }

    // Options Dialog for Highlight (Change Color / Delete)
    if (selectedOptionsHighlight != null) {
        val targetHl = selectedOptionsHighlight!!
        val presetColors = listOf(
            "#FFF59D" to "Yellow",
            "#A5D6A7" to "Green",
            "#90CAF9" to "Blue",
            "#F48FB1" to "Pink",
            "#CE93D8" to "Purple",
            "#FFCC80" to "Orange"
        )

        Dialog(onDismissRequest = { selectedOptionsHighlight = null }) {
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
                        text = "🎨 Highlight Options",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Verse: ${targetHl.reference}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "Change Color:",
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
                            val isSelected = targetHl.color == hex

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
                                        viewModel.saveHighlight(targetHl.reference, hex)
                                        selectedOptionsHighlight = null
                                    }
                            )
                        }
                    }

                    HorizontalDivider()

                    Button(
                        onClick = {
                            viewModel.deleteHighlight(targetHl.reference)
                            selectedOptionsHighlight = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗑️ Clear Highlight")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedOptionsHighlight = null }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HighlightCard(
    highlight: HighlightRecord,
    verses: List<VerseRecord>,
    viewModel: BibleViewModel,
    onReadVerse: (String) -> Unit,
    onLongPress: (HighlightRecord) -> Unit
) {
    val highlightBgColor = remember(highlight.color) {
        try {
            Color(android.graphics.Color.parseColor(highlight.color))
        } catch (e: Exception) {
            Color(0xFFFFF59D)
        }
    }

    val formattedDate = remember(highlight.timestamp) {
        if (highlight.timestamp > 0) {
            val sdf = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            sdf.format(Date(highlight.timestamp))
        } else {
            ""
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(highlight.reference) {
                detectTapGestures(
                    onTap = { onReadVerse(highlight.reference) },
                    onLongPress = { onLongPress(highlight) }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Highlight Color Badge + Verse Reference + Date/Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(highlightBgColor)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                    )
                    Text(
                        text = highlight.reference,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (formattedDate.isNotBlank()) {
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Verse Text Content Preview
            if (verses.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = highlightBgColor.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        verses.forEach { v ->
                            Text(
                                text = "${v.verse} ${v.text}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { onReadVerse(highlight.reference) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("📖 Read in Bible Reader")
            }
        }
    }
}
