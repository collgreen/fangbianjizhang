package com.example.fangbianjizhang.domain.repository

import android.net.Uri

data class ImportSummary(
    val accountCount: Int,
    val categoryCount: Int,
    val transactionCount: Int
)

interface BackupRepository {
    suspend fun exportJson(outputUri: Uri, password: String?): Result<Unit>
    suspend fun importJson(inputUri: Uri, password: String?): Result<ImportSummary>
    suspend fun exportCsv(outputUri: Uri, start: Long, end: Long): Result<Unit>
}
