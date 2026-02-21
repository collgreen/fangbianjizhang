package com.example.fangbianjizhang.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.Frequency
import com.example.fangbianjizhang.domain.model.RecurringTemplate
import com.example.fangbianjizhang.util.AmountFormatter
import com.example.fangbianjizhang.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringManageScreen(
    onBack: () -> Unit,
    viewModel: RecurringManageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("定期扣款") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, "添加")
                    }
                }
            )
        }
    ) { padding ->
        if (state.templates.isEmpty()) {
            Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无定期扣款，点击右上角 + 添加")
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.templates, key = { it.id }) { template ->
                    RecurringItem(
                        template = template,
                        accountName = { id ->
                            state.accounts.firstOrNull { it.id == id }?.name ?: ""
                        },
                        onToggle = { viewModel.toggleEnabled(template.id, it) },
                        onDelete = { viewModel.delete(template.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddRecurringDialog(
            accounts = state.accounts.map { it.id to it.name },
            onConfirm = { name, amount, freq, src, tgt, day ->
                viewModel.add(name, amount, freq, src, tgt, day)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
private fun RecurringItem(
    template: RecurringTemplate,
    accountName: (Long) -> String,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(template.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    AmountFormatter.toDisplayWithSymbol(template.amount),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "${template.frequency.displayName()} | ${accountName(template.sourceAccountId)} -> ${accountName(template.targetAccountId)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "下次执行: ${DateUtils.formatDay(template.nextDueDate)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Switch(checked = template.isEnabled, onCheckedChange = onToggle)
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, "删除")
            }
        }
    }
}

private fun Frequency.displayName() = when (this) {
    Frequency.MONTHLY -> "每月"
    Frequency.WEEKLY -> "每周"
    Frequency.CUSTOM_DAYS -> "自定义天数"
}

@Composable
private fun AddRecurringDialog(
    accounts: List<Pair<Long, String>>,
    onConfirm: (String, String, Frequency, Long, Long, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf(Frequency.MONTHLY) }
    var dayValue by remember { mutableStateOf("1") }
    var srcIndex by remember { mutableIntStateOf(0) }
    var tgtIndex by remember { mutableIntStateOf(if (accounts.size > 1) 1 else 0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加定期扣款") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("名称") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("金额 (元)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("频率", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Frequency.entries.forEach { f ->
                        FilterChip(
                            selected = frequency == f,
                            onClick = { frequency = f },
                            label = { Text(f.displayName()) }
                        )
                    }
                }
                OutlinedTextField(
                    value = dayValue, onValueChange = { dayValue = it },
                    label = {
                        Text(
                            when (frequency) {
                                Frequency.MONTHLY -> "每月几号"
                                Frequency.WEEKLY -> "周几 (1-7)"
                                Frequency.CUSTOM_DAYS -> "间隔天数"
                            }
                        )
                    },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                if (accounts.isNotEmpty()) {
                    AccountPicker("来源账户", accounts, srcIndex) { srcIndex = it }
                    AccountPicker("目标账户", accounts, tgtIndex) { tgtIndex = it }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && amount.isNotBlank() && accounts.isNotEmpty()) {
                    onConfirm(
                        name.trim(), amount.trim(), frequency,
                        accounts[srcIndex].first, accounts[tgtIndex].first,
                        dayValue.toIntOrNull()
                    )
                }
            }) { Text("确定") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
}

@Composable
private fun AccountPicker(
    label: String,
    accounts: List<Pair<Long, String>>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit
) {
    Text(label, style = MaterialTheme.typography.bodySmall)
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        accounts.forEachIndexed { i, (_, name) ->
            FilterChip(
                selected = i == selectedIndex,
                onClick = { onSelect(i) },
                label = { Text(name) }
            )
        }
    }
}
