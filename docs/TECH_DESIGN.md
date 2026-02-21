# 技术架构设计

## 1. 架构总览

采用 MVVM + Clean Architecture 三层分离：

```
┌─────────────────────────────────────┐
│         Presentation Layer          │
│   Compose Screens + ViewModels      │
│   (UI 状态管理、用户交互)            │
├─────────────────────────────────────┤
│           Domain Layer              │
│   UseCases + Domain Models          │
│   + Repository Interfaces           │
│   (业务逻辑、不依赖任何框架)         │
├─────────────────────────────────────┤
│            Data Layer               │
│   Repository Impl + Room DAOs       │
│   + DataStore + Mappers             │
│   (数据存取、外部接口)               │
└─────────────────────────────────────┘
```

依赖方向：Presentation → Domain ← Data（Domain 层不依赖其他层）

## 2. 包结构

```
com.example.fangbianjizhang/
├── di/                          # Hilt 模块
│   ├── DatabaseModule.kt        # Room 数据库提供
│   ├── RepositoryModule.kt      # Repository 绑定
│   └── DataStoreModule.kt       # DataStore 提供
├── data/
│   ├── local/
│   │   ├── db/
│   │   │   ├── AppDatabase.kt          # Room 数据库定义
│   │   │   ├── dao/
│   │   │   │   ├── AccountDao.kt
│   │   │   │   ├── TransactionDao.kt
│   │   │   │   ├── CategoryDao.kt
│   │   │   │   ├── BudgetDao.kt
│   │   │   │   └── RecurringDao.kt
│   │   │   ├── entity/
│   │   │   │   ├── AccountEntity.kt
│   │   │   │   ├── TransactionEntity.kt
│   │   │   │   ├── CategoryEntity.kt
│   │   │   │   ├── BudgetEntity.kt
│   │   │   │   └── RecurringEntity.kt
│   │   │   └── converter/
│   │   │       └── Converters.kt        # TypeConverter
│   │   └── datastore/
│   │       └── PreferencesManager.kt    # DataStore 封装
│   ├── repository/
│   │   ├── AccountRepositoryImpl.kt
│   │   ├── TransactionRepositoryImpl.kt
│   │   ├── CategoryRepositoryImpl.kt
│   │   ├── BudgetRepositoryImpl.kt
│   │   ├── RecurringRepositoryImpl.kt
│   │   └── BackupRepositoryImpl.kt
│   └── mapper/
│       ├── AccountMapper.kt             # Entity <-> Domain
│       ├── TransactionMapper.kt
│       └── CategoryMapper.kt
├── domain/
│   ├── model/
│   │   ├── Account.kt
│   │   ├── Transaction.kt
│   │   ├── Category.kt
│   │   ├── Budget.kt
│   │   ├── RecurringTemplate.kt
│   │   └── enums.kt                    # AccountType, TransactionType 等
│   ├── repository/
│   │   ├── AccountRepository.kt         # 接口
│   │   ├── TransactionRepository.kt
│   │   ├── CategoryRepository.kt
│   │   ├── BudgetRepository.kt
│   │   ├── RecurringRepository.kt
│   │   └── BackupRepository.kt
│   └── usecase/
│       ├── account/
│       ├── transaction/
│       ├── category/
│       ├── budget/
│       ├── statistics/
│       ├── recurring/
│       └── backup/
├── ui/
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   ├── navigation/
│   │   ├── AppNavHost.kt
│   │   ├── BottomNavBar.kt
│   │   └── Routes.kt
│   ├── common/                          # 共享组件
│   │   ├── AmountText.kt
│   │   ├── CategoryIcon.kt
│   │   └── BudgetProgressBar.kt
│   ├── home/
│   │   ├── HomeScreen.kt
│   │   └── HomeViewModel.kt
│   ├── asset/
│   │   ├── AssetScreen.kt
│   │   └── AssetViewModel.kt
│   ├── statistics/
│   │   ├── StatisticsScreen.kt
│   │   ├── StatisticsViewModel.kt
│   │   └── PieChart.kt
│   ├── settings/
│   │   ├── SettingsScreen.kt
│   │   ├── CategoryManageScreen.kt
│   │   ├── BudgetSettingScreen.kt
│   │   └── SettingsViewModel.kt
│   └── record/
│       ├── RecordScreen.kt
│       └── RecordViewModel.kt
├── worker/
│   ├── RecurringWorker.kt               # 定期扣款检查
│   └── UpdateCheckWorker.kt             # 版本更新检查
└── util/
    ├── AmountFormatter.kt               # 分 -> 元 格式化
    ├── DateUtils.kt                     # 日期工具
    └── Constants.kt
```

## 3. 技术选型

