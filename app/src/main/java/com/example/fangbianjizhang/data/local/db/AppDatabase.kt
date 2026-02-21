package com.example.fangbianjizhang.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fangbianjizhang.data.local.db.dao.AccountDao
import com.example.fangbianjizhang.data.local.db.dao.BudgetDao
import com.example.fangbianjizhang.data.local.db.dao.CategoryDao
import com.example.fangbianjizhang.data.local.db.dao.RecurringDao
import com.example.fangbianjizhang.data.local.db.dao.TransactionDao
import com.example.fangbianjizhang.data.local.db.entity.AccountEntity
import com.example.fangbianjizhang.data.local.db.entity.BudgetEntity
import com.example.fangbianjizhang.data.local.db.entity.CategoryEntity
import com.example.fangbianjizhang.data.local.db.entity.RecurringEntity
import com.example.fangbianjizhang.data.local.db.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        TransactionEntity::class,
        CategoryEntity::class,
        BudgetEntity::class,
        RecurringEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun budgetDao(): BudgetDao
    abstract fun recurringDao(): RecurringDao

    companion object {
        const val DB_NAME = "fangbianjizhang.db"
    }
}
