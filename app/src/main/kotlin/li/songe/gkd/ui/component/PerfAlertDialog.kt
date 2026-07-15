package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog

/**
 * MIUIX 对话框。使用 [WindowDialog]（独立窗口），不依赖 Scaffold Overlay 宿主。
 */
@Composable
fun PerfAlertDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: @Composable (RowScope.() -> Unit)? = null,
    title: @Composable (() -> Unit)? = null,
    text: @Composable (() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
) {
    WindowDialog(
        show = true,
        modifier = modifier,
        onDismissRequest = onDismissRequest.takeIf {
            properties.dismissOnClickOutside || properties.dismissOnBackPress
        } ?: onDismissRequest,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            title?.let {
                CompositionLocalProvider(
                    LocalTextStyle provides MiuixTheme.textStyles.title3,
                    content = it,
                )
            }
            text?.let {
                CompositionLocalProvider(
                    LocalTextStyle provides MiuixTheme.textStyles.body1,
                    content = it,
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                dismissButton?.invoke(this)
                confirmButton()
            }
        }
    }
}
