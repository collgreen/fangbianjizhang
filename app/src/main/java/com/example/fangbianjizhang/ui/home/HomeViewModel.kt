package com.example.fangbianjizhang.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.Account
import com.example.fangbianjizhang.domain.model.AccountType
import com.example.fangbianjizhang.domain.model.BudgetMode
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.repository.AccountRepository
import com.example.fangbianjizhang.domain.repository.BudgetRepository
import com.example.fangbianjizhang.domain.repository.DailySummary
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import com.example.fangbianjizhang.data.local.datastore.PreferencesManager
import com.example.fangbianjizhang.data.local.db.dao.TransactionDao
import com.example.fangbianjizhang.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BudgetStatus(
    val mode: BudgetMode = BudgetMode.NONE,
    val budget: Long = 0,
    val spent: Long = 0
)

data class RepaymentReminder(
    val accountName: String,
    val repaymentDay: Int,
    val daysLeft: Int,
    val isCredit: Boolean,
    val amount: Long = 0
)

data class HomeUiState(
    val year: Int = LocalDate.now().year,
    val month: Int = LocalDate.now().monthValue,
    val budgetStatus: BudgetStatus = BudgetStatus(),
    val dailySummaries: List<DailySummary> = emptyList(),
    val repaymentReminders: List<RepaymentReminder> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val monthlyExpense: Long = 0,
    val monthlyIncome: Long = 0,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val accountRepo: AccountRepository,
    private val transactionDao: TransactionDao,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _year = MutableStateFlow(LocalDate.now().year)
    private val _month = MutableStateFlow(LocalDate.now().monthValue)

    val uiState: StateFlow<HomeUiState> = combine(
        _year, _month
    ) { y, m -> y to m }
        .flatMapLatest { (y, m) ->
            val (start, end) = DateUtils.monthRange(y, m)
            val ym = String.format("%d-%02d", y, m)
            combine(
                transactionRepo.getDailySummary(start, end),
                budgetFlow(ym, start, end),
                repaymentFlow(),
                accountRepo.getAllActive(),
                combine(
                    transactionDao.getTotalByType(start, end, "EXPENSE"),
                    transactionDao.getTotalByType(start, end, "INCOME")
                ) { exp, inc -> exp to inc }
            ) { summaries, budget, reminders, accounts, totals ->
                HomeUiState(
                    year = y,
                    month = m,
                    budgetStatus = budget,
                    dailySummaries = summaries,
                    repaymentReminders = reminders,
                    accounts = accounts,
                    monthlyExpense = totals.first,
                    monthlyIncome = totals.second,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun prevMonth() {
        if (_month.value == 1) { _year.value--; _month.value = 12 }
        else _month.value--
    }

    fun nextMonth() {
        val now = LocalDate.now()
        if (_year.value > now.year || (_year.value == now.year && _month.value >= now.monthValue)) return
        if (_month.value == 12) { _year.value++; _month.value = 1 }
        else _month.value++
    }

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch { transactionRepo.update(tx) }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { transactionRepo.softDelete(id) }
    }

    private fun repaymentFlow(): Flow<List<RepaymentReminder>> {
        return accountRepo.getAllActive().map { accounts ->
            val today = LocalDate.now()
            accounts.filter {
                (it.type == AccountType.CREDIT || it.type == AccountType.LOAN) && it.repaymentDay != null
            }.map { acc ->
                val repayDay = acc.repaymentDay!!
                val nextRepay = today.withDayOfMonth(repayDay).let {
                    if (it.isBefore(today) || it.isEqual(today)) it.plusMonths(1) else it
                }
                val daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, nextRepay).toInt()
                val amount = if (acc.type == AccountType.CREDIT) acc.usedAmount ?: 0
                    else acc.monthlyPayment ?: 0
                RepaymentReminder(acc.name, repayDay, daysLeft, acc.type == AccountType.CREDIT, amount)
            }.sortedBy { it.daysLeft }
        }
    }

    private fun budgetFlow(ym: String, start: Long, end: Long): Flow<BudgetStatus> {
        return combine(
            prefs.budgetMode,
            budgetRepo.getEffectiveTotalBudget(ym),
            budgetRepo.getEffectiveCategoryBudgetSum(ym),
            transactionDao.getTotalByType(start, end, "EXPENSE")
        ) { mode, totalBudget, categorySum, spent ->
            when (mode) {
                BudgetMode.NONE -> BudgetStatus()
                BudgetMode.TOTAL -> BudgetStatus(mode, totalBudget?.amount ?: 0, spent)
                BudgetMode.PER_CATEGORY -> BudgetStatus(mode, categorySum, spent)
            }
        }
    }
}
