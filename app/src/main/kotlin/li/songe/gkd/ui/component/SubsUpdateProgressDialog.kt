package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import li.songe.gkd.util.updateSubsMutex
import top.yukonga.miuix.kmp.basic.InfiniteProgressIndicator
import top.yukonga.miuix.kmp.window.WindowDialog

/** 订阅下载/解析/刷新进行中时提示，避免用户以为卡住。 */
@Composable
fun SubsUpdateProgressDialog() {
    val loading by updateSubsMutex.state.collectAsState()
    WindowDialog(
        show = loading,
        title = "正在加载订阅",
        summary = "正在下载并解析订阅文件，请稍候…",
        onDismissRequest = null,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            InfiniteProgressIndicator()
        }
    }
}
