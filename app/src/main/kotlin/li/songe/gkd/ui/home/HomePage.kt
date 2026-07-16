package li.songe.gkd.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.liquid.IosLiquidGlassNavigationBar
import li.songe.gkd.ui.share.LocalLayerBackdrop
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.share.LocalMiuixBlurActive
import top.yukonga.miuix.kmp.basic.FloatingNavigationBar
import top.yukonga.miuix.kmp.basic.FloatingNavigationBarItem
import top.yukonga.miuix.kmp.basic.FloatingToolbarDefaults
import top.yukonga.miuix.kmp.basic.NavigationBar as MiuixNavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem as MiuixNavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationItem
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.basic.Scaffold as MiuixScaffold

sealed class BottomNavItem(
    val key: Int,
    val label: String,
    val icon: ImageVector,
) {
    object Control : BottomNavItem(
        key = 0,
        label = "首页",
        icon = PerfIcon.Home,
    )

    object SubsManage : BottomNavItem(
        key = 1,
        label = "订阅",
        icon = PerfIcon.FormatListBulleted,
    )

    object AppList : BottomNavItem(
        key = 2,
        label = "应用",
        icon = PerfIcon.Apps,
    )

    object Settings : BottomNavItem(
        key = 3,
        label = "设置",
        icon = PerfIcon.Settings,
    )

    companion object {
        val allSubObjects by lazy { arrayOf(Control, SubsManage, AppList, Settings) }
    }
}

@Serializable
data object HomeRoute : NavKey

@Composable
fun HomePage() {
    val mainVm = LocalMainViewModel.current
    viewModel<HomeVm>() // init state
    val tab by mainVm.tabFlow.collectAsState()
    val pages = arrayOf(useControlPage(), useSubsManagePage(), useAppListPage(), useSettingsPage())
    val page = pages.find { p -> p.navItem.key == tab } ?: pages.first()

    MiuixHomeScaffold(pages = pages, page = page, tab = tab)
}

@Composable
private fun MiuixHomeScaffold(
    pages: Array<ScaffoldExt>,
    page: ScaffoldExt,
    tab: Int,
) {
    val store by storeFlow.collectAsState()
    val useFloating = store.useFloatingNavBar
    val shaderOk = isRuntimeShaderSupported()
    val blurWanted = store.enableMiuixBlur && shaderOk
    // 液态玻璃依赖悬浮底栏 + 模糊
    val liquidGlass = useFloating && store.enableLiquidGlass && blurWanted

    if (useFloating) {
        MiuixFloatingNavScaffold(
            pages = pages,
            page = page,
            tab = tab,
            blurActive = blurWanted,
            liquidGlass = liquidGlass,
        )
    } else {
        MiuixDockedNavScaffold(
            pages = pages,
            page = page,
            tab = tab,
            blurActive = blurWanted,
        )
    }
}

/**
 * 普通贴底 NavigationBar（可选 textureBlur），对齐 MIUIX Demo 非悬浮模式。
 */
@Composable
private fun MiuixDockedNavScaffold(
    pages: Array<ScaffoldExt>,
    page: ScaffoldExt,
    tab: Int,
    blurActive: Boolean,
) {
    val mainVm = LocalMainViewModel.current
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val barColor = if (blurActive) Color.Transparent else surfaceColor
    val blurColors = BlurDefaults.blurColors(
        blendColors = listOf(
            BlendColorEntry(color = surfaceColor.copy(alpha = 0.8f)),
        ),
    )

    CompositionLocalProvider(LocalLayerBackdrop provides backdrop) {
        MiuixScaffold(
            modifier = page.modifier,
            topBar = {
                MiuixBlurredTopBar(
                    backdrop = backdrop,
                    blurActive = blurActive,
                    content = page.topBar,
                )
            },
            floatingActionButton = page.floatingActionButton,
            bottomBar = {
                Box(
                    modifier = Modifier
                        .then(
                            if (blurActive) {
                                Modifier.textureBlur(
                                    backdrop = backdrop,
                                    shape = RectangleShape,
                                    blurRadius = 25f,
                                    colors = blurColors,
                                )
                            } else {
                                Modifier
                            }
                        )
                        .background(barColor),
                ) {
                    MiuixNavigationBar(color = barColor) {
                        pages.forEach { p ->
                            MiuixNavigationBarItem(
                                selected = p.navItem.key == tab,
                                onClick = { mainVm.handleClickTab(p.navItem) },
                                icon = p.navItem.icon,
                                label = p.navItem.label,
                            )
                        }
                    }
                }
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .layerBackdrop(backdrop),
                ) {
                    page.content(padding)
                }
            },
        )
    }
}

