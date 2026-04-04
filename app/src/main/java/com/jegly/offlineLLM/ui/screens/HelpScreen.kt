package com.jegly.offlineLLM.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    onNavigateBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Help") },
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
            HelpSection(
                title = "Getting a Model",
                content = "OfflineLLM needs a GGUF model file to work. Here\u2019s how to get one:\n\n" +
                    "1. Visit huggingface.co on your phone or PC\n" +
                    "2. Search for a model (e.g. \u201Cgemma 3 1b GGUF\u201D or \u201Cqwen 0.8b GGUF\u201D)\n" +
                    "3. Look for files ending in .gguf \u2014 choose Q4_K_M for best quality/speed balance\n" +
                    "4. Download the .gguf file to your device\n" +
                    "5. In OfflineLLM, go to Settings \u2192 Import GGUF Model\n" +
                    "6. Select the downloaded file"
            )

            HelpSection(
                title = "Recommended Models",
                content = "For devices with 4GB RAM:\n" +
                    "\u2022 Gemma 3 270M (~300MB) \u2014 Fast, good for simple tasks\n" +
                    "\u2022 Qwen3.5 0.8B (~530MB) \u2014 Good balance\n\n" +
                    "For devices with 6-8GB RAM:\n" +
                    "\u2022 Gemma 3 1B (~750MB) \u2014 Recommended\n" +
                    "\u2022 Qwen3.5 4B (~2.5GB) \u2014 Higher quality\n\n" +
                    "For flagships with 12GB+ RAM:\n" +
                    "\u2022 Any model up to ~4GB GGUF size"
            )

            HelpSection(
                title = "Choosing Quantization",
                content = "GGUF models come in different quantization levels:\n\n" +
                    "\u2022 Q4_K_M \u2014 Best balance of quality and speed (recommended)\n" +
                    "\u2022 Q5_K_M \u2014 Slightly better quality, slightly slower\n" +
                    "\u2022 Q8_0 \u2014 Near-original quality, uses more RAM\n" +
                    "\u2022 Q2_K \u2014 Smallest size, lowest quality"
            )

            HelpSection(
                title = "Performance Tips",
                content = "\u2022 Close other apps before chatting to free up RAM\n" +
                    "\u2022 Use a smaller context size (2048) on low-RAM devices\n" +
                    "\u2022 Lower temperature (0.3-0.5) gives more focused answers\n" +
                    "\u2022 Higher temperature (0.8-1.0) gives more creative answers\n" +
                    "\u2022 If responses are slow, try a smaller model"
            )

            HelpSection(
                title = "Privacy",
                content = "OfflineLLM has no internet permission. Your conversations never leave your device. " +
                    "The app cannot connect to any server, send analytics, or transmit data of any kind. " +
                    "All processing happens entirely on your phone\u2019s CPU."
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HelpSection(title: String, content: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
