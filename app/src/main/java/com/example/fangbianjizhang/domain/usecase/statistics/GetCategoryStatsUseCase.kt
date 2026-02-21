package com.example.fangbianjizhang.domain.usecase.statistics

import com.example.fangbianjizhang.data.local.db.dao.CategoryTotal
import com.example.fangbianjizhang.data.local.db.dao.TransactionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

data class CategoryAmount(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val amount: Long,
    val percentage: Float
)

class GetCategoryStatsUseCase @Inject constructor(
    private val transactionDao: TransactionDao
) {
    operator fun invoke(start: Long, end: Long, type: String): Flow<List<CategoryTotal>> =
        transactionDao.getCategoryStats(start, end, type)
}
