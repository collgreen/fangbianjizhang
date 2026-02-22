package com.example.fangbianjizhang.ui.settings

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

data class UpdateUiState(
    val checking: Boolean = false,
    val downloading: Boolean = false,
    val progress: Int = 0,
    val latestVersion: String? = null,
    val downloadUrl: String? = null,
    val error: String? = null
)

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val app: Application
) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(UpdateUiState())
    val uiState: StateFlow<UpdateUiState> = _state.asStateFlow()

    val currentVersion: String
        get() = app.packageManager.getPackageInfo(app.packageName, 0).versionName ?: "1.0.0"

    fun checkUpdate() {
        viewModelScope.launch {
            _state.value = UpdateUiState(checking = true)
            try {
                val release = withContext(Dispatchers.IO) { fetchLatest() }
                if (release != null && release.version != currentVersion) {
                    _state.value = UpdateUiState(
                        latestVersion = release.version,
                        downloadUrl = release.apkUrl
                    )
                } else {
                    _state.value = UpdateUiState(error = "already_latest")
                }
            } catch (e: Exception) {
                _state.value = UpdateUiState(error = e.message)
            }
        }
    }

    fun downloadAndInstall() {
        val url = _state.value.downloadUrl ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(downloading = true, progress = 0)
            try {
                val file = withContext(Dispatchers.IO) { downloadApk(url) }
                installApk(file)
                _state.value = _state.value.copy(downloading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(downloading = false, error = e.message)
            }
        }
    }

    fun dismiss() {
        _state.value = UpdateUiState()
    }

    private fun fetchLatest(): ReleaseInfo? {
        val urls = PROXIES.map { it + GITHUB_RELEASES } + GITHUB_RELEASES
        for (apiUrl in urls) {
            try {
                val conn = URL(apiUrl).openConnection() as HttpURLConnection
                conn.connectTimeout = 8_000
                conn.readTimeout = 8_000
                val arr = JSONArray(conn.inputStream.bufferedReader().use { it.readText() })
                conn.disconnect()
                if (arr.length() == 0) return null
                val rel = arr.getJSONObject(0)
                val tag = rel.getString("tag_name").removePrefix("v")
                val assets = rel.optJSONArray("assets")
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

    private fun downloadApk(url: String): File {
        val dir = File(app.cacheDir, "apk").apply { mkdirs() }
        val file = File(dir, "update.apk")
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 30_000
        try {
            val total = conn.contentLength
            conn.inputStream.use { input ->
                file.outputStream().use { output ->
                    val buf = ByteArray(8192)
                    var downloaded = 0L
                    var len: Int
                    while (input.read(buf).also { len = it } != -1) {
                        output.write(buf, 0, len)
                        downloaded += len
                        if (total > 0) {
                            _state.value = _state.value.copy(
                                progress = (downloaded * 100 / total).toInt()
                            )
                        }
                    }
                }
            }
        } finally {
            conn.disconnect()
        }
        return file
    }

    private fun installApk(file: File) {
        val uri = FileProvider.getUriForFile(app, "${app.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        app.startActivity(intent)
    }

    private data class ReleaseInfo(val version: String, val apkUrl: String?)

    companion object {
        private const val GITHUB_RELEASES =
            "https://api.github.com/repos/collgreen/fangbianjizhang/releases"
        private val PROXIES = listOf("https://ghfast.top/", "https://gh-proxy.com/")
    }
}
