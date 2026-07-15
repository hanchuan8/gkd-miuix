package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import li.songe.gkd.ui.share.LocalMiuixBlurActive
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.TopAppBarDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 顶栏：始终使用 MIUIX [TopAppBar]。
 * 搜索等自定义内容放 [bottomContent]；[titleText]/[subtitle] 为字符串标题。
 * 毛玻璃激活时容器透明，由外层 textureBlur 提供模糊底。
 */
@Composable
fun PerfTopAppBar(
    modifier: Modifier = Modifier,
    titleText: String = "",
    subtitle: String = "",
    bottomContent: @Composable () -> Unit = {},
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    color: Color = Color.Unspecified,
    titleColor: Color = MiuixTheme.colorScheme.onSurface,
    largeTitleColor: Color = MiuixTheme.colorScheme.onSurface,
    subtitleColor: Color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    titlePadding: Dp = TopAppBarDefaults.TitlePadding,
    miuixScrollBehavior: ScrollBehavior? = null,
    canScroll: Boolean = true,
) {
    val blurActive = LocalMiuixBlurActive.current
    val barColor = when {
        color != Color.Unspecified -> color
        blurActive -> Color.Transparent
        else -> MiuixTheme.colorScheme.surface
    }
    TopAppBar(
        title = titleText,
        subtitle = subtitle,
        bottomContent = bottomContent,
        modifier = modifier,
        color = barColor,
        titleColor = titleColor,
        largeTitleColor = largeTitleColor,
        subtitleColor = subtitleColor,
        titlePadding = titlePadding,
        navigationIcon = navigationIcon,
        actions = actions,
        scrollBehavior = if (canScroll) miuixScrollBehavior else null,
        defaultWindowInsetsPadding = true,
    )
}
