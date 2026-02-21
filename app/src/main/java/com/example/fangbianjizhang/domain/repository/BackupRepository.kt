package com.example.fangbianjizhang.domain.repository

import android.net.Uri

data class ImportSummary(
    val accountCount: Int,
    val categoryCount: Int,
    val transactionCount: Int,
    val budgetCount: Int = 0,
    val recurringCount: Int = 0
)

data class ImportConflict(
    val duplicateAccountNames: List<String>
)

interface BackupRepository {
    suspend fun exportJson(outputUri: Uri, password: String?): Result<Unit>
    suspend fun checkImportConflicts(inputUri: Uri, password: String?): Result<ImportConflict>
    suspend fun importJson(inputUri: Uri, password: String?, overwriteAccounts: Boolean = false): Result<ImportSummary>
    suspend fun exportCsv(outputUri: Uri, start: Long, end: Long): Result<Unit>
}
