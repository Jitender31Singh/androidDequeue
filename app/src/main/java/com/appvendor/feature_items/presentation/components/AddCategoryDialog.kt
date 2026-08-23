package com.appvendor.feature_items.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.model.CategoryField
import com.appvendor.feature_items.domain.model.FieldType
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onSave: (Category) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var fields by remember { mutableStateOf(listOf<CategoryField>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Category") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Category Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Dynamic Fields", 
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = {
                            fields = fields + CategoryField(
                                id = UUID.randomUUID().toString(),
                                name = "",
                                type = FieldType.TEXT,
                                isRequired = false
                            )
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Field")
                        }
                    }
                }

                itemsIndexed(fields) { index, field ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = field.name,
                                    onValueChange = { newName ->
                                        val updatedFields = fields.toMutableList()
                                        updatedFields[index] = field.copy(name = newName)
                                        fields = updatedFields
                                    },
                                    label = { Text("Field Name") },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    val updatedFields = fields.toMutableList()
                                    updatedFields.removeAt(index)
                                    fields = updatedFields
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Remove Field", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Category(
                                id = UUID.randomUUID().toString(),
                                name = name,
                                description = description,
                                fields = fields
                            )
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
