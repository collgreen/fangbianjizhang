package com.example.fangbianjizhang.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fangbianjizhang.data.local.db.entity.AccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE is_deleted = 0 ORDER BY sort_order")
    fun getAllActive(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE type = :type AND is_deleted = 0 ORDER BY sort_order")
    fun getByType(type: String): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    fun getById(id: Long): Flow<AccountEntity?>

    @Insert
    suspend fun insert(entity: AccountEntity): Long

    @Update
    suspend fun update(entity: AccountEntity)

    @Query("UPDATE accounts SET is_deleted = 1, deleted_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)

    @Query("UPDATE accounts SET balance = balance + :delta, updated_at = :now WHERE id = :id")
    suspend fun updateBalance(id: Long, delta: Long, now: Long)

    @Query("UPDATE accounts SET used_amount = used_amount + :delta, updated_at = :now WHERE id = :id")
    suspend fun updateUsedAmount(id: Long, delta: Long, now: Long)

    @Query("UPDATE accounts SET already_paid = already_paid + :delta, updated_at = :now WHERE id = :id")
    suspend fun updateAlreadyPaid(id: Long, delta: Long, now: Long)

    @Query("SELECT * FROM accounts WHERE is_deleted = 0")
    suspend fun getAllActiveList(): List<AccountEntity>

    @Query("SELECT * FROM accounts WHERE name = :name AND is_deleted = 0 LIMIT 1")
    suspend fun getByName(name: String): AccountEntity?

    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE type = 'FUND' AND is_deleted = 0 AND include_in_total = 1")
    fun getFundTotal(): Flow<Long>

    @Query("SELECT COALESCE(SUM(balance), 0) FROM accounts WHERE type = 'INVESTMENT' AND is_deleted = 0 AND include_in_total = 1")
    fun getInvestmentTotal(): Flow<Long>

    @Query("SELECT COALESCE(SUM(used_amount), 0) FROM accounts WHERE type = 'CREDIT' AND is_deleted = 0")
    fun getCreditUsedTotal(): Flow<Long>

    @Query("SELECT COALESCE(SUM(total_loan - already_paid), 0) FROM accounts WHERE type = 'LOAN' AND is_deleted = 0")
    fun getLoanRemainingTotal(): Flow<Long>
}
