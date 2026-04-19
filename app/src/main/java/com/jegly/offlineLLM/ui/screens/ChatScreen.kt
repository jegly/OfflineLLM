package com.jegly.offlineLLM.ui.screens

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jegly.offlineLLM.ai.ModelManager
import com.jegly.offlineLLM.data.local.entities.Conversation
import com.jegly.offlineLLM.ui.components.InputBar
import com.jegly.offlineLLM.ui.components.LoadingIndicator
import com.jegly.offlineLLM.ui.components.MessageBubble
import com.jegly.offlineLLM.ui.components.StreamingMessage
import kotlinx.coroutines.launch

private val SensitiveDataKey = SemanticsPropertyKey<Boolean>("SensitiveData")
private var SemanticsPropertyReceiver.sensitiveData by SensitiveDataKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sensitiveDataModifier = if (
        uiState.sensitiveDataAccessibilityEnabled &&
        Build.VERSION.SDK_INT >= 36 // Android 16+
    ) {
        Modifier.semantics { sensitiveData = true }
    } else {
        Modifier
    }
    var inputText by rememberSaveable { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<com.jegly.offlineLLM.data.local.entities.Message?>(null) }
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.initialize()
    }

    val isAtBottom by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val total = listState.layoutInfo.totalItemsCount
            lastVisible == null || lastVisible.index >= total - 2
        }
    }

    // isDragged is ONLY true when the user's finger is physically dragging —
    // not during flings, not during programmatic scrolls. This lets us
    // distinguish user intent from auto-scroll animations.
    val isDragged by listState.interactionSource.collectIsDraggedAsState()

    // Sticky flag: only a real finger drag away from the bottom disables it.
    // Re-enabled as soon as the list is back at the bottom.
    var autoScrollEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(isDragged, isAtBottom) {
        when {
            isAtBottom -> autoScrollEnabled = true
            isDragged && !isAtBottom -> autoScrollEnabled = false
        }
    }

    LaunchedEffect(uiState.messages.size, uiState.partialResponse) {
        if (autoScrollEnabled) {
            val totalItems = uiState.messages.size + (if (uiState.isGenerating) 1 else 0)
            if (totalItems > 0) {
                listState.scrollToItem(totalItems - 1)
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    LaunchedEffect(uiState.showConversationDrawer) {
        if (uiState.showConversationDrawer) drawerState.open() else drawerState.close()
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ConversationDrawer(
                conversations = uiState.conversations,
                currentId = uiState.currentConversation?.id,
                onSelect = { viewModel.switchConversation(it) },
                onDelete = { viewModel.deleteConversation(it.id) },
                onRename = { conv, newTitle -> viewModel.renameConversation(conv.id, newTitle) },
                onNew = { viewModel.newConversation() },
            )
        },
        gesturesEnabled = true,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = uiState.currentConversation?.title ?: "OfflineLLM",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            when (uiState.modelState) {
                                is ModelManager.ModelState.Loading -> {
                                    Text(
                                        text = "Loading model\u2026",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                is ModelManager.ModelState.Ready -> {
                                    uiState.tokensPerSecond?.let { tps ->
                                        Text(
                                            text = String.format("%.1f tok/s", tps),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                is ModelManager.ModelState.Error -> {
                                    Text(
                                        text = "Model error",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                                else -> {}
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showSearch = !showSearch; if (!showSearch) searchQuery = "" }) {
                            Icon(
                                if (showSearch) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = "Search"
                            )
                        }
                        IconButton(onClick = { viewModel.newConversation() }) {
                            Icon(Icons.Filled.Add, contentDescription = "New Chat")
                        }
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .then(sensitiveDataModifier)
            ) {
                // Search bar
                if (showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        placeholder = { Text("Search messages\u2026") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Clear")
                                }
                            }
                        },
                    )
                }

                // Filter messages by search
                val displayMessages = if (searchQuery.isBlank()) uiState.messages
                    else uiState.messages.filter { it.content.contains(searchQuery, ignoreCase = true) }

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (uiState.messages.isEmpty() && !uiState.isGenerating) {
                        item {
                            Box(
                                modifier = Modifier.fillParentMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "OfflineLLM",
                                        style = MaterialTheme.typography.headlineMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = when (uiState.modelState) {
                                            is ModelManager.ModelState.Loading -> "Loading model\u2026"
                                            is ModelManager.ModelState.NotLoaded -> "No model loaded yet.\nGo to Settings to import a GGUF model."
                                            is ModelManager.ModelState.Error -> (uiState.modelState as ModelManager.ModelState.Error).message
                                            is ModelManager.ModelState.Ready -> "Send a message to start chatting"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    )
                                    if (uiState.modelState is ModelManager.ModelState.Loading) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }
                    }

                    items(displayMessages, key = { it.id }) { message ->
                        MessageBubble(
                            content = message.content,
                            isUser = message.role == "user",
                            onLongPress = { deleteTarget = message },
                            onSpeak = if (message.role == "assistant") {
                                { viewModel.speakMessage(message.id, message.content) }
                            } else null,
                            onStopSpeaking = { viewModel.stopSpeaking() },
                            isSpeaking = uiState.speakingMessageId == message.id,
                        )
                    }

                    if (uiState.isGenerating) {
                        item {
                            if (uiState.partialResponse.isNotEmpty()) {
                                StreamingMessage(partialResponse = uiState.partialResponse)
                            } else {
                                LoadingIndicator()
                            }
                        }
                    }
                }

                HorizontalDivider()
                InputBar(
                    value = inputText,
                    onValueChange = { inputText = it },
                    onSend = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    onStop = { viewModel.stopGeneration() },
                    isGenerating = uiState.isGenerating,
                    enabled = uiState.modelState is ModelManager.ModelState.Ready,
                )
            }
        }
    }

    // Delete message dialog
    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Message") },
            text = { Text("Delete this message?") },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget?.let { viewModel.deleteMessage(it.id) }
                    deleteTarget = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}

@Composable
private fun ConversationDrawer(
    conversations: List<Conversation>,
    currentId: String?,
    onSelect: (Conversation) -> Unit,
    onDelete: (Conversation) -> Unit,
    onRename: (Conversation, String) -> Unit,
    onNew: () -> Unit,
) {
    var renameTarget by remember { mutableStateOf<Conversation?>(null) }
    var renameText by remember { mutableStateOf("") }

    ModalDrawerSheet(modifier = Modifier.width(300.dp)) {
        Text(
            text = "Conversations",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text("New Chat") },
            selected = false,
            onClick = onNew,
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            modifier = Modifier.padding(horizontal = 12.dp),
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

        LazyColumn {
            items(conversations) { conversation ->
                NavigationDrawerItem(
                    label = {
                        Text(
                            text = conversation.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    selected = conversation.id == currentId,
                    onClick = { onSelect(conversation) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                    badge = {
                        Row {
                            IconButton(onClick = {
                                renameTarget = conversation
                                renameText = conversation.title
                            }) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Rename",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                )
                            }
                            IconButton(onClick = { onDelete(conversation) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    // Rename dialog
    if (renameTarget != null) {
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename Chat") },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Chat name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        renameTarget?.let { onRename(it, renameText) }
                        renameTarget = null
                    },
                    enabled = renameText.isNotBlank(),
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) {
                    Text("Cancel")
                }
            },
        )
    }
}
