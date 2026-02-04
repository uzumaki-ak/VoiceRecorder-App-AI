package com.voicevault.recorder.ui.search

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicevault.recorder.data.database.entities.Recording
import com.voicevault.recorder.utils.toDateFormat
import com.voicevault.recorder.utils.toTimeFormat

// search screen with filters - FIXED
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }

    val searchResults by viewModel.searchResults.collectAsState()
    val dateFilter by viewModel.dateFilter.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val categories by viewModel.categories.collectAsState()

    LaunchedEffect(searchQuery) {
        viewModel.setSearchQuery(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search recordings...") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* voice search */ }) {
                        Icon(Icons.Default.Mic, contentDescription = "Voice search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // filters section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 1.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filters", style = MaterialTheme.typography.titleMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (dateFilter != null || categoryFilter != null) {
                                TextButton(onClick = { viewModel.clearFilters() }) {
                                    Text("Clear")
                                }
                            }
                            IconButton(onClick = { showFilters = !showFilters }) {
                                Icon(
                                    if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (showFilters) "Hide" else "Show"
                                )
                            }
                        }
                    }

                    if (showFilters) {
                        Divider()

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // date filter
                            Text("Date created", style = MaterialTheme.typography.labelMedium)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = dateFilter == DateFilter.YESTERDAY,
                                    onClick = {
                                        viewModel.setDateFilter(
                                            if (dateFilter == DateFilter.YESTERDAY) null else DateFilter.YESTERDAY
                                        )
                                    },
                                    label = { Text("Yesterday") }
                                )
                                FilterChip(
                                    selected = dateFilter == DateFilter.PAST_7_DAYS,
                                    onClick = {
                                        viewModel.setDateFilter(
                                            if (dateFilter == DateFilter.PAST_7_DAYS) null else DateFilter.PAST_7_DAYS
                                        )
                                    },
                                    label = { Text("Past 7 days") }
                                )
                                FilterChip(
                                    selected = dateFilter == DateFilter.PAST_30_DAYS,
                                    onClick = {
                                        viewModel.setDateFilter(
                                            if (dateFilter == DateFilter.PAST_30_DAYS) null else DateFilter.PAST_30_DAYS
                                        )
                                    },
                                    label = { Text("Past 30 days") }
                                )
                            }

                            // category filter
                            Text("Category", style = MaterialTheme.typography.labelMedium)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = categoryFilter == "Uncategorised",
                                    onClick = {
                                        viewModel.setCategoryFilter(
                                            if (categoryFilter == "Uncategorised") null else "Uncategorised"
                                        )
                                    },
                                    label = { Text("Uncategorised") }
                                )
                                FilterChip(
                                    selected = categoryFilter == "Call recording",
                                    onClick = {
                                        viewModel.setCategoryFilter(
                                            if (categoryFilter == "Call recording") null else "Call recording"
                                        )
                                    },
                                    label = { Text("Call recording") }
                                )
                            }
                        }
                    }
                }
            }

            // search results
            if (searchQuery.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Search for recordings",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "No results found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(searchResults, key = { it.id }) { recording ->
                        SearchResultItem(
                            recording = recording,
                            onClick = { onNavigateToPlayer(recording.id) },
                            onToggleFavorite = { viewModel.toggleFavorite(recording.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchResultItem(
    recording: Recording,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    ListItem(
        headlineContent = { Text(recording.fileName) },
        supportingContent = {
            Text("${recording.duration.toTimeFormat()} • ${recording.createdAt.toDateFormat()}")
        },
        leadingContent = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
            }
        },
        trailingContent = {
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (recording.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite"
                )
            }
        },
        modifier = Modifier.combinedClickable(
            onClick = onClick,
            onLongClick = {}
        )
    )
    Divider()
}
