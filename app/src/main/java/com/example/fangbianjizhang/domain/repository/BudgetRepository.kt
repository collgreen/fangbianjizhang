package com.example.fangbianjizhang.domain.repository

import com.example.fangbianjizhang.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getByYearMonth(yearMonth: String): Flow<List<Budget>>
    fun getTotalBudget(yearMonth: String): Flow<Budget?>
    suspend fun setTotalBudget(yearMonth: String, amount: Long)
    suspend fun setCategoryBudget(yearMonth: String, categoryId: Long, amount: Long)
    suspend fun clearByYearMonth(yearMonth: String)
}
