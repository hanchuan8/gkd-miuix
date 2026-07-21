package li.songe.gkd.ui.component

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 圆形进度指示：内部仍用 Material3，调用点统一从此处引入，避免页面直引 material3。
 */
@Composable
fun AppCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = ProgressIndicatorDefaults.circularIndeterminateTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.CircularIndeterminateStrokeCap,
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth,
        trackColor = trackColor,
        strokeCap = strokeCap,
    )
}

/**
 * 线性进度指示（确定进度）：内部仍用 Material3，调用点统一从此处引入。
 */
@Composable
fun AppLinearProgressIndicator(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
) {
    LinearProgressIndicator(
        progress = progress,
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap,
    )
}

/**
 * 线性进度指示（不确定进度）。
 */
@Composable
fun AppLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MiuixTheme.colorScheme.primary,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor,
    strokeCap: StrokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
) {
    LinearProgressIndicator(
        modifier = modifier,
        color = color,
        trackColor = trackColor,
        strokeCap = strokeCap,
    )
}
