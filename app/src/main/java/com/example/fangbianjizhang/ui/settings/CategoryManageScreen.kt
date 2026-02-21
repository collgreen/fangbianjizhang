package com.example.fangbianjizhang.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.CategoryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    onBack: () -> Unit,
    viewModel: CategoryManageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var addName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("分类管理") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = if (state.type == CategoryType.EXPENSE) 0 else 1) {
                Tab(
                    selected = state.type == CategoryType.EXPENSE,
                    onClick = { viewModel.setType(CategoryType.EXPENSE) },
                    text = { Text("支出分类") }
                )
                Tab(
                    selected = state.type == CategoryType.INCOME,
                    onClick = { viewModel.setType(CategoryType.INCOME) },
                    text = { Text("收入分类") }
                )
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.categories, key = { it.id }) { cat ->
                    CategoryManageItem(
                        name = cat.name,
                        icon = cat.icon,
                        children = state.childrenMap[cat.id] ?: emptyList(),
                        onExpand = { viewModel.loadChildren(cat.id) }
                    )
                }
                item {
                    TextButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(4.dp))
                        Text("添加一级分类")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("添加分类") },
            text = {
                OutlinedTextField(
                    value = addName,
                    onValueChange = { addName = it },
                    label = { Text("分类名称") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (addName.isNotBlank()) {
                        viewModel.addCategory(addName.trim())
                        addName = ""
                        showAddDialog = false
                    }
                }) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("取消") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryManageItem(
    name: String,
    icon: String,
    children: List<com.example.fangbianjizhang.domain.model.Category>,
    onExpand: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth().clickable {
                    expanded = !expanded
                    if (expanded) onExpand()
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(icon, modifier = Modifier.padding(end = 8.dp))
                Text(name, style = MaterialTheme.typography.titleSmall)
            }
            if (expanded && children.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    children.forEach { child ->
                        AssistChip(
                            onClick = {},
                            label = { Text(child.name) }
                        )
                    }
                }
            }
        }
    }
}
