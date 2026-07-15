package li.songe.gkd.ui.component

import android.webkit.URLUtil
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.MutableStateFlow
import li.songe.gkd.util.openUri
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton

@Composable
fun TextDialog(
    textFlow: MutableStateFlow<String?>
) {
    val text = textFlow.collectAsState().value
    if (text != null) {
        val isUri = remember(text) { URLUtil.isNetworkUrl(text) }
        val onDismissRequest = {
            textFlow.value = null
        }
        PerfAlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(text = if (isUri) "查看链接" else "查看文本")
            },
            text = {
                CopyTextCard(text = text)
            },
            confirmButton = {
                if (isUri) {
                    TextButton(
                        text = "打开",
                        onClick = throttle {
                            onDismissRequest()
                            openUri(text)
                        },
                        modifier = Modifier.weight(1f),
                    )
                } else {
                    TextButton(
                        text = "关闭",
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                    )
                }
            },
        )
    }
}
