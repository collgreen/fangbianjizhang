package com.example.fangbianjizhang.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.data.local.db.dao.CategoryTotal
import com.example.fangbianjizhang.data.local.db.dao.TransactionDao
import com.example.fangbianjizhang.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import javax.inject.Inject

data class StatisticsUiState(
    val year: Int = LocalDate.now().year,
    val month: Int = LocalDate.now().monthValue,
    val totalIncome: Long = 0,
    val totalExpense: Long = 0,
    val categoryStats: List<CategoryTotal> = emptyList(),
    val showExpense: Boolean = true,
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionDao: TransactionDao
) : ViewModel() {

    private val _year = MutableStateFlow(LocalDate.now().year)
    private val _month = MutableStateFlow(LocalDate.now().monthValue)
    private val _showExpense = MutableStateFlow(true)
    private val _expandedCategories = MutableStateFlow<Set<Long>>(emptySet())

    val expandedCategories: StateFlow<Set<Long>> = _expandedCategories.asStateFlow()

    val uiState: StateFlow<StatisticsUiState> = combine(
        _year, _month, _showExpense
    ) { y, m, exp -> Triple(y, m, exp) }
        .flatMapLatest { (y, m, exp) ->
            val (start, end) = DateUtils.monthRange(y, m)
            val type = if (exp) "EXPENSE" else "INCOME"
            combine(
                transactionDao.getTotalByType(start, end, "INCOME"),
                transactionDao.getTotalByType(start, end, "EXPENSE"),
                transactionDao.getCategoryStats(start, end, type)
            ) { income, expense, stats ->
                StatisticsUiState(y, m, income, expense, stats, exp, false)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatisticsUiState())

    fun prevMonth() {
        if (_month.value == 1) { _year.value--; _month.value = 12 }
        else _month.value--
    }

    fun nextMonth() {
        if (_month.value == 12) { _year.value++; _month.value = 1 }
        else _month.value++
    }

    fun toggleType() {
        _showExpense.value = !_showExpense.value
        _expandedCategories.value = emptySet()
    }

    fun toggleCategoryExpand(categoryId: Long) {
        _expandedCategories.value = _expandedCategories.value.let { current ->
            if (current.contains(categoryId)) current - categoryId else current + categoryId
        }
    }

    fun getSubCategoryStats(parentId: Long): Flow<List<CategoryTotal>> {
        val y = _year.value
        val m = _month.value
        val (start, end) = DateUtils.monthRange(y, m)
        val type = if (_showExpense.value) "EXPENSE" else "INCOME"
        return transactionDao.getSubCategoryStats(start, end, type, parentId)
    }
}
