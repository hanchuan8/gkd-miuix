package li.songe.gkd.ui.component

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import li.songe.gkd.MainActivity
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.share.asMutableState
import li.songe.gkd.util.buildLogFile
import li.songe.gkd.util.launchTry
import li.songe.gkd.util.saveFileToDownloads
import li.songe.gkd.util.shareFile
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun ShareLogDlg(showShareLogDlgFlow: MutableStateFlow<Boolean>) {
    var visible by showShareLogDlgFlow.asMutableState()
    val mainVm = LocalMainViewModel.current
    val context = LocalActivity.current as MainActivity
    WindowDialog(
        show = visible,
        title = "分享日志",
        onDismissRequest = { visible = false },
    ) {
        PerfDropdownMenuItem(
            text = "分享到其他应用",
            onClick = throttle {
                visible = false
                mainVm.viewModelScope.launchTry(Dispatchers.IO) {
                    val logZipFile = buildLogFile()
                    context.shareFile(logZipFile, "分享日志文件")
                }
            },
        )
        PerfDropdownMenuItem(
            text = "保存到下载",
            onClick = throttle {
                visible = false
                mainVm.viewModelScope.launchTry(Dispatchers.IO) {
                    val logZipFile = buildLogFile()
                    context.saveFileToDownloads(logZipFile)
                }
            },
        )
        PerfDropdownMenuItem(
            text = "生成链接(需科学上网)",
            onClick = throttle {
                visible = false
                mainVm.uploadOptions.startTask(
                    getFile = { buildLogFile() },
                )
            },
        )
    }
}
