package com.outofthebox.biblefaithfuelbible.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outofthebox.biblefaithfuelbible.ui.theme.AppFontOption
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetsScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val widgetState = uiState.widgetState

    var bookDropdownExpanded by remember { mutableStateOf(false) }
    var refreshDropdownExpanded by remember { mutableStateOf(false) }

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
                    text = "Widgets",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. LIVE INTERACTIVE WIDGET PREVIEW CARD
            Text(
                text = "📱 Live Widget Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val (bgBrush, textColor, borderColor) = when (widgetState.selectedBackground) {
                "Warm Parchment" -> Triple(
                    Brush.verticalGradient(listOf(Color(0xFFFFF9E6), Color(0xFFFFF3CD))),
                    Color(0xFF3E2723),
                    Color(0xFFE0C9A6)
                )
                "Dark Glass" -> Triple(
                    Brush.verticalGradient(listOf(Color(0xFF232334), Color(0xFF161622))),
                    Color(0xFFE2E2E9),
                    Color(0xFF3F3F5A)
                )
                "Sunset Gradient" -> Triple(
                    Brush.linearGradient(listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B))),
                    Color.White,
                    Color.Transparent
                )
                "Ocean Blue" -> Triple(
                    Brush.linearGradient(listOf(Color(0xFF2B5876), Color(0xFF4E4376))),
                    Color.White,
                    Color.Transparent
                )
                "Accent Theme" -> Triple(
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primaryContainer)),
                    MaterialTheme.colorScheme.onPrimaryContainer,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
                else -> Triple(
                    Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)),
                    MaterialTheme.colorScheme.onSurfaceVariant,
                    MaterialTheme.colorScheme.outlineVariant
                )
            }

            val textAlign = if (widgetState.textAlignment == "Left") TextAlign.Start else TextAlign.Center
            val currentVerse = widgetState.currentVerse

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, borderColor, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bgBrush)
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (widgetState.textAlignment == "Left") Alignment.Start else Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "✝ VERSE OF THE DAY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.7f),
                            letterSpacing = 1.sp
                        )

                        if (currentVerse != null) {
                            Text(
                                text = "“${currentVerse.text}”",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontStyle = FontStyle.Italic,
                                    lineHeight = 24.sp
                                ),
                                fontFamily = widgetState.fontOption.toFontFamily(),
                                color = textColor,
                                textAlign = textAlign,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Text(
                                text = "— ${currentVerse.bookId} ${currentVerse.chapter}:${currentVerse.verse} (${uiState.selectedTranslation})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                fontFamily = widgetState.fontOption.toFontFamily(),
                                color = textColor,
                                textAlign = textAlign,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Text(
                                text = "Loading random verse...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }

                        HorizontalDivider(color = textColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = { viewModel.generateNewRandomVerse() }
                            ) {
                                Text("🎲 Random Verse", color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            }

                            if (currentVerse != null) {
                                TextButton(
                                    onClick = { viewModel.navigateToVerse("${currentVerse.bookId} ${currentVerse.chapter}:${currentVerse.verse}") }
                                ) {
                                    Text("📖 Read", color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            // 2. WIDGET DESIGN & BACKGROUND STYLE SELECTOR
            Text(
                text = "🎨 Widget Design & Background",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            val backgroundOptions = listOf(
                "Warm Parchment",
                "Card Surface",
                "Dark Glass",
                "Sunset Gradient",
                "Ocean Blue",
                "Accent Theme"
            )

            // Grid of background theme options
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                items(backgroundOptions) { option ->
                    val isSelected = widgetState.selectedBackground == option
                    OutlinedButton(
                        onClick = { viewModel.updateWidgetBackground(option) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        ),
                        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1
                        )
                    }
                }
            }

            // Text Alignment Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Text Alignment:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = widgetState.textAlignment == "Center",
                        onClick = { viewModel.updateWidgetTextAlignment("Center") },
                        label = { Text("Center") }
                    )
                    FilterChip(
                        selected = widgetState.textAlignment == "Left",
                        onClick = { viewModel.updateWidgetTextAlignment("Left") },
                        label = { Text("Left") }
                    )
                }
            }

            HorizontalDivider()

            // 3. RANDOM VERSE FILTERING (ALL BIBLE VS SPECIFIC BOOK)
            Text(
                text = "📖 Random Verse Book Filter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Choose whether the widget displays random verses from across the entire Bible or focused on a single book:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Book Selector Dropdown
            ExposedDropdownMenuBox(
                expanded = bookDropdownExpanded,
                onExpandedChange = { bookDropdownExpanded = !bookDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = widgetState.selectedBookName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Selected Book for Random Verses") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bookDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = bookDropdownExpanded,
                    onDismissRequest = { bookDropdownExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("✨ All Books (Entire Bible)", fontWeight = FontWeight.Bold) },
                        onClick = {
                            viewModel.updateWidgetBookName("All Books")
                            bookDropdownExpanded = false
                        }
                    )
                    HorizontalDivider()

                    uiState.allBooks.forEach { book ->
                        DropdownMenuItem(
                            text = { Text(book.name) },
                            onClick = {
                                viewModel.updateWidgetBookName(book.name)
                                bookDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // 4. REFRESH INTERVAL CONFIGURATION
            Text(
                text = "⏱️ Widget Refresh Interval",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Set how frequently a new random verse is updated on your home screen:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val refreshIntervals = listOf(
                1 to "Every 1 Hour",
                3 to "Every 3 Hours",
                6 to "Every 6 Hours",
                12 to "Every 12 Hours",
                24 to "Every 24 Hours (Daily)"
            )

            val currentIntervalLabel = refreshIntervals.find { it.first == widgetState.refreshIntervalHours }?.second
                ?: "Every ${widgetState.refreshIntervalHours} Hours"

            ExposedDropdownMenuBox(
                expanded = refreshDropdownExpanded,
                onExpandedChange = { refreshDropdownExpanded = !refreshDropdownExpanded }
            ) {
                OutlinedTextField(
                    value = currentIntervalLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Refresh Frequency") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = refreshDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                ExposedDropdownMenu(
                    expanded = refreshDropdownExpanded,
                    onDismissRequest = { refreshDropdownExpanded = false }
                ) {
                    refreshIntervals.forEach { (hours, label) ->
                        DropdownMenuItem(
                            text = { Text(label, fontWeight = if (widgetState.refreshIntervalHours == hours) FontWeight.Bold else FontWeight.Normal) },
                            onClick = {
                                viewModel.updateWidgetRefreshInterval(hours)
                                refreshDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            // 5. HELPER CARD ON HOW TO ADD TO HOME SCREEN
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "💡 How to add widget to Home Screen:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "1. Go to your phone's Home Screen.\n2. Long-press an empty space on the screen.\n3. Tap 'Widgets' and scroll to 'Bible Faith Fuel'.\n4. Drag the Verse Widget onto your screen!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
