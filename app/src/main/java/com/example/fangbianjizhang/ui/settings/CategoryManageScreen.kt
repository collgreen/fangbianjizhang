package com.example.fangbianjizhang.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(
    onBack: () -> Unit,
    viewModel: CategoryManageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dialogState by remember { mutableStateOf<CatDialogState>(CatDialogState.None) }

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
                Tab(selected = state.type == CategoryType.EXPENSE, onClick = { viewModel.setType(CategoryType.EXPENSE) }, text = { Text("支出分类") })
                Tab(selected = state.type == CategoryType.INCOME, onClick = { viewModel.setType(CategoryType.INCOME) }, text = { Text("收入分类") })
            }

            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.categories, key = { it.id }) { cat ->
                    CategoryManageItem(
                        category = cat,
                        children = state.childrenMap[cat.id] ?: emptyList(),
                        onExpand = { viewModel.loadChildren(cat.id) },
                        onRename = { dialogState = CatDialogState.Rename(it) },
                        onDelete = { dialogState = CatDialogState.Delete(it) },
                        onAddChild = { dialogState = CatDialogState.AddChild(it) }
                    )
                }
                item {
                    TextButton(onClick = { dialogState = CatDialogState.AddTop }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("添加一级分类")
                    }
                }
            }
        }
    }

    CategoryDialogs(dialogState, viewModel) { dialogState = CatDialogState.None }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryManageItem(
    category: Category,
    children: List<Category>,
    onExpand: () -> Unit,
    onRename: (Category) -> Unit,
    onDelete: (Category) -> Unit,
    onAddChild: (Long) -> Unit
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
                Text(category.icon, modifier = Modifier.padding(end = 8.dp))
                Text(category.name, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                IconButton(onClick = { onRename(category) }) { Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(18.dp)) }
                IconButton(onClick = { onDelete(category) }) { Icon(Icons.Default.Delete, "删除", modifier = Modifier.size(18.dp)) }
            }
            if (expanded) {
                if (children.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        children.forEach { child ->
                            InputChip(
                                selected = false,
                                onClick = { onRename(child) },
                                label = { Text(child.name) },
                                trailingIcon = {
                                    Icon(Icons.Default.Close, "删除", modifier = Modifier.size(16.dp).clickable { onDelete(child) })
                                }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(4.dp))
                TextButton(onClick = { onAddChild(category.id) }) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("添加子分类")
                }
            }
        }
    }
}

private sealed class CatDialogState {
    data object None : CatDialogState()
    data object AddTop : CatDialogState()
    data class AddChild(val parentId: Long) : CatDialogState()
    data class Rename(val category: Category) : CatDialogState()
    data class Delete(val category: Category) : CatDialogState()
}

@Composable
private fun CategoryDialogs(
    state: CatDialogState,
    viewModel: CategoryManageViewModel,
    onDismiss: () -> Unit
) {
    when (state) {
        CatDialogState.None -> {}
        CatDialogState.AddTop -> {
            NameInputDialog("添加一级分类", "") { name ->
                viewModel.addCategory(name)
                onDismiss()
            }
        }
        is CatDialogState.AddChild -> {
            NameInputDialog("添加子分类", "") { name ->
                viewModel.addSubCategory(state.parentId, name)
                onDismiss()
            }
        }
        is CatDialogState.Rename -> {
            NameInputDialog("重命名", state.category.name) { name ->
                viewModel.renameCategory(state.category.id, name)
                onDismiss()
            }
        }
        is CatDialogState.Delete -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("确认删除") },
                text = { Text("确定删除「${state.category.name}」吗？") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteCategory(state.category.id)
                        onDismiss()
                    }) { Text("删除") }
                },
                dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
            )
        }
    }
}

@Composable
private fun NameInputDialog(title: String, initial: String, onConfirm: (String) -> Unit) {
    var name by remember { mutableStateOf(initial) }
    var dismissed by remember { mutableStateOf(false) }
    if (dismissed) return

    AlertDialog(
        onDismissRequest = { dismissed = true },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("名称") }, singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name.trim()) }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = { dismissed = true }) { Text("取消") } }
    )
}
