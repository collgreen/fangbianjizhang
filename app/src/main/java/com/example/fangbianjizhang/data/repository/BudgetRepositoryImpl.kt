package com.example.fangbianjizhang.data.repository

import com.example.fangbianjizhang.data.local.db.dao.BudgetDao
import com.example.fangbianjizhang.data.local.db.entity.BudgetEntity
import com.example.fangbianjizhang.domain.model.Budget
import com.example.fangbianjizhang.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun getByYearMonth(yearMonth: String): Flow<List<Budget>> =
        dao.getByYearMonth(yearMonth).map { list ->
            list.map { Budget(it.id, it.categoryId, it.amount, it.yearMonth) }
        }

    override fun getTotalBudget(yearMonth: String): Flow<Budget?> =
        dao.getTotalBudget(yearMonth).map { it?.let { e -> Budget(e.id, e.categoryId, e.amount, e.yearMonth) } }

    override suspend fun setTotalBudget(yearMonth: String, amount: Long) {
        val now = System.currentTimeMillis()
        val existing = dao.getTotalBudget(yearMonth).first()
        if (existing == null) {
            dao.insert(BudgetEntity(categoryId = null, amount = amount, yearMonth = yearMonth, createdAt = now, updatedAt = now))
        } else {
            dao.updateTotalBudget(yearMonth, amount, now)
        }
    }

    override suspend fun setCategoryBudget(yearMonth: String, categoryId: Long, amount: Long) {
        val now = System.currentTimeMillis()
        val updated = dao.updateCategoryBudget(yearMonth, categoryId, amount, now)
        if (updated == 0) {
            dao.insert(BudgetEntity(categoryId = categoryId, amount = amount, yearMonth = yearMonth, createdAt = now, updatedAt = now))
        }
    }

    override suspend fun clearByYearMonth(yearMonth: String) =
        dao.clearByYearMonth(yearMonth)
}
