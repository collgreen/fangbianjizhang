package com.example.fangbianjizhang.domain.model

enum class AccountType { FUND, CREDIT, INVESTMENT, LOAN }

enum class AccountSubType {
    // FUND
    BANK_CARD, WECHAT, ALIPAY,
    // CREDIT
    CREDIT_CARD, HUABEI, BAITIAO,
    // INVESTMENT
    PENSION, FIXED_DEPOSIT, STOCK, FUTURES,
    // LOAN
    MORTGAGE, CAR_LOAN, CONSUMER_LOAN
}

enum class TransactionType(val label: String) {
    EXPENSE("支出"), INCOME("收入"), TRANSFER("转账"),
    LOAN_BORROW("借入"), LOAN_LEND("借出")
}

enum class CategoryType { EXPENSE, INCOME }

enum class Frequency { MONTHLY, WEEKLY, CUSTOM_DAYS }

enum class BudgetMode { NONE, TOTAL, PER_CATEGORY }

enum class ThemeMode { LIGHT, DARK, SYSTEM }
