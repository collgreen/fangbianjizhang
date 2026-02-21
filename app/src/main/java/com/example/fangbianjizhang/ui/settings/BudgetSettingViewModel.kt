package com.example.fangbianjizhang.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fangbianjizhang.data.local.datastore.PreferencesManager
import com.example.fangbianjizhang.domain.model.Budget
import com.example.fangbianjizhang.domain.model.BudgetMode
import com.example.fangbianjizhang.domain.model.Category
import com.example.fangbianjizhang.domain.model.CategoryType
import com.example.fangbianjizhang.domain.repository.BudgetRepository
import com.example.fangbianjizhang.domain.repository.CategoryRepository
import com.example.fangbianjizhang.util.AmountFormatter
import com.example.fangbianjizhang.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BudgetSettingUiState(
    val mode: BudgetMode = BudgetMode.NONE,
    val totalBudget: String = "",
    val categories: List<Category> = emptyList(),
    val categoryBudgets: Map<Long, String> = emptyMap(),
    val saved: Boolean = false
)

@HiltViewModel
class BudgetSettingViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository,
    private val categoryRepo: CategoryRepository,
    private val prefs: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(BudgetSettingUiState())
    val uiState: StateFlow<BudgetSettingUiState> = _state.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            val mode = prefs.budgetMode.first()
            _state.value = _state.value.copy(mode = mode)

            val ym = DateUtils.currentYearMonth()
            val budgets = budgetRepo.getByYearMonth(ym).first()

            if (mode == BudgetMode.TOTAL) {
                val total = budgets.firstOrNull { it.categoryId == null }
                _state.value = _state.value.copy(
                    totalBudget = total?.let { AmountFormatter.toDisplay(it.amount) } ?: ""
                )
            }

            val cats = categoryRepo.getTopLevelByType(CategoryType.EXPENSE).first()
            val catMap = budgets.filter { it.categoryId != null }
                .associate { it.categoryId!! to AmountFormatter.toDisplay(it.amount) }
            _state.value = _state.value.copy(categories = cats, categoryBudgets = catMap)
        }
    }

    fun setMode(mode: BudgetMode) {
        _state.value = _state.value.copy(mode = mode)
    }

    fun setTotalAmount(amount: String) {
        _state.value = _state.value.copy(totalBudget = amount)
    }

    fun setCategoryAmount(categoryId: Long, amount: String) {
        _state.value = _state.value.copy(
            categoryBudgets = _state.value.categoryBudgets + (categoryId to amount)
        )
    }

    fun save() {
        val s = _state.value
        viewModelScope.launch {
            val ym = DateUtils.currentYearMonth()
            prefs.setBudgetMode(s.mode)
            budgetRepo.clearByYearMonth(ym)

            when (s.mode) {
                BudgetMode.TOTAL -> {
                    if (s.totalBudget.isNotBlank()) {
                        budgetRepo.setTotalBudget(ym, AmountFormatter.toLong(s.totalBudget))
                    }
                }
                BudgetMode.PER_CATEGORY -> {
                    s.categoryBudgets.forEach { (catId, amt) ->
                        if (amt.isNotBlank()) {
                            budgetRepo.setCategoryBudget(ym, catId, AmountFormatter.toLong(amt))
                        }
                    }
                }
                BudgetMode.NONE -> {}
            }
            _state.value = s.copy(saved = true)
        }
    }
}
