package com.example.fangbianjizhang.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    indices = [
        Index("is_deleted", "transaction_date"),
        Index("is_deleted", "type", "transaction_date"),
        Index("account_id"),
        Index("category_id")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "target_account_id") val targetAccountId: Long? = null,
    val fee: Long = 0,
    val counterparty: String? = null,
    @ColumnInfo(name = "due_date") val dueDate: Long? = null,
    val note: String? = null,
    @ColumnInfo(name = "image_uri") val imageUri: String? = null,
    @ColumnInfo(name = "transaction_date") val transactionDate: Long,
    @ColumnInfo(name = "recurring_id") val recurringId: Long? = null,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
