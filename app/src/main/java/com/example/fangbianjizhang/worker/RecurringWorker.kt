package com.example.fangbianjizhang.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fangbianjizhang.R
import com.example.fangbianjizhang.domain.usecase.recurring.ExecuteRecurringUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class RecurringWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val executeRecurring: ExecuteRecurringUseCase
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val results = executeRecurring()
            if (results.isNotEmpty()) {
                val success = results.count { it.success }
                val failed = results.count { !it.success }
                showNotification(success, failed)
            }
            if (results.any { !it.success }) Result.retry() else Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun showNotification(success: Int, failed: Int) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "定期扣款", NotificationManager.IMPORTANCE_DEFAULT)
        nm.createNotificationChannel(channel)

        val text = buildString {
            if (success > 0) append("成功执行 $success 笔")
            if (failed > 0) {
                if (success > 0) append("，")
                append("$failed 笔失败")
            }
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("定期扣款")
            .setContentText(text)
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    companion object {
        const val WORK_NAME = "recurring_check"
        private const val CHANNEL_ID = "recurring_channel"
        private const val NOTIFICATION_ID = 1001
    }
}
