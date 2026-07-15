package li.songe.gkd.util

import android.content.Intent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import li.songe.gkd.ui.component.PerfAlertDialog
import androidx.compose.material3.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import io.ktor.client.call.body
import io.ktor.client.plugins.onDownload
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import li.songe.gkd.META
import li.songe.gkd.app
import li.songe.gkd.store.createAnyFlow
import li.songe.gkd.store.storeFlow
import java.io.File
import java.net.URI
import kotlin.time.Duration.Companion.days


private val UPDATE_URL: String
    get() = UpdateChannelOption.objects.findOption(storeFlow.value.updateChannel).url

@Serializable
data class NewVersion(
    val versionCode: Int,
    val versionName: String,
    val changelog: String,
    val downloadUrl: String,
    val fileSize: Long,
    val versionLogs: List<VersionLog> = emptyList(),
)

@Serializable
data class VersionLog(
    val name: String,
    val code: Int,
    val desc: String,
)

private var lastCheckTime = 0L

class UpdateStatus(val scope: CoroutineScope) {
    private val checkUpdatingMutex = MutexState()
    val checkUpdatingFlow
        get() = checkUpdatingMutex.state
    private val newVersionFlow = MutableStateFlow<NewVersion?>(null)
    private val downloadStatusFlow = MutableStateFlow<LoadStatus<File>?>(null)
    private var downloadJob: Job? = null

    private val ignoreVersionListFlow by lazy {
        createAnyFlow(
            key = "ignore_version_list",
            default = { emptySet<Int>() },
            scope = scope,
        )
    }
    private var lastManual = false

    val canRecheck get() = System.currentTimeMillis() - lastCheckTime > 1.days.inWholeMilliseconds

    fun checkUpdate(manual: Boolean = false) = scope.launchTry(Dispatchers.IO, silent = !manual) {
        lastManual = manual
        checkUpdatingMutex.whenUnLock {
            lastCheckTime = System.currentTimeMillis()
            if (!NetworkUtils.isAvailable()) {
                error("网络不可用")
            }
            val newVersion = client.get(UPDATE_URL).body<NewVersion>()
            if (newVersion.versionCode <= META.versionCode) {
                if (manual) toast("暂无更新")
                return@launchTry
            }
            if (!manual && ignoreVersionListFlow.value.contains(newVersion.versionCode)) return@launchTry
            newVersionFlow.value = newVersion
        }
    }.let { }

    private fun startDownload(newVersion: NewVersion) {
        if (downloadStatusFlow.value is LoadStatus.Loading) return
        downloadStatusFlow.value = LoadStatus.Loading(0f)
        val apkFile = sharedDir.resolve("gkd-v${newVersion.versionCode}.apk").apply {
            if (exists()) {
                delete()
            }
        }
        downloadJob = scope.launch(Dispatchers.IO) {
            try {
                val channel =
                    client.get(URI(UPDATE_URL).resolve(newVersion.downloadUrl).toString()) {
                        onDownload { bytesSentTotal, _ ->
                            val downloadStatus = downloadStatusFlow.value
                            if (downloadStatus is LoadStatus.Loading) {
                                downloadStatusFlow.value = LoadStatus.Loading(
                                    bytesSentTotal.toFloat() / (newVersion.fileSize)
                                )
                            } else if (downloadStatus is LoadStatus.Failure) {
                                // 提前终止下载
                                downloadJob?.cancel()
                            }
                        }
                    }.bodyAsChannel()
                if (downloadStatusFlow.value is LoadStatus.Loading) {
                    channel.copyAndClose(apkFile.writeChannel())
                    downloadStatusFlow.value = LoadStatus.Success(apkFile)
                }
            } catch (e: Exception) {
                if (downloadStatusFlow.value is LoadStatus.Loading) {
                    downloadStatusFlow.value = LoadStatus.Failure(e)
                }
            } finally {
                downloadJob = null
            }
        }
    }

    @Composable
    fun UpgradeDialog() {
        newVersionFlow.collectAsState().value?.let { newVersionVal ->
            val text = remember {
                val logs = newVersionVal.versionLogs.takeWhile { v ->
                    v.code > META.versionCode
                }
                "v${META.versionName} -> v${newVersionVal.versionName}\n\n${
                    if (logs.size > 1) {
                        logs.joinToString("\n\n") { v -> "v${v.name}\n${v.desc}" }
                    } else if (logs.isNotEmpty()) {
                        logs.first().desc
                    } else {
                        ""
                    }
                }".trimEnd()
            }
            PerfAlertDialog(
                title = {
                    Text(text = "新版本")
                },
                text = {
                    Text(
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    )
                },
                onDismissRequest = { },
                confirmButton = {
                    TextButton(
                        text = "下载更新",
                        onClick = {
                            newVersionFlow.value = null
                            startDownload(newVersionVal)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                },
                dismissButton = {
                    TextButton(
                        text = "取消",
                        onClick = { newVersionFlow.value = null },
                        modifier = Modifier.weight(1f),
                    )
                    if (!lastManual) {
                        TextButton(
                            text = "忽略",
                            onClick = {
                                newVersionFlow.value = null
                                ignoreVersionListFlow.update {
                                    it + newVersionVal.versionCode
                                }
                                toast("已忽略此版本")
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                },
            )
        }

        downloadStatusFlow.collectAsState().value?.let { downloadStatusVal ->
            when (downloadStatusVal) {
                is LoadStatus.Loading -> {
                    PerfAlertDialog(
                        title = { Text(text = "下载中") },
                        text = {
                            LinearProgressIndicator(
                                progress = { downloadStatusVal.progress },
                            )
                        },
                        onDismissRequest = {},
                        confirmButton = {
                            TextButton(
                                text = "终止下载",
                                onClick = {
                                    downloadStatusFlow.value = LoadStatus.Failure(
                                        Exception("终止下载")
                                    )
                                },
                                modifier = Modifier.weight(1f),
                            )
                        },
                    )
                }

                is LoadStatus.Failure -> {
                    PerfAlertDialog(
                        title = { Text(text = "下载失败") },
                        text = {
                            Text(text = downloadStatusVal.exception.let {
                                it.message ?: it.toString()
                            })
                        },
                        onDismissRequest = { downloadStatusFlow.value = null },
                        confirmButton = {
                            TextButton(
                                text = "关闭",
                                onClick = {
                                    downloadStatusFlow.value = null
                                },
                                modifier = Modifier.weight(1f),
                            )
                        },
                    )
                }

                is LoadStatus.Success -> {
                    PerfAlertDialog(
                        title = { Text(text = "下载完毕") },
                        text = {
                            Text(text = "可继续选择安装新版本")
                        },
                        onDismissRequest = {},
                        dismissButton = {
                            TextButton(
                                text = "关闭",
                                onClick = {
                                    downloadStatusFlow.value = null
                                },
                                modifier = Modifier.weight(1f),
                            )
                        },
                        confirmButton = {
                            TextButton(
                                text = "安装",
                                onClick = throttle {
                                    installApk(downloadStatusVal.result)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        })
                }
            }
        }
    }
}


private fun installApk(file: File) {
    val uri = FileProvider.getUriForFile(
        app,
        "${app.packageName}.provider",
        file
    )
    val intent = Intent(Intent.ACTION_VIEW).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        setDataAndType(uri, "application/vnd.android.package-archive")
    }
    app.tryStartActivity(intent)
}
