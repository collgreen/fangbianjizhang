package com.example.fangbianjizhang.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.domain.model.BudgetMode
import com.example.fangbianjizhang.domain.repository.BudgetRepository
import com.example.fangbianjizhang.domain.repository.DailySummary
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import com.example.fangbianjizhang.data.local.datastore.PreferencesManager
import com.example.fangbianjizhang.data.local.db.dao.TransactionDao
import com.example.fangbianjizhang.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class BudgetStatus(
    val mode: BudgetMode = BudgetMode.NONE,
    val budget: Long = 0,
    val spent: Long = 0
)

data class HomeUiState(
    val budgetStatus: BudgetStatus = BudgetStatus(),
    val dailySummaries: List<DailySummary> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val transactionDao: TransactionDao,
    private val prefs: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        transactionFlow(),
        budgetFlow()
    ) { summaries, budget ->
        HomeUiState(
            budgetStatus = budget,
            dailySummaries = summaries,
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private fun transactionFlow(): Flow<List<DailySummary>> {
        val now = LocalDate.now()
        val (start, end) = DateUtils.monthRange(now.year, now.monthValue)
        return transactionRepo.getDailySummary(start, end)
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
