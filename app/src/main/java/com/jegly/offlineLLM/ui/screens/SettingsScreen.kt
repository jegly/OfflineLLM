package com.jegly.offlineLLM.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jegly.offlineLLM.ai.SystemPrompts
import com.jegly.offlineLLM.ui.theme.ThemeMode
import com.jegly.offlineLLM.ui.theme.accentColors
import com.jegly.offlineLLM.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAbout: () -> Unit = {},
    onNavigateToHelp: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val importModelLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importModel(it) }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportChats(it) }
    }

    val importChatsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importChats(it) }
    }

    var showClearDialog by remember { mutableStateOf(false) }
    var clearConfirmText by remember { mutableStateOf("") }

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
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // === THEME SECTION ===
            SectionHeader("Appearance")

            ThemeMode.entries.forEach { mode ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = uiState.themeMode == mode.name,
                            onClick = { viewModel.setTheme(mode) },
                            role = Role.RadioButton,
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.themeMode == mode.name)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = uiState.themeMode == mode.name,
                            onClick = null,
                        )
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 12.dp),
                        )
                    }
                }
            }

            // === ACCENT COLOUR SECTION ===
            SectionHeader("Accent Colour")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                accentColors.forEach { accent ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accent.seed)
                            .then(
                                if (uiState.accentColor == accent.key)
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                else Modifier
                            )
                            .clickable { viewModel.setAccentColor(accent.key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.accentColor == accent.key) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            HorizontalDivider()

            // === MODEL SECTION ===
            SectionHeader("Model")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.activeModel != null) {
                        Text("Active: ${uiState.activeModel!!.name}", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Size: ${FileUtils.formatFileSize(uiState.activeModel!!.sizeBytes)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text("No model selected", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            uiState.models.forEach { model ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (model.id == uiState.activeModel?.id)
                            MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(model.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                FileUtils.formatFileSize(model.sizeBytes),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row {
                            if (model.id != uiState.activeModel?.id) {
                                TextButton(onClick = { viewModel.selectModel(model.id) }) {
                                    Text("Use")
                                }
                            }
                            if (!model.isBundled) {
                                IconButton(onClick = { viewModel.deleteModel(model.id) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }

            Button(
                onClick = { importModelLauncher.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.FileOpen, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Import GGUF Model")
            }

            HorizontalDivider()

            // === GENERATION SECTION ===
            SectionHeader("Generation")

            var tempValue by remember { mutableFloatStateOf(uiState.temperature) }
            Text("Temperature: ${String.format("%.2f", tempValue)}")
            Slider(
                value = tempValue,
                onValueChange = { tempValue = it },
                onValueChangeFinished = { viewModel.setTemperature(tempValue) },
                valueRange = 0.1f..1.0f,
                steps = 17,
            )

            Text("Max Tokens: ${uiState.maxTokens}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(256, 512, 1024, 2048).forEach { value ->
                    Button(
                        onClick = { viewModel.setMaxTokens(value) },
                        colors = if (uiState.maxTokens == value) ButtonDefaults.buttonColors()
                        else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("$value")
                    }
                }
            }

            Text("Context Size: ${uiState.contextSize}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(2048, 4096, 8192).forEach { value ->
                    Button(
                        onClick = { viewModel.setContextSize(value) },
                        colors = if (uiState.contextSize == value) ButtonDefaults.buttonColors()
                        else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("$value")
                    }
                }
            }

            HorizontalDivider()

            // === SYSTEM PROMPT SECTION ===
            SectionHeader("System Prompt")

            var promptExpanded by remember { mutableStateOf(false) }
            var customPrompt by rememberSaveable { mutableStateOf("") }

            ExposedDropdownMenuBox(
                expanded = promptExpanded,
                onExpandedChange = { promptExpanded = it },
            ) {
                OutlinedTextField(
                    value = SystemPrompts.getLabel(uiState.systemPromptKey),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = promptExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(
                    expanded = promptExpanded,
                    onDismissRequest = { promptExpanded = false },
                ) {
                    SystemPrompts.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = {
                                viewModel.setSystemPrompt(option.key)
                                promptExpanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.systemPromptKey == "custom") {
                OutlinedTextField(
                    value = customPrompt,
                    onValueChange = { customPrompt = it },
                    label = { Text("Custom System Prompt") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 6,
                )
            }

            HorizontalDivider()

            // === GENERATION BEHAVIOUR ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Strip Thinking Tags")
                    Text(
                        "Hide <think> blocks from model output",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = uiState.disableThinking,
                    onCheckedChange = { viewModel.setDisableThinking(it) },
                )
            }

            HorizontalDivider()

            // === SECURITY SECTION ===
            SectionHeader("Security")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Biometric Lock")
                Switch(
                    checked = uiState.biometricLock,
                    onCheckedChange = { viewModel.setBiometricLock(it) },
                )
            }

            HorizontalDivider()

            // === DATA SECTION ===
            SectionHeader("Data Management")

            Button(
                onClick = { exportLauncher.launch("offlinellm_export.json") },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Export Chats as JSON")
            }

            Button(
                onClick = { importChatsLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Import Chats from JSON")
            }

            Button(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Clear All Chats")
            }

            HorizontalDivider()

            // === ABOUT & HELP ===
            Button(
                onClick = onNavigateToHelp,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(),
            ) {
                Icon(Icons.Filled.HelpOutline, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Help & Model Guide")
            }

            Button(
                onClick = onNavigateToAbout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(),
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("About OfflineLLM")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Model import loading dialog
    if (uiState.isImportingModel) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Importing Model") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text(
                        "Copying and validating model file\u2026\nThis may take a moment for large models.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            },
            confirmButton = {},
        )
    }

    // Clear all dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = {
                showClearDialog = false
                clearConfirmText = ""
            },
            title = { Text("Clear All Chats") },
            text = {
                Column {
                    Text("This will permanently delete all conversations and messages. This cannot be undone.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Type DELETE to confirm:")
                    OutlinedTextField(
                        value = clearConfirmText,
                        onValueChange = { clearConfirmText = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearAllChats()
                        showClearDialog = false
                        clearConfirmText = ""
                    },
                    enabled = clearConfirmText == "DELETE",
                ) {
                    Text("Confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    clearConfirmText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}
