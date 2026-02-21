# 数据库设计

## 1. 概述

- **数据库**：Room (SQLite)
- **数据库名**：`fangbianjizhang.db`
- **金额字段**：全部使用 `Long` 类型，单位为**分**（禁止 Float/Double）
- **时间字段**：全部使用 `Long` 类型，Unix 毫秒时间戳
- **软删除**：所有表包含 `is_deleted` + `deleted_at` 字段
- **版本管理**：Room Migration，禁止 destructive migration

## 2. 表关系

```
accounts ──1:N──> transactions (account_id)
accounts ──1:N──> transactions (target_account_id, 转账)
categories ──自引用──> categories (parent_id, 两级)
categories ──1:N──> transactions (category_id)
categories ──1:N──> budgets (category_id)
accounts ──1:N──> recurring_templates (source_account_id)
accounts ──1:N──> recurring_templates (target_account_id)
```

---

## 3. 表结构

### 3.1 accounts（账户表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| name | String | NOT NULL | 账户名称 |
| type | String | NOT NULL | FUND / CREDIT / INVESTMENT / LOAN |
| sub_type | String | NOT NULL | 见下方枚举 |
| balance | Long | NOT NULL, DEFAULT 0 | 资金/投资账户余额（分） |
| total_limit | Long | NULL | 信用账户总额度（分） |
| used_amount | Long | NULL | 信用账户已用额度（分） |
| total_loan | Long | NULL | 贷款总额（分） |
| already_paid | Long | NULL | 已还金额（分） |
| monthly_payment | Long | NULL | 月供金额（分） |
| bill_day | Int | NULL | 账单日 1-31，仅 CREDIT |
| repayment_day | Int | NULL | 还款日 1-31 |
| icon | String | NOT NULL | 图标标识符 |
| sort_order | Int | NOT NULL, DEFAULT 0 | 排序权重 |
| include_in_total | Boolean | NOT NULL, DEFAULT true | 是否计入总资产 |
| is_deleted | Boolean | NOT NULL, DEFAULT false | 软删除 |
| deleted_at | Long | NULL | 删除时间戳 |
| created_at | Long | NOT NULL | 创建时间戳 |
| updated_at | Long | NOT NULL | 更新时间戳 |

**sub_type 枚举值**：

| type | sub_type 可选值 |
|------|----------------|
| FUND | BANK_CARD, WECHAT, ALIPAY |
| CREDIT | CREDIT_CARD, HUABEI, BAITIAO |
| INVESTMENT | PENSION, FIXED_DEPOSIT, STOCK, FUTURES |
| LOAN | MORTGAGE, CAR_LOAN, CONSUMER_LOAN |

**索引**：`(type)`, `(is_deleted)`

### 3.2 categories（分类表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| name | String | NOT NULL | 分类名称 |
| type | String | NOT NULL | EXPENSE / INCOME |
| parent_id | Long | NULL, FK → categories.id | 父分类ID，null=一级分类 |
| icon | String | NOT NULL | 图标标识符 |
| sort_order | Int | NOT NULL, DEFAULT 0 | 排序权重 |
| is_default | Boolean | NOT NULL, DEFAULT false | 是否系统默认 |
| is_deleted | Boolean | NOT NULL, DEFAULT false | 软删除 |
| deleted_at | Long | NULL | 删除时间戳 |
| created_at | Long | NOT NULL | 创建时间戳 |

**约束**：
- parent_id 只能指向 parent_id 为 null 的记录（应用层校验，限制两级）
- 系统默认分类（is_default=true）不可物理删除

**索引**：`(type, is_deleted)`, `(parent_id)`

### 3.3 transactions（交易流水表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| type | String | NOT NULL | EXPENSE / INCOME / TRANSFER / LOAN_BORROW / LOAN_LEND |
| amount | Long | NOT NULL | 金额（分），始终正数 |
| category_id | Long | NULL, FK → categories.id | 分类，转账时为null |
| account_id | Long | NOT NULL, FK → accounts.id | 主账户 |
| target_account_id | Long | NULL, FK → accounts.id | 目标账户，仅转账 |
| fee | Long | NOT NULL, DEFAULT 0 | 手续费（分） |
| counterparty | String | NULL | 借贷对方名称 |
| due_date | Long | NULL | 预计还款日 |
| note | String | NULL | 备注 |
| image_uri | String | NULL | 附件图片URI |
| transaction_date | Long | NOT NULL | 交易日期时间戳 |
| recurring_id | Long | NULL, FK → recurring_templates.id | 关联定期模板 |
| is_deleted | Boolean | NOT NULL, DEFAULT false | 软删除 |
| deleted_at | Long | NULL | 删除时间戳 |
| created_at | Long | NOT NULL | 创建时间戳 |
| updated_at | Long | NOT NULL | 更新时间戳 |

