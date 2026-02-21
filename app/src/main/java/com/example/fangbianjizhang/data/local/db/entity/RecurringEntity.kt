package com.example.fangbianjizhang.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recurring_templates",
    indices = [Index("is_enabled", "is_deleted", "next_due_date")]
)
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Long,
    val frequency: String,
    @ColumnInfo(name = "day_of_month") val dayOfMonth: Int? = null,
    @ColumnInfo(name = "day_of_week") val dayOfWeek: Int? = null,
    @ColumnInfo(name = "interval_days") val intervalDays: Int? = null,
    @ColumnInfo(name = "source_account_id") val sourceAccountId: Long,
    @ColumnInfo(name = "target_account_id") val targetAccountId: Long,
    @ColumnInfo(name = "next_due_date") val nextDueDate: Long,
    @ColumnInfo(name = "last_executed_at") val lastExecutedAt: Long? = null,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = true,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
