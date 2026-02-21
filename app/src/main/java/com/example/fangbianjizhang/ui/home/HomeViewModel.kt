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
    val isCredit: Boolean
)

data class HomeUiState(
    val budgetStatus: BudgetStatus = BudgetStatus(),
    val dailySummaries: List<DailySummary> = emptyList(),
    val repaymentReminders: List<RepaymentReminder> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val accountRepo: AccountRepository,
    private val transactionDao: TransactionDao,
    private val prefs: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        transactionFlow(),
        budgetFlow(),
        repaymentFlow(),
        accountRepo.getAllActive()
    ) { summaries, budget, reminders, accounts ->
        HomeUiState(
            budgetStatus = budget,
            dailySummaries = summaries,
            repaymentReminders = reminders,
            accounts = accounts,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun updateTransaction(tx: Transaction) {
        viewModelScope.launch { transactionRepo.update(tx) }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch { transactionRepo.softDelete(id) }
    }

    private fun transactionFlow(): Flow<List<DailySummary>> {
        val now = LocalDate.now()
        val (start, end) = DateUtils.monthRange(now.year, now.monthValue)
        return transactionRepo.getDailySummary(start, end)
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
                RepaymentReminder(acc.name, repayDay, daysLeft, acc.type == AccountType.CREDIT)
            }.sortedBy { it.daysLeft }
        }
    }

    private fun budgetFlow(): Flow<BudgetStatus> {
        val ym = DateUtils.currentYearMonth()
        val now = LocalDate.now()
        val (start, end) = DateUtils.monthRange(now.year, now.monthValue)
        return combine(
            prefs.budgetMode,
            budgetRepo.getTotalBudget(ym),
            transactionDao.getTotalByType(start, end, "EXPENSE")
        ) { mode, budget, spent ->
            when (mode) {
                BudgetMode.NONE -> BudgetStatus()
                BudgetMode.TOTAL -> BudgetStatus(mode, budget?.amount ?: 0, spent)
                BudgetMode.PER_CATEGORY -> BudgetStatus(mode, budget?.amount ?: 0, spent)
            }
        }
    }
}