**索引**：
- `(is_deleted, transaction_date)` — 首页流水列表
- `(is_deleted, type, transaction_date)` — 统计查询
- `(account_id)`, `(category_id)`

### 3.4 budgets（预算表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| category_id | Long | NULL, FK → categories.id | 分类ID，TOTAL模式下为null |
| amount | Long | NOT NULL | 预算金额（分） |
| year_month | String | NOT NULL | 格式 "2026-02" |
| created_at | Long | NOT NULL | 创建时间戳 |
| updated_at | Long | NOT NULL | 更新时间戳 |

**唯一约束**：`UNIQUE(year_month, category_id)`
- category_id 为 null 时表示总预算（TOTAL 模式）
- category_id 非 null 时表示分类预算（PER_CATEGORY 模式）

**索引**：`(year_month)`

### 3.5 recurring_templates（定期扣款模板表）

| 字段 | 类型 | 约束 | 说明 |
|------|------|------|------|
| id | Long | PK, autoGenerate | 主键 |
| name | String | NOT NULL | 模板名称 |
| amount | Long | NOT NULL | 金额（分） |
| frequency | String | NOT NULL | MONTHLY / WEEKLY / CUSTOM_DAYS |
| day_of_month | Int | NULL | 每月几号，仅 MONTHLY |
| day_of_week | Int | NULL | 每周几(1-7)，仅 WEEKLY |
| interval_days | Int | NULL | 间隔天数，仅 CUSTOM_DAYS |
| source_account_id | Long | NOT NULL, FK → accounts.id | 扣款源账户 |
| target_account_id | Long | NOT NULL, FK → accounts.id | 收款目标账户 |
| next_due_date | Long | NOT NULL | 下次执行日期 |
| last_executed_at | Long | NULL | 上次执行时间 |
| is_enabled | Boolean | NOT NULL, DEFAULT true | 是否启用 |
| is_deleted | Boolean | NOT NULL, DEFAULT false | 软删除 |
| created_at | Long | NOT NULL | 创建时间戳 |
| updated_at | Long | NOT NULL | 更新时间戳 |

**索引**：`(is_enabled, is_deleted, next_due_date)`

---

## 4. 默认种子数据

### 4.1 默认支出分类

| 一级分类 | 二级分类 |
|----------|----------|
| 餐饮 | 早餐、午餐、晚餐、零食、饮料 |
| 生活 | 日用品、水电燃气、房租、物业 |
| 娱乐 | 电影、游戏、旅行 |
| 交通 | 公交地铁、打车、加油 |
| 教育 | 书籍、课程、培训 |

### 4.2 默认收入分类

| 一级分类 | 二级分类 |
|----------|----------|
| 工资 | 基本工资、奖金、补贴 |
| 兼职 | — |
| 理财收益 | 利息、分红 |
| 其他 | — |

种子数据在 `AppDatabase` 的 `Callback.onCreate()` 中插入，`is_default = true`。

---

## 5. 关键 SQL 查询模板

### 5.1 首页 — 按日分组流水

```sql
SELECT 
  (transaction_date / 86400000) AS day_key,
  type,
  SUM(amount) AS day_total
FROM transactions
WHERE is_deleted = 0
  AND transaction_date BETWEEN :monthStart AND :monthEnd
GROUP BY day_key, type
ORDER BY day_key DESC
```

### 5.2 首页 — 当日流水明细

```sql
SELECT t.*, c.name AS category_name, c.icon AS category_icon,
       pc.name AS parent_category_name
FROM transactions t
LEFT JOIN categories c ON t.category_id = c.id
LEFT JOIN categories pc ON c.parent_id = pc.id
WHERE t.is_deleted = 0
  AND t.transaction_date BETWEEN :dayStart AND :dayEnd
ORDER BY t.transaction_date DESC
```

