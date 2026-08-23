package com.appvendor.feature_items.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.appvendor.feature_items.domain.model.Category
import com.appvendor.feature_items.domain.model.FieldType
import com.appvendor.feature_items.domain.model.Item
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemSheet(
    category: Category,
    itemToEdit: Item? = null,
    onDismiss: () -> Unit,
    onSave: (Item) -> Unit
) {
    var name by remember { mutableStateOf(itemToEdit?.name ?: "") }
    var description by remember { mutableStateOf(itemToEdit?.description ?: "") }
    var price by remember { mutableStateOf(itemToEdit?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(itemToEdit?.stockQuantity?.toString() ?: "") }
    
    val dynamicFields = remember { mutableStateMapOf<String, String>() }
    
    LaunchedEffect(itemToEdit) {
        if (itemToEdit != null) {
            dynamicFields.putAll(itemToEdit.dynamicFields)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = if (itemToEdit == null) "Add Item to ${category.name}" else "Edit Item",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f, fill = false),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Price") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock Quantity") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                
                if (category.fields.isNotEmpty()) {
                    item {
                        Text(
                            "Dynamic Fields", 
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    
                    category.fields.forEach { field ->
                        item {
                            when (field.type) {
                                FieldType.TEXT, FieldType.NUMBER -> {
                                    OutlinedTextField(
                                        value = dynamicFields[field.id] ?: "",
                                        onValueChange = { dynamicFields[field.id] = it },
                                        label = { Text(field.name) },
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = if (field.type == FieldType.NUMBER) KeyboardType.Number else KeyboardType.Text
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                                FieldType.BOOLEAN -> {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                                    ) {
                                        Text(field.name, modifier = Modifier.weight(1f))
                                        Switch(
                                            checked = dynamicFields[field.id] == "true",
                                            onCheckedChange = { dynamicFields[field.id] = it.toString() }
                                        )
                                    }
                                }
                                FieldType.SELECT -> {
                                    // Simplified select as text field for this demo
                                    OutlinedTextField(
                                        value = dynamicFields[field.id] ?: "",
                                        onValueChange = { dynamicFields[field.id] = it },
                                        label = { Text("${field.name} (Select)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.padding(end = 8.dp)) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val parsedPrice = price.toDoubleOrNull() ?: 0.0
                        val parsedStock = stock.toIntOrNull() ?: 0
                        
                        onSave(
                            Item(
                                id = itemToEdit?.id ?: UUID.randomUUID().toString(),
                                categoryId = category.id,
                                name = name,
                                description = description,
                                price = parsedPrice,
                                stockQuantity = parsedStock,
                                imageUrl = itemToEdit?.imageUrl,
                                dynamicFields = dynamicFields.toMap()
                            )
                        )
                        onDismiss()
                    },
                    enabled = name.isNotBlank() && price.isNotBlank() && stock.isNotBlank()
                ) {
                    Text("Save")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
