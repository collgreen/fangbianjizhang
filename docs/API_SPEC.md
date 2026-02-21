# 内部接口规范

## 1. Repository 接口

### 1.1 AccountRepository

```kotlin
interface AccountRepository {
    fun getAllActive(): Flow<List<Account>>
    fun getByType(type: AccountType): Flow<List<Account>>
    fun getById(id: Long): Flow<Account?>
    suspend fun insert(account: Account): Long
    suspend fun update(account: Account)
    suspend fun softDelete(id: Long)
    suspend fun updateBalance(id: Long, delta: Long)
    suspend fun updateUsedAmount(id: Long, delta: Long)
    suspend fun updateAlreadyPaid(id: Long, delta: Long)
    fun getNetAsset(): Flow<NetAsset>
}

data class NetAsset(
    val fundTotal: Long,
    val investmentTotal: Long,
    val creditUsedTotal: Long,
    val loanRemainingTotal: Long
) {
    val net: Long get() = fundTotal + investmentTotal - creditUsedTotal - loanRemainingTotal
}
```

### 1.2 TransactionRepository

```kotlin
interface TransactionRepository {
    fun getByDateRange(start: Long, end: Long): Flow<List<Transaction>>
    fun getDailySummary(start: Long, end: Long): Flow<List<DailySummary>>
    fun getById(id: Long): Flow<Transaction?>
    suspend fun insert(transaction: Transaction)
    suspend fun update(transaction: Transaction)
    suspend fun softDelete(id: Long)
}

data class DailySummary(
    val dayTimestamp: Long,
    val totalExpense: Long,
    val totalIncome: Long,
    val transactions: List<Transaction>
)
```

### 1.3 CategoryRepository

```kotlin
interface CategoryRepository {
    fun getByType(type: CategoryType): Flow<List<Category>>
    fun getTopLevelByType(type: CategoryType): Flow<List<Category>>
    fun getChildren(parentId: Long): Flow<List<Category>>
    suspend fun insert(category: Category): Long
    suspend fun update(category: Category)
    suspend fun softDelete(id: Long)
    suspend fun updateSortOrder(ids: List<Long>)
}
```

### 1.4 BudgetRepository

```kotlin
interface BudgetRepository {
    fun getByYearMonth(yearMonth: String): Flow<List<Budget>>
    fun getTotalBudget(yearMonth: String): Flow<Budget?>
    suspend fun setTotalBudget(yearMonth: String, amount: Long)
    suspend fun setCategoryBudget(yearMonth: String, categoryId: Long, amount: Long)
    suspend fun clearByYearMonth(yearMonth: String)
}
```

### 1.5 RecurringRepository

```kotlin
interface RecurringRepository {
    fun getAll(): Flow<List<RecurringTemplate>>
    fun getDueTemplates(todayTimestamp: Long): Flow<List<RecurringTemplate>>
    suspend fun insert(template: RecurringTemplate): Long
    suspend fun update(template: RecurringTemplate)
    suspend fun setEnabled(id: Long, enabled: Boolean)
    suspend fun updateNextDueDate(id: Long, nextDate: Long)
    suspend fun softDelete(id: Long)
}
```

### 1.6 BackupRepository

```kotlin
interface BackupRepository {
    suspend fun exportJson(outputUri: Uri, password: String?): Result<Unit>
    suspend fun importJson(inputUri: Uri, password: String?): Result<ImportSummary>
    suspend fun exportCsv(outputUri: Uri, start: Long, end: Long): Result<Unit>
}

data class ImportSummary(
    val accountCount: Int,
    val categoryCount: Int,
    val transactionCount: Int
)
```

---

## 2. UseCase 列表

### 2.1 交易相关

```kotlin
// 插入交易（含账户余额更新）
class InsertTransactionUseCase(
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(transaction: Transaction): Result<Unit>
}

// 删除交易（恢复账户余额）
class DeleteTransactionUseCase(...)  {
    suspend operator fun invoke(id: Long): Result<Unit>
}

// 编辑交易（差额更新余额）
class UpdateTransactionUseCase(...) {
    suspend operator fun invoke(old: Transaction, new: Transaction): Result<Unit>
}
```

### 2.2 统计相关

