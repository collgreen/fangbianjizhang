package com.example.fangbianjizhang.ui.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.foundation.text.selection.SelectionContainer
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
    viewModel: SettingsViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val updateState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val importConflict by viewModel.importConflict.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showStartDayDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
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
        item { SettingsItem("账户管理") { navController.navigate(Routes.ACCOUNT_MANAGE) } }
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
                Text("自动检查更新")
                Switch(
                    checked = state.autoCheckUpdate,
                    onCheckedChange = { viewModel.setAutoCheckUpdate(it) }
                )
            }
        }
        item { SettingsItem("手动检查更新") { updateViewModel.checkUpdate() } }
        item { SettingsItem("关于", "v${updateViewModel.currentVersion}") { showAboutDialog = true } }
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

    importConflict?.let { (_, names) ->
        AlertDialog(
            onDismissRequest = { viewModel.cancelImport() },
            title = { Text("发现同名账户") },
            text = { Text("以下账户已存在，导入将覆盖其数据：\n${names.joinToString("、")}\n\n确定要覆盖吗？") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.confirmOverwrite { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                }) { Text("覆盖导入") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.cancelImport() }) { Text("取消") }
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(
            version = updateViewModel.currentVersion,
            onDismiss = { showAboutDialog = false }
        )
    }

    UpdateDialog(updateState, updateViewModel)
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsItem(title: String, value: String = "", onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge)
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

@Composable
private fun AboutDialog(version: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("方便记账 v$version") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("GitHub 开源地址：")
                SelectionContainer {
                    Text(
                        "github.com/collgreen/fangbianjizhang",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    "本项目永久免费、完全开源。\n\n" +
                    "没有广告，没有会员，没有「尊贵的VIP才能记超过100笔」这种离谱操作。\n\n" +
                    "你的每一分钱都应该被好好记录，而不是先花钱买个记账App。\n\n" +
                    "如果觉得好用，给个 Star 就是最好的支持 ⭐",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("知道了") } }
    )
}

@Composable
private fun UpdateDialog(state: UpdateUiState, vm: UpdateViewModel) {
    when {
        state.checking -> {
            AlertDialog(
                onDismissRequest = { vm.dismiss() },
                title = { Text("检查更新") },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text("正在检查...")
                    }
                },
                confirmButton = {}
            )
        }
        state.downloading -> {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("下载中") },
                text = {
                    Column {
                        LinearProgressIndicator(
                            progress = { state.progress / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("${state.progress}%")
                    }
                },
                confirmButton = {}
            )
        }
        state.error == "already_latest" -> {
            AlertDialog(
                onDismissRequest = { vm.dismiss() },
                title = { Text("检查更新") },
                text = { Text("当前已是最新版本") },
                confirmButton = { TextButton(onClick = { vm.dismiss() }) { Text("确定") } }
            )
        }
        state.error != null -> {
            AlertDialog(
                onDismissRequest = { vm.dismiss() },
                title = { Text("检查失败") },
                text = { Text(state.error) },
                confirmButton = { TextButton(onClick = { vm.dismiss() }) { Text("确定") } }
            )
        }
        state.latestVersion != null -> {
            AlertDialog(
                onDismissRequest = { vm.dismiss() },
                title = { Text("发现新版本 v${state.latestVersion}") },
                text = {
                    if (state.downloadUrl != null) Text("是否下载并安装更新？")
                    else Text("请前往 GitHub Releases 下载")
                },
                confirmButton = {
                    if (state.downloadUrl != null) {
                        TextButton(onClick = { vm.downloadAndInstall() }) { Text("下载更新") }
                    }
                },
                dismissButton = { TextButton(onClick = { vm.dismiss() }) { Text("取消") } }
            )
        }
    }
}
