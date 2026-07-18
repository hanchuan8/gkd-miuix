package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Card

/** 设置分组：圆角 Card（MIUIX 下不展示分类小字标题） */
@Composable
fun PreferenceGroup(
    @Suppress("UNUSED_PARAMETER") title: String? = null,
    @Suppress("UNUSED_PARAMETER") showTop: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        content = content,
    )
}
