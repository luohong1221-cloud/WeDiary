package com.example.diary.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.diary.data.repository.DarkMode
import com.example.diary.data.repository.FontSize
import com.example.diary.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTags: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Appearance section
            item {
                SettingsSectionHeader(title = "Appearance")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark Mode",
                    subtitle = uiState.darkMode.toDisplayString(),
                    onClick = { showDarkModeDialog = true }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.FormatSize,
                    title = "Font Size",
                    subtitle = uiState.fontSize.toDisplayString(),
                    onClick = { showFontSizeDialog = true }
                )
            }

            // Content section
            item {
                SettingsSectionHeader(title = "Content")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Label,
                    title = "Manage Tags",
                    subtitle = "Create and organize tags",
                    onClick = onNavigateToTags
                )
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Save,
                    title = "Auto Save",
                    subtitle = "Automatically save drafts",
                    checked = uiState.autoSave,
                    onCheckedChange = { viewModel.setAutoSave(it) }
                )
            }

            // Security section
            item {
                SettingsSectionHeader(title = "Security")
            }

            item {
                SettingsSwitchItem(
                    icon = Icons.Default.Lock,
                    title = "App Lock",
                    subtitle = "Require authentication to open app",
                    checked = uiState.appLockEnabled,
                    onCheckedChange = {
                        if (it) {
                            showPinDialog = true
                        } else {
                            viewModel.setAppLockEnabled(false)
                            viewModel.clearPinCode()
                        }
                    }
                )
            }

            if (uiState.appLockEnabled) {
                item {
                    SettingsSwitchItem(
                        icon = Icons.Default.Fingerprint,
                        title = "Use Biometric",
                        subtitle = "Unlock with fingerprint or face",
                        checked = uiState.useBiometric,
                        onCheckedChange = { viewModel.setUseBiometric(it) }
                    )
                }
            }

            // About section
            item {
                SettingsSectionHeader(title = "About")
            }

            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "Version",
                    subtitle = "1.0.0",
                    onClick = { }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "Privacy Policy",
                    subtitle = "Your data stays on your device",
                    onClick = { }
                )
            }
        }
    }

    // Dark mode dialog
    if (showDarkModeDialog) {
        OptionDialog(
            title = "Dark Mode",
            options = DarkMode.entries.map { it to it.toDisplayString() },
            selectedOption = uiState.darkMode,
            onOptionSelected = {
                viewModel.setDarkMode(it)
                showDarkModeDialog = false
            },
            onDismiss = { showDarkModeDialog = false }
        )
    }

    // Font size dialog
    if (showFontSizeDialog) {
        OptionDialog(
            title = "Font Size",
            options = FontSize.entries.map { it to it.toDisplayString() },
            selectedOption = uiState.fontSize,
            onOptionSelected = {
                viewModel.setFontSize(it)
                showFontSizeDialog = false
            },
            onDismiss = { showFontSizeDialog = false }
        )
    }

    // PIN setup dialog
    if (showPinDialog) {
        PinSetupDialog(
            onPinSet = { pin ->
                viewModel.setPinCode(pin)
                viewModel.setAppLockEnabled(true)
                showPinDialog = false
            },
            onDismiss = { showPinDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    )
}

@Composable
private fun <T> OptionDialog(
    title: String,
    options: List<Pair<T, String>>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (option, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOptionSelected(option) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = option == selectedOption,
                            onClick = { onOptionSelected(option) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun PinSetupDialog(
    onPinSet: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(1) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (step == 1) "Create PIN" else "Confirm PIN") },
        text = {
            Column {
                Text(
                    text = if (step == 1) "Enter a 4-digit PIN" else "Enter your PIN again",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = if (step == 1) pin else confirmPin,
                    onValueChange = {
                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                            if (step == 1) pin = it else confirmPin = it
                            error = null
                        }
                    },
                    label = { Text("PIN") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (step == 1) {
                        if (pin.length == 4) {
                            step = 2
                        } else {
                            error = "PIN must be 4 digits"
                        }
                    } else {
                        if (confirmPin == pin) {
                            onPinSet(pin)
                        } else {
                            error = "PINs don't match"
                            confirmPin = ""
                        }
                    }
                }
            ) {
                Text(if (step == 1) "Next" else "Set PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun DarkMode.toDisplayString(): String = when (this) {
    DarkMode.LIGHT -> "Light"
    DarkMode.DARK -> "Dark"
    DarkMode.SYSTEM -> "Follow system"
}

private fun FontSize.toDisplayString(): String = when (this) {
    FontSize.SMALL -> "Small"
    FontSize.MEDIUM -> "Medium"
    FontSize.LARGE -> "Large"
    FontSize.EXTRA_LARGE -> "Extra Large"
}
