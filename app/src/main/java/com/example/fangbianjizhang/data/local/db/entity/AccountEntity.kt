package com.example.fangbianjizhang.data.local.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [
        Index("type"),
        Index("is_deleted")
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String,
    @ColumnInfo(name = "sub_type") val subType: String,
    val balance: Long = 0,
    @ColumnInfo(name = "total_limit") val totalLimit: Long? = null,
    @ColumnInfo(name = "used_amount") val usedAmount: Long? = null,
    @ColumnInfo(name = "installment_amount") val installmentAmount: Long? = null,
    @ColumnInfo(name = "total_loan") val totalLoan: Long? = null,
    @ColumnInfo(name = "already_paid") val alreadyPaid: Long? = null,
    @ColumnInfo(name = "monthly_payment") val monthlyPayment: Long? = null,
    @ColumnInfo(name = "bill_day") val billDay: Int? = null,
    @ColumnInfo(name = "repayment_day") val repaymentDay: Int? = null,
    val icon: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
    @ColumnInfo(name = "include_in_total") val includeInTotal: Boolean = true,
    @ColumnInfo(name = "is_deleted") val isDeleted: Boolean = false,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
