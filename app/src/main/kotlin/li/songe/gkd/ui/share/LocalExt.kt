package li.songe.gkd.ui.share

import androidx.compose.runtime.staticCompositionLocalOf
import li.songe.gkd.MainViewModel
import top.yukonga.miuix.kmp.blur.LayerBackdrop

val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
    error("not found MainViewModel")
}

val LocalDarkTheme = staticCompositionLocalOf { false }

val LocalIsTalkbackEnabled = staticCompositionLocalOf {
    false
}

/** MIUIX：顶栏是否处于毛玻璃透明态（由壳层提供） */
val LocalMiuixBlurActive = staticCompositionLocalOf { false }

/** 页面内容 [LayerBackdrop]，供液态玻璃 FAB 等采样背景 */
val LocalLayerBackdrop = staticCompositionLocalOf<LayerBackdrop?> { null }
