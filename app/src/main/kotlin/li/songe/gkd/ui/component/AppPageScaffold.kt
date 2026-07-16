package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.share.LocalLayerBackdrop
import li.songe.gkd.ui.share.LocalMiuixBlurActive
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar as MiuixTopAppBar
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold

/**
 * 二级页壳：MIUIX TopAppBar + MiuixScrollBehavior + BlurredBar（surface @ 0.87）。
 */
@Composable
fun AppPageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val store by storeFlow.collectAsState()
    val blurActive = store.enableMiuixBlur && isRuntimeShaderSupported()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val barColor = if (blurActive) Color.Transparent else surfaceColor
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    CompositionLocalProvider(LocalLayerBackdrop provides backdrop) {
        MiuixScaffold(
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CompositionLocalProvider(LocalMiuixBlurActive provides blurActive) {
                    Box(
                        modifier = if (blurActive) {
                            Modifier.textureBlur(
                                backdrop = backdrop,
                                shape = RectangleShape,
                                blurRadius = 25f,
                                colors = BlurColors(
                                    blendColors = listOf(
                                        BlendColorEntry(color = surfaceColor.copy(alpha = 0.87f)),
                                    ),
                                ),
                            )
                        } else {
                            Modifier
                        },
                    ) {
                        MiuixTopAppBar(
                            title = title,
                            color = barColor,
                            navigationIcon = navigationIcon,
                            actions = actions,
                            scrollBehavior = scrollBehavior,
                            defaultWindowInsetsPadding = true,
                        )
                    }
                }
            },
            floatingActionButton = floatingActionButton,
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop),
                ) {
                    content(padding)
                }
            },
        )
    }
}