### 5.3 统计 — 月度分类饼图（仅一级分类）

```sql
SELECT pc.id, pc.name, pc.icon, SUM(t.amount) AS total
FROM transactions t
JOIN categories c ON t.category_id = c.id
JOIN categories pc ON COALESCE(c.parent_id, c.id) = pc.id
WHERE t.is_deleted = 0
  AND t.type = :type  -- 'EXPENSE' 或 'INCOME'
  AND t.transaction_date BETWEEN :start AND :end
  AND pc.parent_id IS NULL
GROUP BY pc.id
ORDER BY total DESC
```

### 5.4 预算 — 当月使用情况

```sql
-- TOTAL 模式
SELECT b.amount AS budget,
       COALESCE(SUM(t.amount), 0) AS spent
FROM budgets b
LEFT JOIN transactions t
  ON t.type = 'EXPENSE'
  AND t.is_deleted = 0
  AND t.transaction_date BETWEEN :monthStart AND :monthEnd
WHERE b.year_month = :yearMonth
  AND b.category_id IS NULL

-- PER_CATEGORY 模式
SELECT b.category_id, c.name, b.amount AS budget,
       COALESCE(SUM(t.amount), 0) AS spent
FROM budgets b
JOIN categories c ON b.category_id = c.id
LEFT JOIN transactions t
  ON (t.category_id = b.category_id
      OR t.category_id IN (SELECT id FROM categories WHERE parent_id = b.category_id))
  AND t.type = 'EXPENSE'
  AND t.is_deleted = 0
  AND t.transaction_date BETWEEN :monthStart AND :monthEnd
WHERE b.year_month = :yearMonth
  AND b.category_id IS NOT NULL
GROUP BY b.category_id
```

### 5.5 定期扣款 — 到期任务

```sql
SELECT rt.*, 
       sa.name AS source_name, ta.name AS target_name
FROM recurring_templates rt
JOIN accounts sa ON rt.source_account_id = sa.id
JOIN accounts ta ON rt.target_account_id = ta.id
WHERE rt.is_enabled = 1
  AND rt.is_deleted = 0
  AND rt.next_due_date <= :todayTimestamp
```

### 5.6 年度账单 — 按月汇总

```sql
SELECT 
  SUBSTR(DATETIME(transaction_date/1000, 'unixepoch', 'localtime'), 1, 7) AS month,
  type,
  SUM(amount) AS total
FROM transactions
WHERE is_deleted = 0
  AND transaction_date BETWEEN :yearStart AND :yearEnd
GROUP BY month, type
ORDER BY month
```

### 5.7 净资产计算

```sql
-- 资金账户总余额
SELECT COALESCE(SUM(balance), 0) FROM accounts
WHERE type = 'FUND' AND is_deleted = 0 AND include_in_total = 1

-- 投资账户总市值
SELECT COALESCE(SUM(balance), 0) FROM accounts
WHERE type = 'INVESTMENT' AND is_deleted = 0 AND include_in_total = 1

-- 信用账户已用总额
SELECT COALESCE(SUM(used_amount), 0) FROM accounts
WHERE type = 'CREDIT' AND is_deleted = 0

-- 贷款剩余总额
SELECT COALESCE(SUM(total_loan - already_paid), 0) FROM accounts
WHERE type = 'LOAN' AND is_deleted = 0
```

---

## 6. 数据库版本迁移策略

- 初始版本：version = 1
- 每次 schema 变更编写 `Migration(N, N+1)` 类
- 禁止使用 `fallbackToDestructiveMigration()`（生产环境）
- 迁移测试：使用 Room 的 `MigrationTestHelper` 验证每个迁移

```kotlin
// 示例
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE accounts ADD COLUMN note TEXT DEFAULT ''")
    }
}
```

## 7. 备份 JSON Schema

```json
{
  "schema_version": 1,
  "exported_at": "2026-02-20T10:30:00+08:00",
  "app_version": "1.0.0",
  "accounts": [
    {
      "id": 1, "name": "招商银行", "type": "FUND",
      "sub_type": "BANK_CARD", "balance": 1000000,
      "...": "..."
    }
  ],
  "categories": [],
  "transactions": [],
  "budgets": [],
  "recurring_templates": []
}
```

导入时根据 `schema_version` 决定是否需要数据迁移转换。
