package com.example.diary.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.diary.data.entity.DiaryImage
import com.example.diary.data.entity.Mood
import com.example.diary.data.entity.Tag
import com.example.diary.data.entity.Weather
import com.example.diary.ui.theme.TagColors
import com.example.diary.ui.viewmodel.EditDiaryViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDiaryScreen(
    diaryId: Long,
    onNavigateBack: () -> Unit,
    onSaveSuccess: (Long) -> Unit,
    viewModel: EditDiaryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var showMoodPicker by remember { mutableStateOf(false) }
    var showWeatherPicker by remember { mutableStateOf(false) }
    var showTagPicker by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }

    LaunchedEffect(diaryId) {
        viewModel.loadDiary(diaryId)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            viewModel.addImages(uris)
        }
    }

    val handleBack = {
        if (viewModel.hasUnsavedChanges()) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isNewDiary) "New Diary" else "Edit Diary")
                },
                navigationIcon = {
                    IconButton(onClick = { handleBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveDiary { savedId ->
                                onSaveSuccess(savedId)
                            }
                        },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Mood and Weather row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Mood selector
                    AssistChip(
                        onClick = { showMoodPicker = true },
                        label = { Text(uiState.mood.label) },
                        leadingIcon = { Text(uiState.mood.emoji) }
                    )

                    // Weather selector
                    AssistChip(
                        onClick = { showWeatherPicker = true },
                        label = { Text(uiState.weather?.label ?: "Weather") },
                        leadingIcon = {
                            uiState.weather?.let { Text(it.icon) }
                                ?: Icon(Icons.Default.Cloud, contentDescription = null, Modifier.size(18.dp))
                        }
                    )
                }

                // Title input
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("Title") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.titleLarge
                )

                // Content input
                OutlinedTextField(
                    value = uiState.content,
                    onValueChange = { viewModel.updateContent(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp)
                        .padding(horizontal = 16.dp),
                    placeholder = { Text("What's on your mind today?") },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )

                // Tags section
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { showTagPicker = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Tag")
                        }
                    }

                    if (uiState.selectedTags.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(uiState.selectedTags) { tag ->
                                InputChip(
                                    selected = true,
                                    onClick = { viewModel.toggleTag(tag) },
                                    label = { Text(tag.name) },
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            Modifier.size(16.dp)
                                        )
                                    },
                                    colors = InputChipDefaults.inputChipColors(
                                        selectedContainerColor = Color(tag.color).copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }
                    }
                }

                // Images section
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Photos",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Photos")
                        }
                    }

                    val allImages = uiState.images + uiState.pendingImageUris.mapIndexed { index, uri ->
                        PendingImage(uri, index)
                    }

                    if (allImages.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            // Existing images
                            items(uiState.images, key = { it.id }) { image ->
                                ImageThumbnail(
                                    imagePath = image.thumbnailPath ?: image.imagePath,
                                    onRemove = { viewModel.removeImage(image) }
                                )
                            }

                            // Pending images
                            items(uiState.pendingImageUris) { uri ->
                                ImageThumbnail(
                                    uri = uri,
                                    onRemove = { viewModel.removePendingImage(uri) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }

    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDiscardDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep editing")
                }
            }
        )
    }

    // Mood picker bottom sheet
    if (showMoodPicker) {
        MoodPickerSheet(
            selectedMood = uiState.mood,
            onMoodSelected = {
                viewModel.updateMood(it)
                showMoodPicker = false
            },
            onDismiss = { showMoodPicker = false }
        )
    }

    // Weather picker bottom sheet
    if (showWeatherPicker) {
        WeatherPickerSheet(
            selectedWeather = uiState.weather,
            onWeatherSelected = {
                viewModel.updateWeather(it)
                showWeatherPicker = false
            },
            onDismiss = { showWeatherPicker = false }
        )
    }

    // Tag picker bottom sheet
    if (showTagPicker) {
        TagPickerSheet(
            availableTags = uiState.availableTags,
            selectedTags = uiState.selectedTags,
            onTagToggled = { viewModel.toggleTag(it) },
            onCreateNewTag = { showNewTagDialog = true },
            onDismiss = { showTagPicker = false }
        )
    }

    // New tag dialog
    if (showNewTagDialog) {
        NewTagDialog(
            onCreateTag = { name, color ->
                viewModel.createAndSelectTag(name, color)
                showNewTagDialog = false
            },
            onDismiss = { showNewTagDialog = false }
        )
    }

    // Error snackbar
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }
}

private data class PendingImage(val uri: Uri, val index: Int)

@Composable
private fun ImageThumbnail(
    imagePath: String? = null,
    uri: Uri? = null,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier.size(100.dp)
    ) {
        AsyncImage(
            model = imagePath?.let { File(it) } ?: uri,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )

        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodPickerSheet(
    selectedMood: Mood,
    onMoodSelected: (Mood) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "How are you feeling?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Mood.entries.chunked(5).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row.forEach { mood ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMoodSelected(mood) }
                                .then(
                                    if (mood == selectedMood)
                                        Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                                    else Modifier
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = mood.emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = mood.label,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherPickerSheet(
    selectedWeather: Weather?,
    onWeatherSelected: (Weather?) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "What's the weather like?",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Weather.entries.forEach { weather ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onWeatherSelected(weather) }
                            .then(
                                if (weather == selectedWeather)
                                    Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                                else Modifier
                            )
                            .padding(12.dp)
                    ) {
                        Text(
                            text = weather.icon,
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = weather.label,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            TextButton(
                onClick = { onWeatherSelected(null) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Clear")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagPickerSheet(
    availableTags: List<Tag>,
    selectedTags: List<Tag>,
    onTagToggled: (Tag) -> Unit,
    onCreateNewTag: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Tags",
                    style = MaterialTheme.typography.titleMedium
                )
                TextButton(onClick = onCreateNewTag) {
                    Icon(Icons.Default.Add, contentDescription = null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Tag")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (availableTags.isEmpty()) {
                Text(
                    text = "No tags yet. Create your first tag!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                availableTags.chunked(3).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        row.forEach { tag ->
                            val isSelected = selectedTags.any { it.id == tag.id }
                            FilterChip(
                                selected = isSelected,
                                onClick = { onTagToggled(tag) },
                                label = { Text(tag.name) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(tag.color).copy(alpha = 0.2f)
                                )
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NewTagDialog(
    onCreateTag: (String, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var tagName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(TagColors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Tag") },
        text = {
            Column {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TagColors.take(5).forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TagColors.drop(5).forEach { color ->
                        ColorOption(
                            color = color,
                            isSelected = color == selectedColor,
                            onClick = { selectedColor = color }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreateTag(tagName, selectedColor.value.toLong()) },
                enabled = tagName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorOption(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(color)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.padding(4.dp)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
