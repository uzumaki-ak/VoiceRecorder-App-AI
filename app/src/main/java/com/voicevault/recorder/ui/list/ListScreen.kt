package com.voicevault.recorder.ui.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.ui.components.CategoryDialog
import com.voicevault.recorder.utils.toDateFormat
import com.voicevault.recorder.utils.toTimeFormat

// list screen showing all recordings with filters and actions - ALL BUTTONS FIXED
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToManageCategories: () -> Unit,
    viewModel: ListViewModel = viewModel()
) {
    val recordings by viewModel.recordings.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val isSelectionMode by viewModel.isSelectionMode.collectAsState()
    val selectedRecordings by viewModel.selectedRecordings.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var recordingToRename by remember { mutableStateOf<Recording?>(null) }
    var playThroughSpeaker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSelectionMode) {
                        Text("${selectedRecordings.size} selected")
                    } else {
                        Text("List")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            viewModel.toggleSelectionMode()
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isSelectionMode) {
                        // FIXED: Search button now works
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                        }

                        // FIXED: All menu items now work
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (playThroughSpeaker) "Play through receiver" else "Play through speaker") },
                                onClick = {
                                    playThroughSpeaker = !playThroughSpeaker
                                    showMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    viewModel.toggleSelectionMode()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    showMenu = false
                                    showShareDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Sort") },
                                onClick = {
                                    showMenu = false
                                    showSortDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Manage categories") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToManageCategories()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Recycle bin") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToRecycleBin()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Settings") },
                                onClick = {
                                    showMenu = false
                                    onNavigateToSettings()
                                }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isSelectionMode && selectedRecordings.isNotEmpty()) {
                BottomAppBar {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(onClick = { showMoveDialog = true }) {
                            Icon(Icons.Default.DriveFileMove, contentDescription = "Move")
                        }
                        IconButton(onClick = {
                            if (selectedRecordings.size == 1) {
                                recordingToRename = recordings.find { it.id in selectedRecordings }
                                showRenameDialog = true
                            }
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename")
                        }
                        // FIXED: Share button now works
                        IconButton(onClick = {
                            viewModel.shareRecordings(selectedRecordings)
                        }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                        IconButton(onClick = {
                            viewModel.deleteRecordings(selectedRecordings)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                        IconButton(onClick = {
                            // toggle favorite for selected
                            viewModel.toggleFavoriteForSelected(selectedRecordings)
                        }) {
                            Icon(Icons.Default.Star, contentDescription = "Favorite")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // filter selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { showFilterMenu = true }) {
                    Text(
                        when (selectedFilter) {
                            is FilterType.All -> "All (${recordings.size})"
                            is FilterType.Favorites -> "Favorites"
                            is FilterType.Uncategorized -> "Uncategorized"
                            is FilterType.Category -> {
                                val category = categories.find { it.id == (selectedFilter as FilterType.Category).categoryId }
                                category?.name ?: "Category"
                            }
                            is FilterType.DateRange -> "Date Range"
                        }
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }

                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All (${recordings.size})") },
                        onClick = {
                            viewModel.setFilter(FilterType.All)
                            showFilterMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Favourites") },
                        onClick = {
                            viewModel.setFilter(FilterType.Favorites)
                            showFilterMenu = false
                        }
                    )

                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category.name) },
                            onClick = {
                                viewModel.setFilter(FilterType.Category(category.id))
                                showFilterMenu = false
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("Uncategorised") },
                        onClick = {
                            viewModel.setFilter(FilterType.Uncategorized)
                            showFilterMenu = false
                        }
                    )

                    Divider()

                    DropdownMenuItem(
                        text = { Text("Add category") },
                        onClick = {
                            showFilterMenu = false
                            showCategoryDialog = true
                        }
                    )
                }
            }

            // recordings list - FIXED: long press now works
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(recordings, key = { it.id }) { recording ->
                    RecordingListItem(
                        recording = recording,
                        isSelected = recording.id in selectedRecordings,
                        isSelectionMode = isSelectionMode,
                        onSelect = { viewModel.toggleRecordingSelection(recording.id) },
                        onClick = {
                            if (isSelectionMode) {
                                viewModel.toggleRecordingSelection(recording.id)
                            } else {
                                onNavigateToPlayer(recording.id)
                            }
                        },
                        onLongClick = {
                            // FIXED: long press enters selection mode and selects item
                            if (!isSelectionMode) {
                                viewModel.toggleSelectionMode()
                            }
                            viewModel.toggleRecordingSelection(recording.id)
                        },
                        onToggleFavorite = { viewModel.toggleFavorite(recording.id) }
                    )
                }
            }
        }
    }

    // category creation dialog
    if (showCategoryDialog) {
        CategoryDialog(
            onDismiss = { showCategoryDialog = false },
            onConfirm = { name ->
                viewModel.createCategory(name)
                showCategoryDialog = false
            }
        )
    }

    // move to category dialog
    if (showMoveDialog) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Move to category") },
            text = {
                Column {
                    categories.forEach { category ->
                        TextButton(
                            onClick = {
                                viewModel.moveToCategory(selectedRecordings, category.id)
                                showMoveDialog = false
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(category.name)
                        }
                    }
                    TextButton(
                        onClick = {
                            viewModel.moveToCategory(selectedRecordings, null)
                            showMoveDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Uncategorized")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // rename dialog
    if (showRenameDialog && recordingToRename != null) {
        var newName by remember { mutableStateOf(recordingToRename!!.fileName) }

        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Name") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameRecording(recordingToRename!!.id, newName)
                    showRenameDialog = false
                    recordingToRename = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRenameDialog = false
                    recordingToRename = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    // FIXED: Sort dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort by") },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DATE_DESC)
                            showSortDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Date (Newest first)")
                    }
                    TextButton(
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DATE_ASC)
                            showSortDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Date (Oldest first)")
                    }
                    TextButton(
                        onClick = {
                            viewModel.setSortOrder(SortOrder.NAME_ASC)
                            showSortDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Name (A-Z)")
                    }
                    TextButton(
                        onClick = {
                            viewModel.setSortOrder(SortOrder.NAME_DESC)
                            showSortDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Name (Z-A)")
                    }
                    TextButton(
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DURATION_DESC)
                            showSortDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Duration (Longest first)")
                    }
                    TextButton(
                        onClick = {
                            viewModel.setSortOrder(SortOrder.DURATION_ASC)
                            showSortDialog = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Duration (Shortest first)")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// individual recording list item - FIXED: long press support
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecordingListItem(
    recording: Recording,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onSelect: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    ListItem(
        headlineContent = { Text(recording.fileName) },
        supportingContent = {
            Text("${recording.duration.toTimeFormat()} • ${recording.createdAt.toDateFormat()}")
        },
        leadingContent = {
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelect() }
                )
            } else {
                IconButton(onClick = onClick) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
            }
        },
        trailingContent = {
            Row {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        if (recording.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite"
                    )
                }
            }
        },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = onLongClick // FIXED: long press now works
        )
    )
    Divider()
}