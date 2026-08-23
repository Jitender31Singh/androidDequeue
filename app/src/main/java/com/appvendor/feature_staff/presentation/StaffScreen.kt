package com.appvendor.feature_staff.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appvendor.feature_staff.domain.model.Staff
import com.appvendor.feature_departments.domain.model.Department

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffScreen(
    viewModel: StaffViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Staff", fontWeight = FontWeight.Bold)
                        Text("Manage employees and roles", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.openCreateForm() }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Add", modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New Staff")
                    }
                }
            )
        },
        snackbarHost = {
            state.error?.let { err ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.dismissError() }) {
                            Text("Dismiss")
                        }
                    }
                ) {
                    Text(err)
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.staffList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.PeopleOutline, 
                            contentDescription = null, 
                            modifier = Modifier.size(64.dp), 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("No staff members yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Add employees and assign them to departments.", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant, 
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { viewModel.openCreateForm() }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Staff Member")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.staffList, key = { it.id }) { staff ->
                        StaffItem(
                            staff = staff,
                            onEdit = { viewModel.openEditForm(staff) },
                            onDelete = { viewModel.deleteStaff(staff.id) },
                            onToggleStatus = { active -> viewModel.toggleStatus(staff.id, active) }
                        )
                    }
                }
            }
        }

        if (state.isFormOpen) {
            StaffFormBottomSheet(
                initialData = state.editingStaff,
                departments = state.departments,
                onDismiss = { viewModel.closeForm() },
                onSave = { id, name, email, password, phone, deptId, role, perms -> 
                    viewModel.saveStaff(id, name, email, password, phone, deptId, role, perms)
                },
                isUpdating = state.isUpdating
            )
        }
    }
}

@Composable
fun StaffItem(
    staff: Staff,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleStatus: (Boolean) -> Unit
) {
    val isActive = staff.status == "ACTIVE"
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    
    val cardColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val opacity = if (isActive) 1f else 0.6f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = opacity)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = staff.name.firstOrNull()?.toString()?.uppercase() ?: "S",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = opacity)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(
                            text = staff.name, 
                            style = MaterialTheme.typography.titleMedium, 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = opacity)
                        )
                        Text(
                            text = staff.email, 
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = opacity)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = opacity),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = staff.role,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = opacity)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = staff.departmentName ?: "No Dept", 
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = opacity)
                            )
                        }
                    }
                }
                
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp).offset(x = 8.dp, y = (-4).dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (isActive) "Deactivate" else "Activate") },
                            onClick = { showMenu = false; onToggleStatus(!isActive) }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { showMenu = false; showDeleteConfirm = true }
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete \"${staff.name}\"?") },
            text = { Text("This will permanently remove the staff member. They will no longer have access to the vendor app.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false; onDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffFormBottomSheet(
    initialData: Staff?,
    departments: List<Department>,
    onDismiss: () -> Unit,
    onSave: (String?, String, String, String?, String?, String?, String, List<String>) -> Unit,
    isUpdating: Boolean
) {
    var name by remember { mutableStateOf(initialData?.name ?: "") }
    var email by remember { mutableStateOf(initialData?.email ?: "") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(initialData?.phone ?: "") }
    var selectedDepartmentId by remember { mutableStateOf(initialData?.departmentId ?: "") }
    var selectedRole by remember { mutableStateOf(initialData?.role ?: "KITCHEN_STAFF") }
    
    val roles = listOf("ADMIN", "MANAGER", "KITCHEN_STAFF", "COUNTER_STAFF", "DELIVERY")

    ModalBottomSheet(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (initialData == null) "New Staff Member" else "Edit Staff Member",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            if (initialData == null) {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Temporary Password") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }
            
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(Modifier.height(8.dp))
            Text("Role", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            // Modern Role Selector
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roles.chunked(2).forEach { rowRoles ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowRoles.forEach { role ->
                            FilterChip(
                                selected = selectedRole == role,
                                onClick = { selectedRole = role },
                                label = { Text(role, modifier = Modifier.padding(vertical = 4.dp)) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowRoles.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(8.dp))
            Text("Department", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            
            var expandedDept by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedDept,
                onExpandedChange = { expandedDept = it }
            ) {
                OutlinedTextField(
                    value = departments.find { it.id == selectedDepartmentId }?.name ?: "None",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assign Department") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDept) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedDept,
                    onDismissRequest = { expandedDept = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = { 
                            selectedDepartmentId = ""
                            expandedDept = false 
                        }
                    )
                    departments.forEach { dept ->
                        DropdownMenuItem(
                            text = { Text(dept.name) },
                            onClick = { 
                                selectedDepartmentId = dept.id
                                expandedDept = false 
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    onSave(
                        initialData?.id, 
                        name.trim(), 
                        email.trim(), 
                        password.ifBlank { null }, 
                        phone.trim().ifEmpty { null }, 
                        selectedDepartmentId.ifEmpty { null }, 
                        selectedRole, 
                        emptyList()
                    )
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotBlank() && email.isNotBlank() && (initialData != null || password.isNotBlank()) && !isUpdating,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isUpdating) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(if (initialData == null) "Create Staff Member" else "Save Changes")
                }
            }
        }
    }
}
