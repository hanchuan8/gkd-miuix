package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.TextButton

@Composable
fun AuthButtonGroup(
    buttons: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
    ) {
        buttons.forEach { (text, click) ->
            TextButton(text = text, onClick = throttle(click))
        }
    }
}
