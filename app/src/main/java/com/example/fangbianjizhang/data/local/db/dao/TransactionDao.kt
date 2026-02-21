package com.example.fangbianjizhang.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fangbianjizhang.data.local.db.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

data class TransactionWithCategory(
    val id: Long,
    val type: String,
    val amount: Long,
    val category_id: Long?,
    val account_id: Long,
    val target_account_id: Long?,
    val fee: Long,
    val counterparty: String?,
    val due_date: Long?,
    val note: String?,
    val image_uri: String?,
    val transaction_date: Long,
    val recurring_id: Long?,
    val category_name: String?,
    val category_icon: String?,
    val parent_category_name: String?
)

data class CategoryTotal(
    val category_id: Long,
    val category_name: String,
    val category_icon: String,
    val total: Long
)

@Dao
interface TransactionDao {
    @Query("""
        SELECT t.id, t.type, t.amount, t.category_id, t.account_id,
               t.target_account_id, t.fee, t.counterparty, t.due_date,
               t.note, t.image_uri, t.transaction_date, t.recurring_id,
               c.name AS category_name, c.icon AS category_icon,
               pc.name AS parent_category_name
        FROM transactions t
        LEFT JOIN categories c ON t.category_id = c.id
        LEFT JOIN categories pc ON c.parent_id = pc.id
        WHERE t.is_deleted = 0
          AND t.transaction_date BETWEEN :start AND :end
        ORDER BY t.transaction_date DESC
    """)
    fun getByDateRange(start: Long, end: Long): Flow<List<TransactionWithCategory>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun getById(id: Long): Flow<TransactionEntity?>

    @Insert
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query("UPDATE transactions SET is_deleted = 1, deleted_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)

    @Query("""
        SELECT pc.id AS category_id, pc.name AS category_name,
               pc.icon AS category_icon, SUM(t.amount) AS total
        FROM transactions t
        JOIN categories c ON t.category_id = c.id
        JOIN categories pc ON COALESCE(c.parent_id, c.id) = pc.id
        WHERE t.is_deleted = 0
          AND t.type = :type
          AND t.transaction_date BETWEEN :start AND :end
          AND pc.parent_id IS NULL
        GROUP BY pc.id
        ORDER BY total DESC
    """)
    fun getCategoryStats(start: Long, end: Long, type: String): Flow<List<CategoryTotal>>

    @Query("SELECT * FROM transactions WHERE is_deleted = 0 ORDER BY transaction_date DESC")
    suspend fun getAllActiveList(): List<TransactionEntity>

    @Query("""
        SELECT * FROM transactions WHERE is_deleted = 0
          AND transaction_date BETWEEN :start AND :end
        ORDER BY transaction_date DESC
    """)
    suspend fun getByDateRangeList(start: Long, end: Long): List<TransactionEntity>

    @Query("""
        SELECT COALESCE(SUM(amount), 0) FROM transactions
        WHERE is_deleted = 0 AND type = :type
          AND transaction_date BETWEEN :start AND :end
    """)
    fun getTotalByType(start: Long, end: Long, type: String): Flow<Long>
}
