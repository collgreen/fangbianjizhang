package com.example.fangbianjizhang.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountSubType
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.util.AmountFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountManageScreen(
    onBack: () -> Unit,
    viewModel: AccountManageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAccount by remember { mutableStateOf<Account?>(null) }
    var deletingId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("账户管理") },
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
        val grouped = state.accounts.groupBy { it.type }
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountType.entries.forEach { type ->
                val list = grouped[type] ?: emptyList()
                if (list.isNotEmpty()) {
                    item { Text(type.displayName(), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp)) }
                    items(list, key = { it.id }) { account ->
                        AccountItem(
                            account = account,
                            onEdit = { editingAccount = account },
                            onDelete = { deletingId = account.id }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AccountEditDialog(
            account = null,
            onConfirm = { name, type, subType, balance, limit, loan, monthly, billDay, repayDay ->
                viewModel.addAccount(name, type, subType, balance, limit, loan, monthly, billDay, repayDay)
                showAddDialog = false
            },
            onDismiss = { showAddDialog = false }
        )
    }

    editingAccount?.let { acc ->
        AccountEditDialog(
            account = acc,
            onConfirm = { name, type, subType, balance, limit, loan, monthly, billDay, repayDay ->
                viewModel.updateAccount(acc.copy(
                    name = name, type = type, subType = subType,
                    balance = balance, totalLimit = limit, totalLoan = loan,
                    monthlyPayment = monthly, billDay = billDay, repaymentDay = repayDay
                ))
                editingAccount = null
            },
            onDismiss = { editingAccount = null }
        )
    }

    deletingId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingId = null },
            title = { Text("确认删除") },
            text = { Text("删除后账户数据将不可恢复，确定删除吗？") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteAccount(id); deletingId = null }) { Text("删除") }
            },
            dismissButton = {
                TextButton(onClick = { deletingId = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun AccountItem(account: Account, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(account.icon, modifier = Modifier.padding(end = 8.dp))
            Column(Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.titleSmall)
                Text(account.subType.displayName(), style = MaterialTheme.typography.bodySmall)
            }
            Text(AmountFormatter.toDisplayWithSymbol(account.balance))
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "编辑") }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "删除") }
        }
    }
}

@Composable
private fun AccountEditDialog(
    account: Account?,
    onConfirm: (String, AccountType, AccountSubType, Long, Long?, Long?, Long?, Int?, Int?) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var selectedType by remember { mutableStateOf(account?.type ?: AccountType.FUND) }
    var selectedSubType by remember { mutableStateOf(account?.subType ?: AccountSubType.BANK_CARD) }
    var balanceStr by remember { mutableStateOf(if (account != null) AmountFormatter.toDisplay(account.balance) else "") }
    var limitStr by remember { mutableStateOf(account?.totalLimit?.let { AmountFormatter.toDisplay(it) } ?: "") }
    var loanStr by remember { mutableStateOf(account?.totalLoan?.let { AmountFormatter.toDisplay(it) } ?: "") }
    var monthlyStr by remember { mutableStateOf(account?.monthlyPayment?.let { AmountFormatter.toDisplay(it) } ?: "") }
    var billDayStr by remember { mutableStateOf(account?.billDay?.toString() ?: "") }
    var repayDayStr by remember { mutableStateOf(account?.repaymentDay?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) "添加账户" else "编辑账户") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("名称") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                if (account == null) {
                    Text("账户类型", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        AccountType.entries.forEach { t ->
                            FilterChip(selected = selectedType == t, onClick = {
                                selectedType = t
                                selectedSubType = t.defaultSubTypes().first()
                            }, label = { Text(t.displayName()) })
                        }
                    }
                    Text("子类型", style = MaterialTheme.typography.bodySmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        selectedType.defaultSubTypes().forEach { st ->
                            FilterChip(selected = selectedSubType == st, onClick = { selectedSubType = st }, label = { Text(st.displayName()) })
                        }
                    }
                }

                if (selectedType == AccountType.FUND || selectedType == AccountType.INVESTMENT) {
                    OutlinedTextField(value = balanceStr, onValueChange = { balanceStr = it }, label = { Text("余额 (元)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                if (selectedType == AccountType.CREDIT) {
                    OutlinedTextField(value = limitStr, onValueChange = { limitStr = it }, label = { Text("额度 (元)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = billDayStr, onValueChange = { billDayStr = it }, label = { Text("账单日") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = repayDayStr, onValueChange = { repayDayStr = it }, label = { Text("还款日") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                }

                if (selectedType == AccountType.LOAN) {
                    OutlinedTextField(value = loanStr, onValueChange = { loanStr = it }, label = { Text("贷款总额 (元)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = monthlyStr, onValueChange = { monthlyStr = it }, label = { Text("月供 (元)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = repayDayStr, onValueChange = { repayDayStr = it }, label = { Text("还款日") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(
                        name.trim(), selectedType, selectedSubType,
                        AmountFormatter.toLong(balanceStr.ifBlank { "0" }),
                        if (selectedType == AccountType.CREDIT) AmountFormatter.toLong(limitStr.ifBlank { "0" }) else null,
                        if (selectedType == AccountType.LOAN) AmountFormatter.toLong(loanStr.ifBlank { "0" }) else null,
                        if (selectedType == AccountType.LOAN) AmountFormatter.toLong(monthlyStr.ifBlank { "0" }) else null,
                        billDayStr.toIntOrNull(),
                        repayDayStr.toIntOrNull()
                    )
                }
            }) { Text("确定") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } }
    )
}

private fun AccountType.displayName() = when (this) {
    AccountType.FUND -> "资金"
    AccountType.CREDIT -> "信用"
    AccountType.INVESTMENT -> "投资"
    AccountType.LOAN -> "贷款"
}

private fun AccountType.defaultSubTypes() = when (this) {
    AccountType.FUND -> listOf(AccountSubType.BANK_CARD, AccountSubType.WECHAT, AccountSubType.ALIPAY)
    AccountType.CREDIT -> listOf(AccountSubType.CREDIT_CARD, AccountSubType.HUABEI, AccountSubType.BAITIAO)
    AccountType.INVESTMENT -> listOf(AccountSubType.PENSION, AccountSubType.FIXED_DEPOSIT, AccountSubType.STOCK, AccountSubType.FUTURES)
    AccountType.LOAN -> listOf(AccountSubType.MORTGAGE, AccountSubType.CAR_LOAN, AccountSubType.CONSUMER_LOAN)
}

private fun AccountSubType.displayName() = when (this) {
    AccountSubType.BANK_CARD -> "银行卡"
    AccountSubType.WECHAT -> "微信"
    AccountSubType.ALIPAY -> "支付宝"
    AccountSubType.CREDIT_CARD -> "信用卡"
    AccountSubType.HUABEI -> "花呗"
    AccountSubType.BAITIAO -> "白条"
    AccountSubType.PENSION -> "养老金"
    AccountSubType.FIXED_DEPOSIT -> "定期存款"
    AccountSubType.STOCK -> "股票"
    AccountSubType.FUTURES -> "期货"
    AccountSubType.MORTGAGE -> "房贷"
    AccountSubType.CAR_LOAN -> "车贷"
    AccountSubType.CONSUMER_LOAN -> "消费贷"
}
