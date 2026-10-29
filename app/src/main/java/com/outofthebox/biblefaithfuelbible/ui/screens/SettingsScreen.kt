package com.outofthebox.biblefaithfuelbible.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.outofthebox.biblefaithfuelbible.ui.theme.AppFontOption
import com.outofthebox.biblefaithfuelbible.ui.theme.IosAccentColor
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

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
                    text = "⚙️ Settings",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            TextButton(onClick = { viewModel.selectDrawerTab("reader") }) {
                Text("← Reader")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Appearance & Theme
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "🎨 Appearance & Theme",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Dark Mode Toggle Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(if (uiState.isDarkTheme) "🌙" else "☀️", style = MaterialTheme.typography.titleLarge)
                            Text(
                                text = if (uiState.isDarkTheme) "Dark Theme" else "Light Theme",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Switch(
                            checked = uiState.isDarkTheme,
                            onCheckedChange = { viewModel.updateDarkTheme(it) }
                        )
                    }

                    HorizontalDivider()

                    // Color Accent Selector
                    Text(
                        text = "Color Accent:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    var accentExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = accentExpanded,
                        onExpandedChange = { accentExpanded = !accentExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedIosAccent.displayName,
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(Color(uiState.selectedIosAccent.primaryHex))
                                )
                            },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accentExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = accentExpanded,
                            onDismissRequest = { accentExpanded = false }
                        ) {
                            IosAccentColor.entries.forEach { accent ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(accent.primaryHex))
                                            )
                                            Text(accent.displayName)
                                        }
                                    },
                                    onClick = {
                                        viewModel.updateIosAccentColor(accent)
                                        accentExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Card 2: Reader Typography & Font Family
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "📖 Reader Typography",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Font Family Dropdown Selector
                    Text(
                        text = "Font Family:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    var fontExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = fontExpanded,
                        onExpandedChange = { fontExpanded = !fontExpanded }
                    ) {
                        OutlinedTextField(
                            value = uiState.selectedFontOption.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fontExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = fontExpanded,
                            onDismissRequest = { fontExpanded = false }
                        ) {
                            AppFontOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = option.displayName,
                                            fontFamily = option.toFontFamily(),
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    onClick = {
                                        viewModel.updateFontOption(option)
                                        fontExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Font Size Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Text Size: ${uiState.fontSizeSp} sp",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedButton(onClick = { viewModel.cycleFontSize() }) {
                            Text("Change AA (${uiState.fontSizeSp} sp)")
                        }
                    }

                    HorizontalDivider()

                    // Sample Typography Preview Card
                    Text(
                        text = "Typography Preview:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "John 1:1",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontFamily = uiState.selectedFontOption.toFontFamily()
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "In the beginning was the Word, and the Word was with God, and the Word was God.",
                                fontFamily = uiState.selectedFontOption.toFontFamily(),
                                fontSize = uiState.fontSizeSp.sp,
                                lineHeight = (uiState.fontSizeSp * 1.4).sp
                            )
                        }
                    }
                }
            }

            // Card 3: Bible Translation
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "📜 Translation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    var translationExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = translationExpanded,
                        onExpandedChange = { translationExpanded = !translationExpanded }
                    ) {
                        OutlinedTextField(
                            value = if (uiState.selectedTranslation == "BSB") "BSB (Berean Standard Bible)" else "ASV (American Standard Version)",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = translationExpanded) },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = translationExpanded,
                            onDismissRequest = { translationExpanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("BSB (Berean Standard Bible)") },
                                onClick = {
                                    viewModel.updateTranslation("BSB")
                                    translationExpanded = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("ASV (American Standard Version)") },
                                onClick = {
                                    viewModel.updateTranslation("ASV")
                                    translationExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