| 技术 | 版本要求 | 用途 |
|------|----------|------|
| Kotlin | 2.0+ | 开发语言 |
| Jetpack Compose | BOM 最新稳定版 | 声明式 UI |
| Material 3 | 随 Compose BOM | 设计系统 |
| Room | 2.6+ | 本地数据库 ORM |
| Hilt | 2.51+ | 依赖注入 |
| Coroutines | 1.8+ | 异步编程 |
| Flow | 随 Coroutines | 响应式数据流 |
| WorkManager | 2.9+ | 后台定时任务 |
| DataStore | 1.1+ | 轻量偏好存储 |
| Navigation Compose | 2.8+ | 页面导航 |
| Vico / MPAndroidChart | 最新 | 图表（饼图、柱状图） |

## 4. 核心设计决策

### 4.1 金额存储：Long（分）

```kotlin
// 工具函数
object AmountFormatter {
    fun toLong(yuan: String): Long =
        (yuan.toBigDecimal() * 100.toBigDecimal()).toLong()

    fun toDisplay(fen: Long): String =
        String.format("%.2f", fen / 100.0)
}
```

所有金额在 UI 层输入时转为分，显示时转为元。计算全程使用 Long，禁止 Float/Double。

### 4.2 账户余额更新（原子性）

所有余额变动在 Room `@Transaction` 中执行：

```kotlin
@Transaction
suspend fun insertExpense(transaction: TransactionEntity) {
    transactionDao.insert(transaction)
    when (account.type) {
        "FUND" -> accountDao.decreaseBalance(transaction.account_id, transaction.amount)
        "CREDIT" -> accountDao.increaseUsedAmount(transaction.account_id, transaction.amount)
    }
}
```

各账户类型余额变动规则：

| 操作 | 资金账户 | 信用账户 | 投资账户 | 贷款账户 |
|------|----------|----------|----------|----------|
| 支出 | balance -= amount | used_amount += amount | — | — |
| 收入 | balance += amount | — | — | — |
| 转出 | balance -= amount | used_amount -= amount(还款) | balance -= amount | — |
| 转入 | balance += amount | used_amount -= amount | balance += amount | already_paid += amount |

### 4.3 预算模式互斥

DataStore 存储 `budget_mode: String`（"TOTAL" / "PER_CATEGORY" / "NONE"）。

切换逻辑：
1. 用户选择新模式 → 弹出确认对话框
2. 确认后删除旧模式的 budgets 记录
3. 更新 DataStore 中的 budget_mode
4. 跳转到对应的预算设置页面

### 4.4 软删除

所有查询默认添加 `WHERE is_deleted = 0`。Room DAO 示例：

```kotlin
@Query("SELECT * FROM accounts WHERE is_deleted = 0 ORDER BY sort_order")
fun getAllActive(): Flow<List<AccountEntity>>
```

## 5. 核心数据流

### 5.1 记账流程

```
用户输入金额/分类/账户
    ↓
RecordViewModel.save()
    ↓
InsertTransactionUseCase.invoke(transaction)
    ↓
TransactionRepository.insert()  ← Room @Transaction
    ├── TransactionDao.insert(entity)
    └── AccountDao.updateBalance(accountId, delta)
    ↓
Flow 自动通知 → HomeViewModel 刷新列表
```

### 5.2 统计查询流程

```
StatisticsViewModel 订阅 Flow
    ↓
GetMonthlyStatsUseCase.invoke(year, month)
    ↓
TransactionRepository.getMonthlyByCategory(start, end, type)
    ↓
Room SQL 聚合 → List<CategoryAmount>
    ↓
ViewModel 转为饼图数据 → UI 渲染
```

### 5.3 定期扣款流程

```
WorkManager 每日触发 RecurringWorker
    ↓
查询 next_due_date <= today 且 is_enabled 的模板
    ↓
对每个到期模板（在 @Transaction 中）：
    ├── 创建 TRANSFER 类型 Transaction
    ├── 更新源账户余额（减少）
    ├── 更新目标账户余额/额度（增加/减少）
    └── 更新 next_due_date 为下一周期
    ↓
成功 → 发送通知
失败 → 发送警告通知 + Result.retry()
```

## 6. 错误处理

Repository 层统一返回 `Result<T>`：

```kotlin
sealed class AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>()
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>()
}
```

ViewModel 将错误转为 UI 状态：

```kotlin
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

- 数据库操作失败 → SnackBar 提示用户
- WorkManager 任务失败 → Notification 通知 + 自动重试
- 导入导出失败 → Dialog 显示详细错误信息

## 7. 安全考虑

| 措施 | 说明 |
|------|------|
| 私有存储 | 数据库文件在 app 私有目录，其他应用无法访问 |
| 备份加密 | 导出 JSON 可选 AES-256 密码加密 |
| 网络权限 | 仅热更新功能需要 INTERNET 权限 |
| 混淆 | Release 包启用 R8/ProGuard |
| 输入校验 | 金额输入限制数字和小数点，最多两位小数 |
