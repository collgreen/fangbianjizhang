package com.example.fangbianjizhang.ui.navigation

object Routes {
    const val HOME = "home"
    const val ASSET = "asset"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val RECORD = "record"
    const val RECORD_EDIT = "record/{transactionId}"
    const val CATEGORY_MANAGE = "category_manage"
    const val BUDGET_SETTING = "budget_setting"
    const val RECURRING_MANAGE = "recurring_manage"
    const val ACCOUNT_MANAGE = "account_manage"

    fun recordEdit(transactionId: Long) = "record/$transactionId"
}
