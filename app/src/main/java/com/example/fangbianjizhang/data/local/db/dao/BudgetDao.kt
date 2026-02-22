package com.example.fangbianjizhang.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.fangbianjizhang.data.local.db.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE year_month = :yearMonth")
    fun getByYearMonth(yearMonth: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE year_month = :yearMonth AND category_id IS NULL LIMIT 1")
    fun getTotalBudget(yearMonth: String): Flow<BudgetEntity?>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM budgets WHERE year_month = :yearMonth AND category_id IS NOT NULL")
    fun getCategoryBudgetSum(yearMonth: String): Flow<Long>

    @Query("SELECT * FROM budgets")
    suspend fun getAllList(): List<BudgetEntity>

    @Insert
    suspend fun insert(entity: BudgetEntity): Long

    @Query("UPDATE budgets SET amount = :amount, updated_at = :now WHERE year_month = :yearMonth AND category_id IS NULL")
    suspend fun updateTotalBudget(yearMonth: String, amount: Long, now: Long)

    @Query("UPDATE budgets SET amount = :amount, updated_at = :now WHERE year_month = :yearMonth AND category_id = :categoryId")
    suspend fun updateCategoryBudget(yearMonth: String, categoryId: Long, amount: Long, now: Long): Int

    @Query("DELETE FROM budgets WHERE year_month = :yearMonth")
    suspend fun clearByYearMonth(yearMonth: String)
}
