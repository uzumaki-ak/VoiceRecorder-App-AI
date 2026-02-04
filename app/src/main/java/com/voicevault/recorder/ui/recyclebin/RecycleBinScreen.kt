package com.voicevault.recorder.ui.recyclebin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// recycle bin screen - NEW
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecycleBinScreen(
    onNavigateBack: () -> Unit,
    viewModel: RecycleBinViewModel = viewModel()
) {
    val deletedRecordings by viewModel.deletedRecordings.collectAsState()
    var showEmptyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recycle bin") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (deletedRecordings.isNotEmpty()) {
                        IconButton(onClick = { showEmptyDialog = true }) {
                            Icon(Icons.Default.DeleteForever, contentDescription = "Empty bin")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (deletedRecordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Recycle bin is empty",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Deleted recordings are kept for 30 days",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(deletedRecordings, key = { it.id }) { recording ->
                    ListItem(
                        headlineContent = { Text(recording.fileName) },
                        supportingContent = {
                            Text("Deleted ${recording.deletedAt?.let { System.currentTimeMillis() - it } ?: 0 / (1000 * 60 * 60 * 24)} days ago")
                        },
                        trailingContent = {
                            Row {
                                IconButton(onClick = { viewModel.restoreRecording(recording.id) }) {
                                    Icon(Icons.Default.RestoreFromTrash, contentDescription = "Restore")
                                }
                                IconButton(onClick = { viewModel.permanentlyDelete(recording.id) }) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = "Delete permanently")
                                }
                            }
                        }
                    )
                    Divider()
                }
            }
        }
    }

    if (showEmptyDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyDialog = false },
            title = { Text("Empty recycle bin?") },
            text = { Text("All recordings will be permanently deleted. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.emptyRecycleBin()
                    showEmptyDialog = false
                }) {
                    Text("Empty")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}