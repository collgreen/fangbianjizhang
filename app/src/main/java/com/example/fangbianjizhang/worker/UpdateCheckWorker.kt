package com.example.fangbianjizhang.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fangbianjizhang.R
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

@HiltWorker
class UpdateCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val latest = fetchLatestVersion() ?: return Result.success()
            val current = applicationContext.packageManager
                .getPackageInfo(applicationContext.packageName, 0).versionName
            if (latest.name != current) {
                showUpdateNotification(latest.name, latest.downloadUrl)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    private fun fetchLatestVersion(): ReleaseInfo? {
        val urls = PROXIES.map { it + GITHUB_RELEASES } + RELEASES_URL
        for (apiUrl in urls) {
            try {
                val conn = URL(apiUrl).openConnection() as HttpURLConnection
                conn.connectTimeout = 8_000
                conn.readTimeout = 8_000
                val json = conn.inputStream.bufferedReader().use { it.readText() }
                conn.disconnect()
                val arr = JSONArray(json)
                if (arr.length() == 0) return null
                val release = arr.getJSONObject(0)
                val tag = release.getString("tag_name").removePrefix("v")
                val assets = release.optJSONArray("assets")
                val apkUrl = if (assets != null && assets.length() > 0) {
                    val raw = assets.getJSONObject(0).getString("browser_download_url")
                    val proxy = PROXIES.firstOrNull { apiUrl.startsWith(it) }
                    if (proxy != null) proxy + raw else raw
                } else null
                return ReleaseInfo(tag, apkUrl)
            } catch (_: Exception) {
                continue
            }
        }
        return null
    }

    private fun showUpdateNotification(version: String, downloadUrl: String?) {
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID, "版本更新", NotificationManager.IMPORTANCE_DEFAULT
        )
        nm.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("发现新版本 v$version")
            .setContentText("点击下载更新")
            .setAutoCancel(true)

        if (downloadUrl != null) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
            val pi = android.app.PendingIntent.getActivity(
                applicationContext, 0, intent,
                android.app.PendingIntent.FLAG_IMMUTABLE
            )
            builder.setContentIntent(pi)
        }

        nm.notify(NOTIFICATION_ID, builder.build())
    }

    private data class ReleaseInfo(val name: String, val downloadUrl: String?)

    companion object {
        const val WORK_NAME = "update_check"
        private const val CHANNEL_ID = "update_channel"
        private const val NOTIFICATION_ID = 1002
        private const val GITHUB_RELEASES =
            "https://api.github.com/repos/collgreen/fangbianjizhang/releases"
        private val PROXIES = listOf("https://ghfast.top/", "https://gh-proxy.com/")
        private const val RELEASES_URL = GITHUB_RELEASES
    }
}
