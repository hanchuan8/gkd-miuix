package li.songe.gkd.ui.component

import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.selection.SelectionContainer
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import li.songe.gkd.util.copyText
import li.songe.gkd.util.throttle


@Composable
fun CopyTextCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        SelectionContainer(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(MiuixTheme.colorScheme.secondaryContainer)
                    .padding(8.dp),
                color = MiuixTheme.colorScheme.secondary,
                style = MiuixTheme.textStyles.body1,
            )
        }
        PerfIcon(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clickable(onClick = throttle {
                    copyText(text)
                })
                .padding(4.dp)
                .size(24.dp),
            imageVector = PerfIcon.ContentCopy,
            tint = MiuixTheme.colorScheme.secondary.copy(alpha = 0.75f),
        )
    }
}