/**
 * FloatingNavigationBar + 可选液态玻璃高光。
 * 底栏必须在 [layerBackdrop] 外，避免自引用闪退。
 */
@Composable
private fun MiuixFloatingNavScaffold(
    pages: Array<ScaffoldExt>,
    page: ScaffoldExt,
    tab: Int,
    blurActive: Boolean,
    liquidGlass: Boolean,
) {
    val mainVm = LocalMainViewModel.current
    val layoutDirection = LocalLayoutDirection.current
    val surfaceColor = MiuixTheme.colorScheme.surface
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val floatingBarShape = remember { RoundedCornerShape(FloatingToolbarDefaults.CornerRadius) }
    val blurColors = BlurDefaults.blurColors(
        blendColors = listOf(
            BlendColorEntry(color = surfaceContainer.copy(alpha = 0.6f)),
        ),
    )
    val backdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
    val glassActive = blurActive
    val floatingBarColor = if (glassActive) Color.Transparent else surfaceContainer
    val navInset = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // 悬浮底栏区域约 112dp + 系统导航；略加余量避免列表被胶囊栏遮住
    val floatingBarBody = if (liquidGlass) 72.dp + 40.dp else 52.dp + 40.dp
    val contentBottomSpace = 112.dp + navInset
    // 应用页白名单编辑仍有 FAB，再留出高度
    val hasFab = tab == BottomNavItem.AppList.key
    val listBottomSpace = if (hasFab) contentBottomSpace + 72.dp else contentBottomSpace

    CompositionLocalProvider(LocalLayerBackdrop provides backdrop) {
        MiuixScaffold(
            modifier = page.modifier,
            topBar = {
                MiuixBlurredTopBar(
                    backdrop = backdrop,
                    blurActive = blurActive,
                    content = page.topBar,
                )
            },
            // 空 bottomBar 时 FAB 只避开系统导航条，会被悬浮底栏挡住，需额外抬高
            floatingActionButton = {
                Box(modifier = Modifier.padding(bottom = floatingBarBody)) {
                    page.floatingActionButton()
                }
            },
            bottomBar = {},
            content = { padding ->
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop),
                    ) {
                        page.content(
                            PaddingValues(
                                start = padding.calculateStartPadding(layoutDirection),
                                top = padding.calculateTopPadding(),
                                end = padding.calculateEndPadding(layoutDirection),
                                bottom = listBottomSpace,
                            )
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                    ) {
                        val navItems = remember(pages) {
                            pages.map { NavigationItem(label = it.navItem.label, icon = it.navItem.icon) }
                        }
                        if (liquidGlass) {
                            // MIUIX Demo「iOS-like」液态玻璃悬浮底栏
                            IosLiquidGlassNavigationBar(
                                items = navItems,
                                selectedIndex = pages.indexOfFirst { it.navItem.key == tab }.coerceAtLeast(0),
                                onItemClick = { index ->
                                    pages.getOrNull(index)?.let { mainVm.handleClickTab(it.navItem) }
                                },
                                backdrop = backdrop,
                                isBlurActive = glassActive,
                            )
                        } else {
                            FloatingNavigationBar(
                                modifier = if (glassActive) {
                                    Modifier.textureBlur(
                                        backdrop = backdrop,
                                        shape = floatingBarShape,
                                        blurRadius = 25f,
                                        colors = blurColors,
                                        highlight = null,
                                    )
                                } else {
                                    Modifier
                                },
                                color = floatingBarColor,
                                defaultWindowInsetsPadding = true,
                            ) {
                                pages.forEach { p ->
                                    FloatingNavigationBarItem(
                                        selected = p.navItem.key == tab,
                                        onClick = { mainVm.handleClickTab(p.navItem) },
                                        icon = p.navItem.icon,
                                        label = p.navItem.label,
                                    )
                                }
                            }
                        }
                    }
                }
            },
        )
    }
}

/**
 * 顶栏毛玻璃：与底栏共用 [backdrop]，顶栏必须在 [layerBackdrop] 外，避免自引用。
 */
@Composable
private fun MiuixBlurredTopBar(
    backdrop: LayerBackdrop,
    blurActive: Boolean,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalMiuixBlurActive provides blurActive) {
        Box(
            modifier = if (blurActive) {
                Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = RectangleShape,
                    blurRadius = 25f,
                    // 顶栏毛玻璃：surface @ 0.87
                    colors = BlurColors(
                        blendColors = listOf(
                            BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(alpha = 0.87f)),
                        ),
                    ),
                )
            } else {
                Modifier
            },
        ) {
            content()
        }
    }
}
