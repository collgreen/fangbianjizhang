package com.example.fangbianjizhang.domain.repository

import com.example.fangbianjizhang.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

data class DailySummary(
    val dayTimestamp: Long,
    val totalExpense: Long,
    val totalIncome: Long,
    val transactions: List<Transaction>
)

interface TransactionRepository {
    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>>
    fun getDailySummary(start: Long, end: Long): Flow<List<DailySummary>>
    fun getById(id: Long): Flow<Transaction?>
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun softDelete(id: Long)
}
