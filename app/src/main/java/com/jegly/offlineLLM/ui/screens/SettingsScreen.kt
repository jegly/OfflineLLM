package com.jegly.offlineLLM.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
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
    ) { uri -> uri?.let { viewModel.importModel(it) } }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportChats(it) } }

    val importChatsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importChats(it) } }

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
            // === THEME ===
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
                        else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = uiState.themeMode == mode.name, onClick = null)
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }

            // === ACCENT COLOUR ===
            SectionHeader("Accent Colour")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                accentColors.forEach { accent ->
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accent.seed)
                            .then(if (uiState.accentColor == accent.key) Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape) else Modifier)
                            .clickable { viewModel.setAccentColor(accent.key) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (uiState.accentColor == accent.key) {
                            Icon(Icons.Filled.Check, contentDescription = "Selected", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            HorizontalDivider()

            // === MODEL ===
            SectionHeader("Model")
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (uiState.activeModel != null) {
                        Text("Active: ${uiState.activeModel!!.name}", style = MaterialTheme.typography.titleSmall)
                        Text("Size: ${FileUtils.formatFileSize(uiState.activeModel!!.sizeBytes)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        Text("No model selected", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
            uiState.models.forEach { model ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (model.id == uiState.activeModel?.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    ),
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(model.name, style = MaterialTheme.typography.bodyLarge)
                            Text("${FileUtils.formatFileSize(model.sizeBytes)} | ctx: ${model.contextSize}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row {
                            if (model.id != uiState.activeModel?.id) {
                                TextButton(onClick = { viewModel.selectModel(model.id) }) { Text("Use") }
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
            Button(onClick = { importModelLauncher.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.FileOpen, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Import GGUF Model")
            }

            HorizontalDivider()

            // === SAMPLING PARAMETERS ===
            SectionHeader("Sampling Parameters")

            // Temperature
            var tempValue by remember { mutableFloatStateOf(uiState.temperature) }
            ParamSlider(
                label = "Temperature: ${String.format("%.2f", tempValue)}",
                description = "Controls randomness. Lower = more focused and deterministic. Higher = more creative and varied.",
                value = tempValue,
                onValueChange = { tempValue = it },
                onValueChangeFinished = { viewModel.setTemperature(tempValue) },
                valueRange = 0.1f..2.0f,
                steps = 38,
            )

            // Top-P (nucleus sampling)
            var topPValue by remember { mutableFloatStateOf(uiState.topP) }
            ParamSlider(
                label = "Top-P: ${String.format("%.2f", topPValue)}",
                description = "Nucleus sampling. Only considers tokens whose cumulative probability exceeds this threshold. Lower = more focused.",
                value = topPValue,
                onValueChange = { topPValue = it },
                onValueChangeFinished = { viewModel.setTopP(topPValue) },
                valueRange = 0.1f..1.0f,
                steps = 18,
            )

            // Top-K
            var topKValue by remember { mutableIntStateOf(uiState.topK) }
            ParamSlider(
                label = "Top-K: $topKValue",
                description = "Limits token selection to the K most likely next tokens. Lower = more focused. 0 = disabled.",
                value = topKValue.toFloat(),
                onValueChange = { topKValue = it.toInt() },
                onValueChangeFinished = { viewModel.setTopK(topKValue) },
                valueRange = 0f..100f,
                steps = 20,
            )

            // Min-P
            var minPValue by remember { mutableFloatStateOf(uiState.minP) }
            ParamSlider(
                label = "Min-P: ${String.format("%.2f", minPValue)}",
                description = "Filters out tokens with probability below this fraction of the top token. Adaptive alternative to Top-K.",
                value = minPValue,
                onValueChange = { minPValue = it },
                onValueChangeFinished = { viewModel.setMinP(minPValue) },
                valueRange = 0.0f..0.5f,
                steps = 10,
            )

            // Repeat Penalty
            var repeatValue by remember { mutableFloatStateOf(uiState.repeatPenalty) }
            ParamSlider(
                label = "Repeat Penalty: ${String.format("%.2f", repeatValue)}",
                description = "Penalises repeating tokens. 1.0 = no penalty. Higher values reduce repetition in output.",
                value = repeatValue,
                onValueChange = { repeatValue = it },
                onValueChangeFinished = { viewModel.setRepeatPenalty(repeatValue) },
                valueRange = 1.0f..2.0f,
                steps = 20,
            )

            // Max tokens
            Text("Max Tokens: ${uiState.maxTokens}", style = MaterialTheme.typography.bodyMedium)
            Text("Maximum number of tokens the model will generate per response.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(256, 512, 1024, 2048).forEach { value ->
                    Button(
                        onClick = { viewModel.setMaxTokens(value) },
                        colors = if (uiState.maxTokens == value) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f),
                    ) { Text("$value") }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(3000, 3500, 4000).forEach { value ->
                    Button(
                        onClick = { viewModel.setMaxTokens(value) },
                        colors = if (uiState.maxTokens == value) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
                        modifier = Modifier.weight(1f),
                    ) { Text("$value") }
                }
            }

            // Context size slider
            var ctxValue by remember { mutableFloatStateOf(uiState.contextSize.toFloat()) }
            ParamSlider(
                label = "Context Size: ${ctxValue.toInt()}",
                description = "How many tokens of conversation history the model can see. Larger = more memory of past messages but uses more RAM.",
                value = ctxValue,
                onValueChange = { ctxValue = it },
                onValueChangeFinished = { viewModel.setContextSize(ctxValue.toInt()) },
                valueRange = 512f..16384f,
                steps = 31,
            )

            HorizontalDivider()

            // === THINKING TOGGLE ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Strip Thinking Tags")
                    Text("Hide <think> blocks from model output", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = uiState.disableThinking, onCheckedChange = { viewModel.setDisableThinking(it) })
            }

            HorizontalDivider()

            // === SYSTEM PROMPT ===
            SectionHeader("System Prompt")
            var promptExpanded by remember { mutableStateOf(false) }
            var customPrompt by rememberSaveable { mutableStateOf(uiState.customSystemPrompt) }
            ExposedDropdownMenuBox(expanded = promptExpanded, onExpandedChange = { promptExpanded = it }) {
                OutlinedTextField(
                    value = SystemPrompts.getLabel(uiState.systemPromptKey),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = promptExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                )
                ExposedDropdownMenu(expanded = promptExpanded, onDismissRequest = { promptExpanded = false }) {
                    SystemPrompts.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.label) },
                            onClick = { viewModel.setSystemPrompt(option.key); promptExpanded = false }
                        )
                    }
                }
            }
            if (uiState.systemPromptKey == "translator") {
                var fromExpanded by remember { mutableStateOf(false) }
                var toExpanded by remember { mutableStateOf(false) }
                val fromLabel = SystemPrompts.languages.find { it.code == uiState.translatorFrom }?.label ?: "English"
                val toLabel = SystemPrompts.languages.find { it.code == uiState.translatorTo }?.label ?: "Spanish"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = fromLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("From") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                            SystemPrompts.languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.label) },
                                    onClick = { viewModel.setTranslatorFrom(lang.code); fromExpanded = false },
                                )
                            }
                        }
                    }
                    ExposedDropdownMenuBox(
                        expanded = toExpanded,
                        onExpandedChange = { toExpanded = it },
                        modifier = Modifier.weight(1f),
                    ) {
                        OutlinedTextField(
                            value = toLabel,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("To") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                            SystemPrompts.languages.forEach { lang ->
                                DropdownMenuItem(
                                    text = { Text(lang.label) },
                                    onClick = { viewModel.setTranslatorTo(lang.code); toExpanded = false },
                                )
                            }
                        }
                    }
                }
            }
            if (uiState.systemPromptKey == "custom") {
                OutlinedTextField(value = customPrompt, onValueChange = { customPrompt = it; viewModel.setCustomSystemPrompt(it) }, label = { Text("Custom System Prompt") }, modifier = Modifier.fillMaxWidth(), minLines = 3, maxLines = 6)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("LaTeX Math Hints")
                    Text("Instruct the model to use \$...\$ notation for math", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(checked = uiState.mathLatexHints, onCheckedChange = { viewModel.setMathLatexHints(it) })
            }

            HorizontalDivider()

            // === SECURITY ===
            SectionHeader("Security")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Biometric Lock")
                Switch(
                    checked = uiState.biometricLock,
                    onCheckedChange = { viewModel.setBiometricLock(it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto-Lock on Background")
                Switch(
                    checked = uiState.biometricLock && uiState.autoLockOnBackground,
                    enabled = uiState.biometricLock,
                    onCheckedChange = { viewModel.setAutoLockOnBackground(it) }
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Screenshot Protection")
                Switch(
                    checked = uiState.screenshotProtectionEnabled,
                    onCheckedChange = { viewModel.setScreenshotProtectionEnabled(it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tapjacking Protection")
                Switch(
                    checked = uiState.tapjackingProtectionEnabled,
                    onCheckedChange = { viewModel.setTapjackingProtectionEnabled(it) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Accessibility Data Sensitivity")
                Switch(
                    checked = uiState.sensitiveDataAccessibilityEnabled,
                    onCheckedChange = { viewModel.setSensitiveDataAccessibilityEnabled(it) }
                )
            }
            Text(
                "Applies on Android 16+ to mark chat content as sensitive for accessibility.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Secure Storage Backend")
                Text(uiState.secureStorageBackend)
            }

            // === DATA ===
            SectionHeader("Data Management")
            Button(onClick = { exportLauncher.launch("offlinellm_export.json") }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Upload, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Export Chats as JSON")
            }
            Button(onClick = { importChatsLauncher.launch(arrayOf("application/json")) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Import Chats from JSON")
            }
            Button(onClick = { showClearDialog = true }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Clear All Chats")
            }

            HorizontalDivider()

            // === ABOUT & HELP ===
            Button(onClick = onNavigateToHelp, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Help & Model Guide")
            }
            Button(onClick = onNavigateToAbout, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.outlinedButtonColors()) {
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
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                    Text("Copying and validating model file\u2026\nThis may take a moment for large models.", style = MaterialTheme.typography.bodyMedium)
                }
            },
            confirmButton = {},
        )
    }

    // Clear all dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false; clearConfirmText = "" },
            title = { Text("Clear All Chats") },
            text = {
                Column {
                    Text("This will permanently delete all conversations and messages. This cannot be undone.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Type DELETE to confirm:")
                    OutlinedTextField(value = clearConfirmText, onValueChange = { clearConfirmText = it }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAllChats(); showClearDialog = false; clearConfirmText = "" }, enabled = clearConfirmText == "DELETE") {
                    Text("Confirm", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false; clearConfirmText = "" }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
}

@Composable
private fun ParamSlider(
    label: String,
    description: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int,
) {
    Column {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            steps = steps,
        )
    }
}
