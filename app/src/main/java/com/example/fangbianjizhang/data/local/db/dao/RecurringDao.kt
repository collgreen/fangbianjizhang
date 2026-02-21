package com.example.fangbianjizhang.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.fangbianjizhang.data.local.db.entity.RecurringEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_templates WHERE is_deleted = 0 ORDER BY next_due_date")
    fun getAll(): Flow<List<RecurringEntity>>

    @Query("SELECT * FROM recurring_templates WHERE is_enabled = 1 AND is_deleted = 0 AND next_due_date <= :todayTimestamp")
    fun getDueTemplates(todayTimestamp: Long): Flow<List<RecurringEntity>>

    @Query("SELECT * FROM recurring_templates WHERE is_deleted = 0")
    suspend fun getAllActiveList(): List<RecurringEntity>

    @Insert
    suspend fun insert(entity: RecurringEntity): Long

    @Update
    suspend fun update(entity: RecurringEntity)

    @Query("UPDATE recurring_templates SET is_enabled = :enabled, updated_at = :now WHERE id = :id")
    suspend fun setEnabled(id: Long, enabled: Boolean, now: Long)

    @Query("UPDATE recurring_templates SET next_due_date = :nextDate, last_executed_at = :now, updated_at = :now WHERE id = :id")
    suspend fun updateNextDueDate(id: Long, nextDate: Long, now: Long)

    @Query("UPDATE recurring_templates SET is_deleted = 1, updated_at = :now WHERE id = :id")
    suspend fun softDelete(id: Long, now: Long)
}
