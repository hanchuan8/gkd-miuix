package li.songe.gkd.ui.component

import androidx.compose.material3.LocalTextStyle
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalNumberCharWidth = compositionLocalOf { 0.dp }

@Composable
fun measureNumberTextWidth(style: TextStyle = LocalTextStyle.current): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure("0", style).size.width.toFloat()
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

/**
 * 时间等宽数字文本。
 *
 * 旧实现按字符拆成多个 Text，列表一屏会多出上百个节点，进页/滑动极易掉帧。
 * 现用单个 Text + `tnum`（tabular figures）保持数字列对齐。
 */
@Composable
fun FixedTimeText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(fontFeatureSettings = "tnum"),
        color = color,
        softWrap = false,
        maxLines = 1,
    )
}
