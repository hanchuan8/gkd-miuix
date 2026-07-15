package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.SmallTitle

/** 设置分组：圆角 Card + SmallTitle */
@Composable
fun PreferenceGroup(
    title: String? = null,
    showTop: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (title != null) {
        SmallTitle(
            text = title,
            modifier = if (showTop) Modifier else Modifier.padding(top = 0.dp),
        )
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        content = content,
    )
}
