package com.example.fangbianjizhang.data.repository

import com.example.fangbianjizhang.data.local.db.dao.TransactionDao
import com.example.fangbianjizhang.data.mapper.toDomain
import com.example.fangbianjizhang.data.mapper.toEntity
import com.example.fangbianjizhang.domain.model.Transaction
import com.example.fangbianjizhang.domain.model.TransactionType
import com.example.fangbianjizhang.domain.repository.DailySummary
import com.example.fangbianjizhang.domain.repository.TransactionRepository
import com.example.fangbianjizhang.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        dao.getByDateRange(start, end).map { list ->
            list.map { twc ->
                Transaction(
                    id = twc.id,
                    type = TransactionType.valueOf(twc.type),
                    amount = twc.amount,
                    categoryId = twc.category_id,
                    accountId = twc.account_id,
                    targetAccountId = twc.target_account_id,
                    fee = twc.fee,
                    counterparty = twc.counterparty,
                    dueDate = twc.due_date,
                    note = twc.note,
                    imageUri = twc.image_uri,
                    transactionDate = twc.transaction_date,
                    recurringId = twc.recurring_id
                )
            }
        }

    override fun getDailySummary(start: Long, end: Long): Flow<List<DailySummary>> =
        getByDateRange(start, end).map { transactions ->
            transactions.groupBy { DateUtils.toLocalDate(it.transactionDate) }
                .map { (date, txns) ->
                    DailySummary(
                        dayTimestamp = txns.first().transactionDate,
                        totalExpense = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount },
                        totalIncome = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
                        transactions = txns
                    )
                }
                .sortedByDescending { it.dayTimestamp }
        }

    override fun getById(id: Long): Flow<Transaction?> =
        dao.getById(id).map { it?.toDomain() }

    override suspend fun insert(transaction: Transaction) {
        dao.insert(transaction.toEntity())
    }

    override suspend fun update(transaction: Transaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun softDelete(id: Long) =
        dao.softDelete(id, System.currentTimeMillis())
}
