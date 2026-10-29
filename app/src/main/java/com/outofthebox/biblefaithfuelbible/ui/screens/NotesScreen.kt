package com.outofthebox.biblefaithfuelbible.ui.screens

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.outofthebox.biblefaithfuelbible.data.local.NoteRecord
import com.outofthebox.biblefaithfuelbible.ui.viewmodel.BibleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var filterQuery by remember { mutableStateOf("") }
    var editingNote by remember { mutableStateOf<NoteRecord?>(null) }
    var selectedOptionsNote by remember { mutableStateOf<NoteRecord?>(null) }

    val filteredNotes = remember(uiState.notes, filterQuery) {
        if (filterQuery.isBlank()) {
            uiState.notes
        } else {
            uiState.notes.filter {
                it.reference.contains(filterQuery, ignoreCase = true) ||
                        it.content.contains(filterQuery, ignoreCase = true)
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
                    text = "Notes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Search / Filter Input
        OutlinedTextField(
            value = filterQuery,
            onValueChange = { filterQuery = it },
            placeholder = { Text("Filter saved notes...", style = MaterialTheme.typography.bodyMedium) },
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

        if (filteredNotes.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (filterQuery.isBlank())
                        "No saved notes yet.\nTap on any verse while reading to add a personal note!"
                    else "No notes found matching '$filterQuery'.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredNotes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onReadVerse = { ref ->
                            viewModel.navigateToVerse(ref)
                        },
                        onLongPress = { nt ->
                            selectedOptionsNote = nt
                        }
                    )
                }
            }
        }
    }

    // Long-Press Options Dialog for Notes (Edit / Delete)
    if (selectedOptionsNote != null) {
        val targetNt = selectedOptionsNote!!

        Dialog(onDismissRequest = { selectedOptionsNote = null }) {
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
                        text = "📝 Note Options (${targetNt.reference})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = targetNt.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Button(
                        onClick = {
                            val nt = targetNt
                            selectedOptionsNote = null
                            editingNote = nt
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("✏️ Edit Note")
                    }

                    Button(
                        onClick = {
                            val noteId = targetNt.id
                            selectedOptionsNote = null
                            viewModel.deleteNote(noteId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🗑️ Delete Note")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedOptionsNote = null }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    // Edit Note Content Dialog
    if (editingNote != null) {
        val targetNote = editingNote!!
        var editContentInput by remember { mutableStateOf(targetNote.content) }

        Dialog(onDismissRequest = { editingNote = null }) {
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
                        text = "✏️ Edit Note (${targetNote.reference})",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = editContentInput,
                        onValueChange = { editContentInput = it },
                        label = { Text("Note content") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { editingNote = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (editContentInput.isNotBlank()) {
                                    viewModel.updateNote(targetNote.id, editContentInput)
                                    editingNote = null
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

@Composable
fun NoteCard(
    note: NoteRecord,
    onReadVerse: (String) -> Unit,
    onLongPress: (NoteRecord) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(note.id) {
                detectTapGestures(
                    onTap = { onReadVerse(note.reference) },
                    onLongPress = { onLongPress(note) }
                )
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = note.reference,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
