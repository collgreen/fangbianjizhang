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

    /** 查找当月或最近一个月的总预算（用于展示，跨月继承） */
    @Query("SELECT * FROM budgets WHERE category_id IS NULL AND year_month <= :yearMonth ORDER BY year_month DESC LIMIT 1")
    fun getEffectiveTotalBudget(yearMonth: String): Flow<BudgetEntity?>

    /** 查找当月或最近一个月的分类预算总和（用于展示，跨月继承） */
    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM budgets
        WHERE category_id IS NOT NULL
        AND year_month = (
            SELECT DISTINCT year_month FROM budgets
            WHERE category_id IS NOT NULL AND year_month <= :yearMonth
            ORDER BY year_month DESC LIMIT 1
        )
    """)
    fun getEffectiveCategoryBudgetSum(yearMonth: String): Flow<Long>

    /** 查找当月或最近一个月的所有预算记录（用于设置页加载，跨月继承） */
    @Query("""
        SELECT * FROM budgets
        WHERE year_month = (
            SELECT DISTINCT year_month FROM budgets
            WHERE year_month <= :yearMonth
            ORDER BY year_month DESC LIMIT 1
        )
    """)
    fun getEffectiveByYearMonth(yearMonth: String): Flow<List<BudgetEntity>>
}
