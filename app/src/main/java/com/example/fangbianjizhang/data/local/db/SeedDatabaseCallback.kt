package com.example.fangbianjizhang.data.local.db

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class SeedDatabaseCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        val now = System.currentTimeMillis()
        seedExpenseCategories(db, now)
        seedIncomeCategories(db, now)
    }

    private fun seedExpenseCategories(db: SupportSQLiteDatabase, now: Long) {
        val expense = listOf(
            Triple("餐饮", "ic_food", listOf("早餐", "午餐", "晚餐", "零食", "饮料")),
            Triple("生活", "ic_life", listOf("日用品", "水电燃气", "房租", "物业")),
            Triple("娱乐", "ic_entertainment", listOf("电影", "游戏", "旅行")),
            Triple("交通", "ic_transport", listOf("公交地铁", "打车", "加油")),
            Triple("教育", "ic_education", listOf("书籍", "课程", "培训"))
        )
        expense.forEachIndexed { i, (name, icon, children) ->
            db.execSQL(
                "INSERT INTO categories (name, type, parent_id, icon, sort_order, is_default, is_deleted, created_at) VALUES (?, 'EXPENSE', NULL, ?, ?, 1, 0, ?)",
                arrayOf(name, icon, i, now)
            )
            val parentId = queryLastInsertId(db)
            children.forEachIndexed { j, child ->
                db.execSQL(
                    "INSERT INTO categories (name, type, parent_id, icon, sort_order, is_default, is_deleted, created_at) VALUES (?, 'EXPENSE', ?, ?, ?, 1, 0, ?)",
                    arrayOf(child, parentId, icon, j, now)
                )
            }
        }
    }

    private fun seedIncomeCategories(db: SupportSQLiteDatabase, now: Long) {
        val income = listOf(
            Triple("工资", "ic_salary", listOf("基本工资", "奖金", "补贴")),
            Triple("兼职", "ic_parttime", emptyList()),
            Triple("理财收益", "ic_invest", listOf("利息", "分红")),
            Triple("其他", "ic_other", emptyList())
        )
        income.forEachIndexed { i, (name, icon, children) ->
            db.execSQL(
                "INSERT INTO categories (name, type, parent_id, icon, sort_order, is_default, is_deleted, created_at) VALUES (?, 'INCOME', NULL, ?, ?, 1, 0, ?)",
                arrayOf(name, icon, i, now)
            )
            val parentId = queryLastInsertId(db)
            children.forEachIndexed { j, child ->
                db.execSQL(
                    "INSERT INTO categories (name, type, parent_id, icon, sort_order, is_default, is_deleted, created_at) VALUES (?, 'INCOME', ?, ?, ?, 1, 0, ?)",
                    arrayOf(child, parentId, icon, j, now)
                )
            }
        }
    }

    private fun queryLastInsertId(db: SupportSQLiteDatabase): Long {
        val cursor = db.query("SELECT last_insert_rowid()")
        cursor.use {
            it.moveToFirst()
            return it.getLong(0)
        }
    }
}
