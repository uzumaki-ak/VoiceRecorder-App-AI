package com.voicevault.recorder.ui.api

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.voicevault.recorder.utils.Constants

// api settings screen for configuring llm providers
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiSettingsViewModel = viewModel()
) {
    val selectedProvider by viewModel.selectedProvider.collectAsState()
    val selectedModel by viewModel.selectedModel.collectAsState()
    val apiKey by viewModel.apiKey.collectAsState()
    val availableModels by viewModel.availableModels.collectAsState()
    val testResult by viewModel.testResult.collectAsState()
    val isTesting by viewModel.isTesting.collectAsState()

    var showProviderMenu by remember { mutableStateOf(false) }
    var showModelMenu by remember { mutableStateOf(false) }
    var showApiKey by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI API Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // provider info card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Configure your AI provider for chat features",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // provider selection
            Text("Provider", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = showProviderMenu,
                onExpandedChange = { showProviderMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedProvider.replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showProviderMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = showProviderMenu,
                    onDismissRequest = { showProviderMenu = false }
                ) {
                    listOf(
                        Constants.PROVIDER_GROQ,
                        Constants.PROVIDER_GEMINI,
                        Constants.PROVIDER_EURON,
                        Constants.PROVIDER_OPENROUTER,
                        Constants.PROVIDER_MISTRAL
                    ).forEach { provider ->
                        DropdownMenuItem(
                            text = { Text(provider.replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                viewModel.setProvider(provider)
                                showProviderMenu = false
                            }
                        )
                    }
                }
            }

            // model selection
            Text("Model", style = MaterialTheme.typography.titleMedium)

            ExposedDropdownMenuBox(
                expanded = showModelMenu,
                onExpandedChange = { showModelMenu = it }
            ) {
                OutlinedTextField(
                    value = selectedModel,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showModelMenu) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )

                ExposedDropdownMenu(
                    expanded = showModelMenu,
                    onDismissRequest = { showModelMenu = false }
                ) {
                    availableModels.forEach { model ->
                        DropdownMenuItem(
                            text = { Text(model) },
                            onClick = {
                                viewModel.setModel(model)
                                showModelMenu = false
                            }
                        )
                    }
                }
            }

            // api key input
            Text("API Key", style = MaterialTheme.typography.titleMedium)

            OutlinedTextField(
                value = apiKey,
                onValueChange = { viewModel.setApiKey(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter your API key") },
                visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showApiKey = !showApiKey }) {
                        Icon(
                            if (showApiKey) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle visibility"
                        )
                    }
                }
            )

            // provider info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Get your API key:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = when (selectedProvider) {
                            Constants.PROVIDER_GROQ -> "https://console.groq.com/keys"
                            Constants.PROVIDER_GEMINI -> "https://aistudio.google.com/app/apikey"
                            Constants.PROVIDER_EURON -> "https://euron.one/api-keys"
                            Constants.PROVIDER_OPENROUTER -> "https://openrouter.ai/keys"
                            Constants.PROVIDER_MISTRAL -> "https://console.mistral.ai/api-keys"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // test connection button
            Button(
                onClick = { viewModel.testConnection() },
                modifier = Modifier.fillMaxWidth(),
                enabled = apiKey.isNotBlank() && !isTesting
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isTesting) "Testing..." else "Test Connection")
            }

            // test result
            testResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (result.contains("successful")) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Text(
                        text = result,
                        modifier = Modifier.padding(16.dp),
                        color = if (result.contains("successful")) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }

            // save button
            Button(
                onClick = {
                    viewModel.saveApiKey()
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = apiKey.isNotBlank()
            ) {
                Text("Save")
            }
        }
    }
}