```kotlin
// 月度分类统计（饼图数据）
class GetCategoryStatsUseCase(
    private val transactionRepo: TransactionRepository
) {
    operator fun invoke(
        start: Long, end: Long, type: TransactionType
    ): Flow<List<CategoryAmount>>
}

data class CategoryAmount(
    val categoryId: Long,
    val categoryName: String,
    val categoryIcon: String,
    val amount: Long,
    val percentage: Float
)

// 月度收支汇总
class GetMonthlySummaryUseCase(...) {
    operator fun invoke(year: Int, month: Int): Flow<MonthlySummary>
}

data class MonthlySummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val balance: Long  // income - expense
)

// 年度按月汇总
class GetYearlyStatsUseCase(...) {
    operator fun invoke(year: Int): Flow<List<MonthlyTotal>>
}

data class MonthlyTotal(
    val month: Int,
    val income: Long,
    val expense: Long
)
```

### 2.3 预算相关

```kotlin
// 获取当月预算使用情况
class GetBudgetStatusUseCase(
    private val budgetRepo: BudgetRepository,
    private val transactionRepo: TransactionRepository,
    private val prefsManager: PreferencesManager
) {
    operator fun invoke(yearMonth: String): Flow<BudgetStatus>
}

sealed class BudgetStatus {
    object NotSet : BudgetStatus()
    data class Total(val budget: Long, val spent: Long) : BudgetStatus()
    data class PerCategory(val items: List<CategoryBudgetItem>) : BudgetStatus()
}

data class CategoryBudgetItem(
    val categoryId: Long,
    val categoryName: String,
    val budget: Long,
    val spent: Long
)
```

### 2.4 定期扣款相关

```kotlin
// 执行到期的定期任务
class ExecuteRecurringUseCase(
    private val recurringRepo: RecurringRepository,
    private val transactionRepo: TransactionRepository,
    private val accountRepo: AccountRepository
) {
    suspend operator fun invoke(): Result<List<ExecutionResult>>
}

data class ExecutionResult(
    val templateId: Long,
    val templateName: String,
    val success: Boolean,
    val errorMessage: String?
)
```

---

## 3. ViewModel 状态定义

### 3.1 HomeViewModel

```kotlin
data class HomeUiState(
    val budgetStatus: BudgetStatus = BudgetStatus.NotSet,
    val dailySummaries: List<DailySummary> = emptyList(),
    val isLoading: Boolean = true
)
```

### 3.2 AssetViewModel

```kotlin
data class AssetUiState(
    val netAsset: NetAsset = NetAsset(0, 0, 0, 0),
    val fundAccounts: List<Account> = emptyList(),
    val creditAccounts: List<Account> = emptyList(),
    val investmentAccounts: List<Account> = emptyList(),
    val loanAccounts: List<Account> = emptyList(),
    val isLoading: Boolean = true
)
```

### 3.3 StatisticsViewModel

```kotlin
data class StatisticsUiState(
    val year: Int,
    val month: Int,
    val summary: MonthlySummary = MonthlySummary(0, 0, 0),
    val categoryStats: List<CategoryAmount> = emptyList(),
    val showExpense: Boolean = true,  // true=支出饼图, false=收入饼图
    val isCustomRange: Boolean = false,
    val customStart: Long? = null,
    val customEnd: Long? = null,
    val isLoading: Boolean = true
)

data class YearlyUiState(
    val year: Int,
    val monthlyTotals: List<MonthlyTotal> = emptyList(),
    val yearIncome: Long = 0,
    val yearExpense: Long = 0,
    val isLoading: Boolean = true
)
```

### 3.4 RecordViewModel

```kotlin
data class RecordUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: String = "",
    val categoryId: Long? = null,
    val subCategoryId: Long? = null,
    val accountId: Long? = null,
    val targetAccountId: Long? = null,
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val counterparty: String = "",
    val imageUri: String? = null,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val isSaving: Boolean = false
)
```

### 3.5 SettingsViewModel

```kotlin
data class SettingsUiState(
    val theme: ThemeMode = ThemeMode.SYSTEM,
    val currencySymbol: String = "¥",
    val monthStartDay: Int = 1,
    val autoCheckUpdate: Boolean = true,
    val budgetMode: BudgetMode = BudgetMode.NONE
)

enum class ThemeMode { LIGHT, DARK, SYSTEM }
enum class BudgetMode { NONE, TOTAL, PER_CATEGORY }
```
