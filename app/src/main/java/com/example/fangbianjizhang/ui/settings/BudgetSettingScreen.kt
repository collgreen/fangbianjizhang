package com.example.fangbianjizhang.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.BudgetMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSettingScreen(
    onBack: () -> Unit,
    viewModel: BudgetSettingViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("预算设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.save() }) { Text("保存") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ModeSelector(state.mode) { viewModel.setMode(it) } }

            if (state.mode == BudgetMode.TOTAL) {
                item {
                    OutlinedTextField(
                        value = state.totalBudget,
                        onValueChange = { viewModel.setTotalAmount(it) },
                        label = { Text("每月总预算 (元)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (state.mode == BudgetMode.PER_CATEGORY) {
                items(state.categories, key = { it.id }) { cat ->
                    OutlinedTextField(
                        value = state.categoryBudgets[cat.id] ?: "",
                        onValueChange = { viewModel.setCategoryAmount(cat.id, it) },
                        label = { Text("${cat.icon} ${cat.name}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(current: BudgetMode, onSelect: (BudgetMode) -> Unit) {
    Column {
        Text("预算模式", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        BudgetMode.entries.forEach { mode ->
            Row(Modifier.fillMaxWidth()) {
                RadioButton(
                    selected = mode == current,
                    onClick = { onSelect(mode) }
                )
                Text(
                    text = mode.displayName(),
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp)
                )
            }
        }
    }
}

private fun BudgetMode.displayName() = when (this) {
    BudgetMode.NONE -> "不设预算"
    BudgetMode.TOTAL -> "总额预算"
    BudgetMode.PER_CATEGORY -> "分类预算"
}
