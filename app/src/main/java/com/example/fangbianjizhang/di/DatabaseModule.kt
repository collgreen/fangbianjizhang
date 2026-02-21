package com.example.fangbianjizhang.di

import android.content.Context
import androidx.room.Room
import com.example.fangbianjizhang.data.local.db.AppDatabase
import com.example.fangbianjizhang.data.local.db.SeedDatabaseCallback
import com.example.fangbianjizhang.data.local.db.dao.AccountDao
import com.example.fangbianjizhang.data.local.db.dao.BudgetDao
import com.example.fangbianjizhang.data.local.db.dao.CategoryDao
import com.example.fangbianjizhang.data.local.db.dao.RecurringDao
import com.example.fangbianjizhang.data.local.db.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DB_NAME)
            .addCallback(SeedDatabaseCallback())
            .build()

    @Provides fun provideAccountDao(db: AppDatabase): AccountDao = db.accountDao()
    @Provides fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
    @Provides fun provideBudgetDao(db: AppDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideRecurringDao(db: AppDatabase): RecurringDao = db.recurringDao()
}
