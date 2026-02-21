package com.example.fangbianjizhang.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fangbianjizhang.domain.model.ThemeMode
import com.example.fangbianjizhang.ui.navigation.Routes

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showStartDayDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportJson(it) { Toast.makeText(context, "导出成功", Toast.LENGTH_SHORT).show() } }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importJson(it) { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item { SectionHeader("功能") }
        item { SettingsItem("分类管理") { navController.navigate(Routes.CATEGORY_MANAGE) } }
        item { SettingsItem("预算设置") { navController.navigate(Routes.BUDGET_SETTING) } }
        item { SettingsItem("定期扣款") { navController.navigate(Routes.RECURRING_MANAGE) } }

        item { SectionHeader("通用") }
        item {
            SettingsItem("主题", state.theme.displayName()) {
                showThemeDialog = true
            }
        }
        item { SettingsItem("货币符号", state.currencySymbol) { showCurrencyDialog = true } }
        item { SettingsItem("每月起始日", "${state.monthStartDay}号") { showStartDayDialog = true } }

        item { SectionHeader("数据") }
        item { SettingsItem("导出数据") { exportLauncher.launch("fangbianjizhang_backup.json") } }
        item { SettingsItem("导入数据") { importLauncher.launch(arrayOf("application/json")) } }

        item { SectionHeader("其他") }
        item {
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("检查更新")
                Switch(
                    checked = state.autoCheckUpdate,
                    onCheckedChange = { viewModel.setAutoCheckUpdate(it) }
                )
            }
        }
        item { SettingsItem("关于", "v1.0.0") {} }
    }

    if (showThemeDialog) {
        ThemeDialog(
            current = state.theme,
            onSelect = { viewModel.setTheme(it); showThemeDialog = false },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showCurrencyDialog) {
        CurrencyDialog(
            current = state.currencySymbol,
            onSelect = { viewModel.setCurrency(it); showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showStartDayDialog) {
        StartDayDialog(
            current = state.monthStartDay,
            onSelect = { viewModel.setMonthStartDay(it); showStartDayDialog = false },
            onDismiss = { showStartDayDialog = false }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsItem(title: String, value: String = "", onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title)
        if (value.isNotEmpty()) Text(value, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun ThemeDialog(current: ThemeMode, onSelect: (ThemeMode) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择主题") },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(mode) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = mode == current, onClick = { onSelect(mode) })
                        Spacer(Modifier.width(8.dp))
                        Text(mode.displayName())
                    }
                }
            }
        },
        confirmButton = {}
    )
}

private fun ThemeMode.displayName() = when (this) {
    ThemeMode.LIGHT -> "浅色"
    ThemeMode.DARK -> "深色"
    ThemeMode.SYSTEM -> "跟随系统"
}

@Composable
private fun CurrencyDialog(current: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    val options = listOf("¥" to "人民币", "$" to "美元", "€" to "欧元", "£" to "英镑")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择货币符号") },
        text = {
            Column {
                options.forEach { (symbol, name) ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(symbol) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = symbol == current, onClick = { onSelect(symbol) })
                        Spacer(Modifier.width(8.dp))
                        Text("$symbol $name")
                    }
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun StartDayDialog(current: Int, onSelect: (Int) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("每月起始日") },
        text = {
            Column {
                listOf(1, 5, 10, 15, 20, 25).forEach { day ->
                    Row(
                        Modifier.fillMaxWidth().clickable { onSelect(day) }.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = day == current, onClick = { onSelect(day) })
                        Spacer(Modifier.width(8.dp))
                        Text("${day}号")
                    }
                }
            }
        },
        confirmButton = {}
    )
}
