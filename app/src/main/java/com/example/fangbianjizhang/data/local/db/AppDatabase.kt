package com.example.fangbianjizhang.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
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
    version = 3,
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

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE accounts ADD COLUMN installment_amount INTEGER DEFAULT NULL")
            }
        }

        /**
         * 修复旧版 Bug 导致的账户余额错误。
         *
         * 旧代码问题：
         * 1. INSERT/UPDATE TRANSFER 对目标账户不区分类型，全部用 updateBalance
         *    - 信用账户：错误地增加 balance，未减少 used_amount
         *    - 贷款账户：错误地增加 balance，未增加 already_paid
         * 2. DELETE 不区分账户类型，全部用 updateBalance
         *    - 信用账户的消费/收入被删除时，未正确回退 used_amount
         *    - 贷款账户的收入被删除时，未正确回退 already_paid
         *
         * 修正策略：针对每种 bug 场景逐一修正 balance/used_amount/already_paid
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ========== 修复信用账户 ==========
                // 1) balance 被错误增加的：还原活跃的转账还款
                db.execSQL("""
                    UPDATE accounts SET balance = balance - COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.target_account_id = accounts.id
                        AND t.type = 'TRANSFER' AND t.is_deleted = 0
                    ), 0) WHERE type = 'CREDIT' AND is_deleted = 0
                """)
                // 2) balance 被错误修改的：还原已删除消费(+amount)和已删除收入(-amount)
                db.execSQL("""
                    UPDATE accounts SET balance = balance
                    - COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.account_id = accounts.id
                        AND t.type = 'EXPENSE' AND t.is_deleted = 1
                    ), 0)
                    + COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.account_id = accounts.id
                        AND t.type = 'INCOME' AND t.is_deleted = 1
                    ), 0)
                    WHERE type = 'CREDIT' AND is_deleted = 0
                """)
                // 3) used_amount 未正确修改的：补偿活跃转账还款 + 已删除消费/收入
                db.execSQL("""
                    UPDATE accounts SET used_amount = COALESCE(used_amount, 0)
                    - COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.target_account_id = accounts.id
                        AND t.type = 'TRANSFER' AND t.is_deleted = 0
                    ), 0)
                    - COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.account_id = accounts.id
                        AND t.type = 'EXPENSE' AND t.is_deleted = 1
                    ), 0)
                    + COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.account_id = accounts.id
                        AND t.type = 'INCOME' AND t.is_deleted = 1
                    ), 0)
                    WHERE type = 'CREDIT' AND is_deleted = 0
                """)

                // ========== 修复贷款账户 ==========
                // 4) balance 被错误增加的：还原活跃的转账还款
                db.execSQL("""
                    UPDATE accounts SET balance = balance - COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.target_account_id = accounts.id
                        AND t.type = 'TRANSFER' AND t.is_deleted = 0
                    ), 0) WHERE type = 'LOAN' AND is_deleted = 0
                """)
                // 5) balance 被错误修改的：还原已删除收入
                db.execSQL("""
                    UPDATE accounts SET balance = balance + COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.account_id = accounts.id
                        AND t.type = 'INCOME' AND t.is_deleted = 1
                    ), 0) WHERE type = 'LOAN' AND is_deleted = 0
                """)
                // 6) already_paid 未正确修改的：补偿活跃转账还款 + 已删除收入
                db.execSQL("""
                    UPDATE accounts SET already_paid = COALESCE(already_paid, 0)
                    + COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.target_account_id = accounts.id
                        AND t.type = 'TRANSFER' AND t.is_deleted = 0
                    ), 0)
                    - COALESCE((
                        SELECT SUM(t.amount) FROM transactions t
                        WHERE t.account_id = accounts.id
                        AND t.type = 'INCOME' AND t.is_deleted = 1
                    ), 0)
                    WHERE type = 'LOAN' AND is_deleted = 0
                """)
            }
        }
    }
}
