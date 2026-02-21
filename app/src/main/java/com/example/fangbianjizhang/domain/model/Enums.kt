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

enum class TransactionType { EXPENSE, INCOME, TRANSFER, LOAN_BORROW, LOAN_LEND }

enum class CategoryType { EXPENSE, INCOME }

enum class Frequency { MONTHLY, WEEKLY, CUSTOM_DAYS }

enum class BudgetMode { NONE, TOTAL, PER_CATEGORY }

enum class ThemeMode { LIGHT, DARK, SYSTEM }
