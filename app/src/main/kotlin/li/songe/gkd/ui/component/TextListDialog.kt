package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun TextListDialog(
    onDismiss: () -> Unit,
    textList: List<Pair<String, () -> Unit>>,
    title: String? = null,
) {
    WindowDialog(
        show = true,
        title = title,
        onDismissRequest = onDismiss,
    ) {
        Column {
            textList.forEach { (text, onClickItem) ->
                PerfDropdownMenuItem(
                    text = text,
                    onClick = throttle {
                        onDismiss()
                        onClickItem()
                    },
                )
            }
        }
    }
}
