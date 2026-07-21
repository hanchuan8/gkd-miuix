package li.songe.gkd.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import li.songe.gkd.ui.WebViewRoute
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.util.ShortUrlSet
import li.songe.gkd.util.copyText
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun ManualAuthDialog(
    commandText: String,
    show: Boolean,
    onUpdateShow: (Boolean) -> Unit,
) {
    if (show) {
        val mainVm = LocalMainViewModel.current
        PerfAlertDialog(
            onDismissRequest = { onUpdateShow(false) },
            title = { Text(text = "命令授权") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 360.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(text = "1. 有一台安装了 adb 的电脑\n\n2.手机开启调试模式后连接电脑授权调试\n\n3. 在电脑 cmd/pwsh 中运行如下命令")
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SelectionContainer(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = commandText,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MiuixTheme.colorScheme.surfaceContainerHigh)
                                    .padding(8.dp),
                                style = MiuixTheme.textStyles.body2,
                            )
                        }
                        PerfIcon(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clickable(onClick = throttle {
                                    copyText(commandText)
                                })
                                .padding(4.dp)
                                .size(20.dp),
                            imageVector = PerfIcon.ContentCopy,
                            tint = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.75f),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        modifier = Modifier
                            .clickable(onClick = throttle {
                                onUpdateShow(false)
                                mainVm.navigatePage(WebViewRoute(initUrl = ShortUrlSet.URL3))
                            }),
                        text = "运行后授权失败?",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    text = "关闭",
                    onClick = {
                        onUpdateShow(false)
                    },
                    modifier = Modifier.weight(1f),
                )
            },
        )
    }
}
