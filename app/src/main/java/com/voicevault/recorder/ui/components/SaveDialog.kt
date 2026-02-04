package com.voicevault.recorder.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.voicevault.recorder.utils.generateFileName

// dialog for saving recording with name and category - FIXED cancel button
@Composable
fun SaveDialog(
    onDismiss: () -> Unit,
    onSave: (fileName: String, categoryId: Long?) -> Unit
) {
    var fileName by remember { mutableStateOf(generateFileName()) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss, // FIXED: this now properly calls dismiss
        title = { Text("Save recording") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Text("None", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (fileName.isNotBlank()) {
                    onSave(fileName, selectedCategoryId)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { // FIXED: properly calls dismiss
                Text("Cancel")
            }
        }
    )
}