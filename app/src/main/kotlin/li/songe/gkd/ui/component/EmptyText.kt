package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun EmptyText(text: String = "暂无数据") {
    Text(
        text = text,
        modifier = Modifier.fillMaxWidth(),
        style = MiuixTheme.textStyles.body2,
        textAlign = TextAlign.Center,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary.copy(alpha = 0.5f),
    )
}
