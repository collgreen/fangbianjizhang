package com.example.fangbianjizhang.data.repository

import android.content.Context
import android.net.Uri
import com.example.fangbianjizhang.data.local.db.dao.*
import com.example.fangbianjizhang.data.local.db.entity.*
import com.example.fangbianjizhang.domain.repository.BackupRepository
import com.example.fangbianjizhang.domain.repository.ImportConflict
import com.example.fangbianjizhang.domain.repository.ImportSummary
import com.example.fangbianjizhang.util.AmountFormatter
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject

class BackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val accountDao: AccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val recurringDao: RecurringDao
) : BackupRepository {

    override suspend fun exportJson(outputUri: Uri, password: String?): Result<Unit> =
        runCatching {
            val json = buildExportJson()
            val bytes = if (password != null) encrypt(json, password) else json.toByteArray()
            context.contentResolver.openOutputStream(outputUri)?.use { it.write(bytes) }
                ?: error("无法打开输出文件")
        }

    override suspend fun checkImportConflicts(inputUri: Uri, password: String?): Result<ImportConflict> =
        runCatching {
            val root = readJson(inputUri, password)
            val names = mutableListOf<String>()
            val accounts = root.optJSONArray("accounts")
            if (accounts != null) for (i in 0 until accounts.length()) {
                val name = accounts.getJSONObject(i).getString("name")
                if (accountDao.getByName(name) != null) names.add(name)
            }
            ImportConflict(names)
        }

    override suspend fun importJson(inputUri: Uri, password: String?, overwriteAccounts: Boolean): Result<ImportSummary> =
        runCatching {
            val root = readJson(inputUri, password)
            parseAndImport(root, overwriteAccounts)
        }

    private fun readJson(uri: Uri, password: String?): JSONObject {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("无法打开输入文件")
        val json = if (password != null) decrypt(bytes, password) else String(bytes)
        return JSONObject(json)
    }

    override suspend fun exportCsv(outputUri: Uri, start: Long, end: Long): Result<Unit> =
        runCatching {
            val transactions = transactionDao.getByDateRangeList(start, end)
            val categories = categoryDao.getAllActiveList().associateBy { it.id }
            val accounts = accountDao.getAllActiveList().associateBy { it.id }
            context.contentResolver.openOutputStream(outputUri)?.use { os ->
                BufferedWriter(OutputStreamWriter(os, Charsets.UTF_8)).use { w ->
                    w.write("\uFEFF") // BOM for Excel
                    w.write("日期,类型,金额,分类,账户,备注,对方\n")
                    for (t in transactions) {
                        val cat = t.categoryId?.let { categories[it]?.name } ?: ""
                        val acc = accounts[t.accountId]?.name ?: ""
                        val amt = AmountFormatter.toDisplay(t.amount)
                        val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.CHINA)
                            .format(java.util.Date(t.transactionDate))
                        w.write("$date,${t.type},$amt,$cat,$acc,${t.note ?: ""},${t.counterparty ?: ""}\n")
                    }
                }
            } ?: error("无法打开输出文件")
        }

    private suspend fun buildExportJson(): String {
        val root = JSONObject()
        root.put("schema_version", 1)
        root.put("exported_at", System.currentTimeMillis())
        root.put("accounts", accountsToJson(accountDao.getAllActiveList()))
        root.put("categories", categoriesToJson(categoryDao.getAllActiveList()))
        root.put("transactions", transactionsToJson(transactionDao.getAllActiveList()))
        root.put("budgets", budgetsToJson(budgetDao.getAllList()))
        root.put("recurring_templates", recurringToJson(recurringDao.getAllActiveList()))
        return root.toString(2)
    }

    private fun accountsToJson(list: List<AccountEntity>): JSONArray {
        val arr = JSONArray()
        for (a in list) {
            arr.put(JSONObject().apply {
                put("id", a.id); put("name", a.name); put("type", a.type)
                put("sub_type", a.subType); put("balance", a.balance)
                put("total_limit", a.totalLimit ?: JSONObject.NULL)
                put("used_amount", a.usedAmount ?: JSONObject.NULL)
                put("total_loan", a.totalLoan ?: JSONObject.NULL)
                put("already_paid", a.alreadyPaid ?: JSONObject.NULL)
                put("monthly_payment", a.monthlyPayment ?: JSONObject.NULL)
                put("bill_day", a.billDay ?: JSONObject.NULL)
                put("repayment_day", a.repaymentDay ?: JSONObject.NULL)
                put("icon", a.icon); put("sort_order", a.sortOrder)
                put("include_in_total", a.includeInTotal)
                put("created_at", a.createdAt)
            })
        }
        return arr
    }

    private fun categoriesToJson(list: List<CategoryEntity>): JSONArray {
        val arr = JSONArray()
        for (c in list) {
            arr.put(JSONObject().apply {
                put("id", c.id); put("name", c.name); put("type", c.type)
                put("parent_id", c.parentId ?: JSONObject.NULL)
                put("icon", c.icon); put("sort_order", c.sortOrder)
                put("is_default", c.isDefault); put("created_at", c.createdAt)
            })
        }
        return arr
    }

    private fun transactionsToJson(list: List<TransactionEntity>): JSONArray {
        val arr = JSONArray()
        for (t in list) {
            arr.put(JSONObject().apply {
                put("id", t.id); put("type", t.type); put("amount", t.amount)
                put("category_id", t.categoryId ?: JSONObject.NULL)
                put("account_id", t.accountId)
                put("target_account_id", t.targetAccountId ?: JSONObject.NULL)
                put("fee", t.fee)
                put("counterparty", t.counterparty ?: JSONObject.NULL)
                put("due_date", t.dueDate ?: JSONObject.NULL)
                put("note", t.note ?: JSONObject.NULL)
                put("transaction_date", t.transactionDate)
                put("recurring_id", t.recurringId ?: JSONObject.NULL)
                put("created_at", t.createdAt)
            })
        }
        return arr
    }

    private fun budgetsToJson(list: List<BudgetEntity>): JSONArray {
        val arr = JSONArray()
        for (b in list) {
            arr.put(JSONObject().apply {
                put("id", b.id)
                put("category_id", b.categoryId ?: JSONObject.NULL)
                put("amount", b.amount); put("year_month", b.yearMonth)
                put("created_at", b.createdAt)
            })
        }
        return arr
    }

    private fun recurringToJson(list: List<RecurringEntity>): JSONArray {
        val arr = JSONArray()
        for (r in list) {
            arr.put(JSONObject().apply {
                put("id", r.id); put("name", r.name); put("amount", r.amount)
                put("frequency", r.frequency)
                put("day_of_month", r.dayOfMonth ?: JSONObject.NULL)
                put("day_of_week", r.dayOfWeek ?: JSONObject.NULL)
                put("interval_days", r.intervalDays ?: JSONObject.NULL)
                put("source_account_id", r.sourceAccountId)
                put("target_account_id", r.targetAccountId)
                put("next_due_date", r.nextDueDate)
                put("is_enabled", r.isEnabled); put("created_at", r.createdAt)
            })
        }
        return arr
    }

    private suspend fun parseAndImport(root: JSONObject, overwrite: Boolean): ImportSummary {
        val now = System.currentTimeMillis()
        var acctCount = 0; var catCount = 0; var txnCount = 0
        var budgetCount = 0; var recurringCount = 0

        // old ID → new ID mappings
        val acctIdMap = mutableMapOf<Long, Long>()
        val catIdMap = mutableMapOf<Long, Long>()

        val accounts = root.optJSONArray("accounts")
        if (accounts != null) for (i in 0 until accounts.length()) {
            val o = accounts.getJSONObject(i)
            val oldId = o.optLong("id", 0)
            val entity = parseAccountJson(o, now)
            val existing = accountDao.getByName(entity.name)
            val newId = if (existing != null && overwrite) {
                accountDao.update(entity.copy(id = existing.id)); existing.id
            } else if (existing != null) {
                existing.id
            } else {
                accountDao.insert(entity)
            }
            if (oldId != 0L) acctIdMap[oldId] = newId
            acctCount++
        }

        val categories = root.optJSONArray("categories")
        if (categories != null) for (i in 0 until categories.length()) {
            val o = categories.getJSONObject(i)
            val oldId = o.optLong("id", 0)
            val oldParentId = o.optLongNull("parent_id")
            val newId = categoryDao.insert(CategoryEntity(
                name = o.getString("name"), type = o.getString("type"),
                parentId = oldParentId?.let { catIdMap[it] ?: it },
                icon = o.getString("icon"), sortOrder = o.optInt("sort_order", 0),
                isDefault = o.optBoolean("is_default", false), createdAt = now
            ))
            if (oldId != 0L) catIdMap[oldId] = newId
            catCount++
        }

        val transactions = root.optJSONArray("transactions")
        if (transactions != null) for (i in 0 until transactions.length()) {
            val o = transactions.getJSONObject(i)
            val oldAcctId = o.getLong("account_id")
            val oldCatId = o.optLongNull("category_id")
            val oldTargetId = o.optLongNull("target_account_id")
            transactionDao.insert(TransactionEntity(
                type = o.getString("type"), amount = o.getLong("amount"),
                categoryId = oldCatId?.let { catIdMap[it] ?: it },
                accountId = acctIdMap[oldAcctId] ?: oldAcctId,
                targetAccountId = oldTargetId?.let { acctIdMap[it] ?: it },
                fee = o.optLong("fee", 0),
                counterparty = o.optStringNull("counterparty"),
                dueDate = o.optLongNull("due_date"),
                note = o.optStringNull("note"),
                transactionDate = o.getLong("transaction_date"),
                recurringId = o.optLongNull("recurring_id"),
                createdAt = now, updatedAt = now
            ))
            txnCount++
        }

        val budgets = root.optJSONArray("budgets")
        if (budgets != null) for (i in 0 until budgets.length()) {
            val o = budgets.getJSONObject(i)
            val oldCatId = o.optLongNull("category_id")
            budgetDao.insert(BudgetEntity(
                categoryId = oldCatId?.let { catIdMap[it] ?: it },
                amount = o.getLong("amount"),
                yearMonth = o.getString("year_month"),
                createdAt = now, updatedAt = now
            ))
            budgetCount++
        }

        val recurring = root.optJSONArray("recurring_templates")
        if (recurring != null) for (i in 0 until recurring.length()) {
            val o = recurring.getJSONObject(i)
            val oldSrc = o.getLong("source_account_id")
            val oldTgt = o.getLong("target_account_id")
            recurringDao.insert(RecurringEntity(
                name = o.getString("name"), amount = o.getLong("amount"),
                frequency = o.getString("frequency"),
                dayOfMonth = o.optIntNull("day_of_month"),
                dayOfWeek = o.optIntNull("day_of_week"),
                intervalDays = o.optIntNull("interval_days"),
                sourceAccountId = acctIdMap[oldSrc] ?: oldSrc,
                targetAccountId = acctIdMap[oldTgt] ?: oldTgt,
                nextDueDate = o.getLong("next_due_date"),
                isEnabled = o.optBoolean("is_enabled", true),
                createdAt = now, updatedAt = now
            ))
            recurringCount++
        }

        return ImportSummary(acctCount, catCount, txnCount, budgetCount, recurringCount)
    }

    private fun parseAccountJson(o: JSONObject, now: Long) = AccountEntity(
        name = o.getString("name"), type = o.getString("type"),
        subType = o.getString("sub_type"), balance = o.getLong("balance"),
        totalLimit = o.optLongNull("total_limit"),
        usedAmount = o.optLongNull("used_amount"),
        totalLoan = o.optLongNull("total_loan"),
        alreadyPaid = o.optLongNull("already_paid"),
        monthlyPayment = o.optLongNull("monthly_payment"),
        billDay = o.optIntNull("bill_day"),
        repaymentDay = o.optIntNull("repayment_day"),
        icon = o.getString("icon"), sortOrder = o.optInt("sort_order", 0),
        includeInTotal = o.optBoolean("include_in_total", true),
        createdAt = now, updatedAt = now
    )

    private fun JSONObject.optLongNull(key: String): Long? =
        if (isNull(key)) null else optLong(key)

    private fun JSONObject.optIntNull(key: String): Int? =
        if (isNull(key)) null else optInt(key)

    private fun JSONObject.optStringNull(key: String): String? =
        if (isNull(key)) null else optString(key)

    private fun encrypt(data: String, password: String): ByteArray {
        val salt = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data.toByteArray())
        return salt + iv + encrypted
    }

    private fun decrypt(data: ByteArray, password: String): String {
        val salt = data.copyOfRange(0, 16)
        val iv = data.copyOfRange(16, 32)
        val encrypted = data.copyOfRange(32, data.size)
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        return String(cipher.doFinal(encrypted))
    }

    private fun deriveKey(password: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(password.toCharArray(), salt, 65536, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return SecretKeySpec(bytes, "AES")
    }
}
