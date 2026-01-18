package com.example.diary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diary.data.entity.DiaryEntry
import com.example.diary.data.entity.Mood
import com.example.diary.ui.components.EmptyState
import com.example.diary.ui.viewmodel.SearchViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilters by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Search diaries...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.surface
                        ),
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        BadgedBox(
                            badge = {
                                val filterCount = uiState.selectedMoods.size + uiState.selectedTags.size +
                                        (if (uiState.dateRangeStart != null) 1 else 0)
                                if (filterCount > 0) {
                                    Badge(
                                        modifier = Modifier.offset(x = 8.dp, y = (-8).dp)
                                    ) {
                                        Text(filterCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters")
                        }
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
            // Filters section
            if (showFilters) {
                FiltersSection(
                    selectedMoods = uiState.selectedMoods,
                    availableTags = uiState.availableTags,
                    selectedTags = uiState.selectedTags,
                    onMoodToggled = { viewModel.toggleMoodFilter(it) },
                    onTagToggled = { viewModel.toggleTagFilter(it) },
                    onClearFilters = { viewModel.clearFilters() }
                )
                Divider()
            }

            // Search results
            when {
                uiState.searchQuery.isEmpty() -> {
                    EmptyState(
                        title = "Search Your Diaries",
                        description = "Enter keywords to search through your diary entries, titles, and content."
                    )
                }
                uiState.isSearching -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.searchResults.isEmpty() -> {
                    EmptyState(
                        title = "No Results Found",
                        description = "Try different keywords or adjust your filters."
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Text(
                                text = "${uiState.searchResults.size} results found",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        items(
                            items = uiState.searchResults,
                            key = { it.id }
                        ) { diary ->
                            SearchResultItem(
                                diary = diary,
                                searchQuery = uiState.searchQuery,
                                onClick = { onNavigateToDetail(diary.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiltersSection(
    selectedMoods: Set<Mood>,
    availableTags: List<com.example.diary.data.entity.Tag>,
    selectedTags: Set<com.example.diary.data.entity.Tag>,
    onMoodToggled: (Mood) -> Unit,
    onTagToggled: (com.example.diary.data.entity.Tag) -> Unit,
    onClearFilters: () -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.titleSmall
            )
            TextButton(onClick = onClearFilters) {
                Text("Clear all")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Mood filters
        Text(
            text = "Mood",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Mood.entries.toList()) { mood ->
                FilterChip(
                    selected = mood in selectedMoods,
                    onClick = { onMoodToggled(mood) },
                    label = { Text(mood.emoji) }
                )
            }
        }

        // Tag filters
        if (availableTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tags",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableTags) { tag ->
                    FilterChip(
                        selected = tag in selectedTags,
                        onClick = { onTagToggled(tag) },
                        label = { Text(tag.name) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultItem(
    diary: DiaryEntry,
    searchQuery: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDate(diary.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = diary.mood.emoji,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (diary.title.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                HighlightedText(
                    text = diary.title,
                    query = searchQuery,
                    style = MaterialTheme.typography.titleSmall
                )
            }

            if (diary.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                HighlightedText(
                    text = diary.content,
                    query = searchQuery,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    query: String,
    style: androidx.compose.ui.text.TextStyle,
    maxLines: Int = 1
) {
    val annotatedString = buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var startIndex = 0

        while (true) {
            val index = lowerText.indexOf(lowerQuery, startIndex)
            if (index < 0) {
                append(text.substring(startIndex))
                break
            }

            append(text.substring(startIndex, index))

            pushStyle(SpanStyle(
                fontWeight = FontWeight.Bold,
                background = MaterialTheme.colorScheme.primaryContainer
            ))
            append(text.substring(index, index + query.length))
            pop()

            startIndex = index + query.length
        }
    }

    Text(
        text = annotatedString,
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface
    )
}

private fun formatDate(timestamp: Long): String {
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
}
