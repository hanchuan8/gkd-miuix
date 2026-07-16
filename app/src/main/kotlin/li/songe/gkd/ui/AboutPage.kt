package li.songe.gkd.ui

import android.os.Build
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.BlendMode as ComposeBlendMode
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import li.songe.gkd.META
import li.songe.gkd.MainActivity
import li.songe.gkd.R
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.component.PerfAlertDialog
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.RotatingLoadingIcon
import li.songe.gkd.ui.component.SettingItem
import li.songe.gkd.ui.component.TextListDialog
import li.songe.gkd.ui.component.TextMenu
import li.songe.gkd.ui.component.effect.BgEffectBackground
import li.songe.gkd.ui.component.effect.ColorBlendToken
import li.songe.gkd.ui.component.waitResult
import li.songe.gkd.ui.share.LocalDarkTheme
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.share.asMutableState
import li.songe.gkd.util.ISSUES_URL
import li.songe.gkd.util.PLAY_STORE_URL
import li.songe.gkd.util.REPOSITORY_URL
import li.songe.gkd.util.ShortUrlSet
import li.songe.gkd.util.UpdateChannelOption
import li.songe.gkd.util.findOption
import li.songe.gkd.util.format
import li.songe.gkd.util.getShareApkFile
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.launchTry
import li.songe.gkd.util.openUri
import li.songe.gkd.util.saveFileToDownloads
import li.songe.gkd.util.shareFile
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toast
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.SmallTopAppBar
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurBlendMode
import top.yukonga.miuix.kmp.blur.BlurColors
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Serializable
data object AboutRoute : NavKey

@Composable
fun AboutPage() {
    val context = LocalActivity.current as MainActivity
    val mainVm = LocalMainViewModel.current
    val vm = viewModel<AboutVm>()
    val store by storeFlow.collectAsState()
    val darkTheme = LocalDarkTheme.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current

    var showInfoDlg by vm.showInfoDlgFlow.asMutableState()
    if (showInfoDlg) {
        PerfAlertDialog(
            onDismissRequest = { showInfoDlg = false },
            title = { Text(text = "版本信息") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column {
                        Text(text = "构建渠道")
                        Text(text = META.channel)
                    }
                    Column {
                        Text(text = "版本代码")
                        Text(text = META.versionCode.toString())
                    }
                    Column {
                        Text(text = "版本名称")
                        Text(text = META.versionName)
                    }
                    Column {
                        Text(text = "代码记录")
                        Text(
                            modifier = Modifier.clickable { openUri(META.commitUrl) },
                            text = META.tagName ?: META.commitId.substring(0, 16),
                            color = MiuixTheme.colorScheme.primary,
                            style = LocalTextStyle.current.copy(textDecoration = TextDecoration.Underline),
                        )
                    }
                    Column {
                        Text(text = "提交时间")
                        Text(text = META.commitTime.format("yyyy-MM-dd HH:mm:ss ZZ"))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    text = "关闭",
                    onClick = { showInfoDlg = false },
                    modifier = Modifier.weight(1f),
                )
            },
        )
    }

    var showShareAppDlg by vm.showShareAppDlgFlow.asMutableState()

    val scrollBehavior = MiuixScrollBehavior()
    val lazyListState = rememberLazyListState()
    var logoHeightPx by remember { mutableIntStateOf(0) }
    var logoHeightDp by remember { mutableStateOf(280.dp) }

    val scrollProgress by remember {
        derivedStateOf {
            if (logoHeightPx <= 0) {
                0f
            } else {
                val index = lazyListState.firstVisibleItemIndex
                val offset = lazyListState.firstVisibleItemScrollOffset
                // 滑过 Logo 占位后视为 1，渐变完全收起，页面只剩 surface 底色
                if (index > 0) 1f else (offset.toFloat() / logoHeightPx).coerceIn(0f, 1f)
            }
        }
    }

    val enableBlur = store.enableMiuixBlur && isRuntimeShaderSupported()
    val effectBackground =
        enableBlur && Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM
    val surfaceColor = MiuixTheme.colorScheme.surface
    val backdrop = rememberLayerBackdrop()
    val barBackdrop = rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }

    val blendColors = remember(darkTheme) {
        if (darkTheme) ColorBlendToken.Overlay_Thin_Light
        else ColorBlendToken.Pured_Regular_Light
    }
    val logoBlend = remember(darkTheme) {
        if (darkTheme) {
            listOf(
                BlendColorEntry(Color(0xe6a1a1a1), BlurBlendMode.ColorDodge),
                BlendColorEntry(Color(0x4de6e6e6), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af500), BlurBlendMode.Lab),
            )
        } else {
            listOf(
                BlendColorEntry(Color(0xcc4a4a4a), BlurBlendMode.ColorBurn),
                BlendColorEntry(Color(0xff4f4f4f), BlurBlendMode.LinearLight),
                BlendColorEntry(Color(0xff1af200), BlurBlendMode.Lab),
            )
        }
    }

    val blurActive = enableBlur && scrollProgress >= 0.99f
    val barColor = when {
        blurActive -> Color.Transparent
        scrollProgress >= 0.99f -> MiuixTheme.colorScheme.surface
        else -> Color.Transparent
    }
    val titleAlpha = ((scrollProgress - 0.35f) / 0.65f).coerceIn(0f, 1f)
    val heroAlpha = 1f - scrollProgress

    Scaffold(
        topBar = {
            Box(
                modifier = if (blurActive) {
                    Modifier.textureBlur(
                        backdrop = barBackdrop,
                        shape = RectangleShape,
                        blurRadius = 25f,
                        colors = BlurColors(
                            blendColors = listOf(
                                BlendColorEntry(
                                    color = MiuixTheme.colorScheme.surface.copy(alpha = 0.87f),
                                ),
                            ),
                        ),
                    )
                } else {
                    Modifier
                },
            ) {
                SmallTopAppBar(
                    title = "关于",
                    scrollBehavior = scrollBehavior,
                    color = barColor,
                    titleColor = MiuixTheme.colorScheme.onSurface.copy(alpha = titleAlpha),
                    navigationIcon = {
                        PerfIconButton(
                            imageVector = PerfIcon.ArrowBack,
                            onClick = { mainVm.popPage() },
                        )
                    },
                    actions = {
                        PerfIconButton(
                            imageVector = PerfIcon.Share,
                            onClick = { showShareAppDlg = true },
                        )
                    },
                )
            }
        },
        contentWindowInsets = WindowInsets.systemBars
            .add(WindowInsets.displayCutout)
            .only(WindowInsetsSides.Horizontal),
    ) { innerPadding ->
        Box(
            modifier = if (enableBlur) {
                Modifier
                    .fillMaxSize()
                    .layerBackdrop(barBackdrop)
            } else {
                Modifier.fillMaxSize()
            },
        ) {
            BgEffectBackground(
                dynamicBackground = effectBackground,
                modifier = Modifier.fillMaxSize(),
                bgModifier = if (enableBlur) Modifier.layerBackdrop(backdrop) else Modifier,
                isFullSize = true,
                effectBackground = effectBackground,
                alpha = { heroAlpha },
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            top = innerPadding.calculateTopPadding() + 72.dp,
                            start = innerPadding.calculateStartPadding(layoutDirection),
                            end = innerPadding.calculateEndPadding(layoutDirection),
                        )
                        .onSizeChanged { size ->
                            with(density) { logoHeightDp = size.height.toDp() }
                        }
                        .graphicsLayer {
                            alpha = heroAlpha
                            scaleX = 1f - (scrollProgress * 0.04f)
                            scaleY = 1f - (scrollProgress * 0.04f)
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(100.dp)
                            .clipToBounds()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                                onClick = throttle { toast("你干嘛~ 哎呦~") },
                            ),
                    ) {
                        Image(
                            modifier = Modifier
                                .requiredSize(220.dp)
                                .then(
                                    if (enableBlur) {
                                        Modifier.textureBlur(
                                            backdrop = backdrop,
                                            shape = RoundedCornerShape(0.dp),
                                            blurRadius = 150f,
                                            colors = BlurColors(blendColors = logoBlend),
                                            contentBlendMode = ComposeBlendMode.DstIn,
                                            enabled = true,
                                        )
                                    } else {
                                        Modifier
                                    },
                                ),
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            colorFilter = ColorFilter.tint(MiuixTheme.colorScheme.onBackground),
                            contentDescription = null,
                        )
                    }
                    Text(
                        modifier = Modifier
                            .padding(top = 12.dp, bottom = 4.dp)
                            .clickable { showInfoDlg = true }
                            .then(
                                if (enableBlur) {
                                    Modifier.textureBlur(
                                        backdrop = backdrop,
                                        shape = RoundedCornerShape(0.dp),
                                        blurRadius = 150f,
                                        colors = BlurColors(blendColors = logoBlend),
                                        contentBlendMode = ComposeBlendMode.DstIn,
                                        enabled = true,
                                    )
                                } else {
                                    Modifier
                                },
                            ),
                        text = META.appName,
                        color = MiuixTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                    )
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showInfoDlg = true },
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        text = META.versionName,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .scrollEndHaptic()
                        .overScrollVertical()
                        .nestedScroll(scrollBehavior.nestedScrollConnection),
                    contentPadding = PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                    ),
                    overscrollEffect = null,
                ) {
                    // 透明占位 = Logo 区高度；必须足够高才能滑到 scrollProgress=1，渐变完全收起
                    item(key = "logoSpacer") {
                        val logoPadTop = innerPadding.calculateTopPadding() + 40.dp + 52.dp
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(
                                    logoHeightDp + logoPadTop -
                                        innerPadding.calculateTopPadding() + 126.dp,
                                )
                                .onSizeChanged { logoHeightPx = it.height },
                        )
                    }

                    // 对齐 KernelSU：fillParentMaxHeight 保证能滑满，卡片浮在渐变上（不要整块 surface 白底）
                    item(key = "aboutBody") {
                        Column(
                            modifier = Modifier
                                .fillParentMaxHeight()
                                .padding(
                                    bottom = innerPadding.calculateBottomPadding() + 12.dp,
                                ),
                        ) {
                            AboutSectionCard(
                                title = "项目",
                                enableBlur = enableBlur,
                                backdrop = backdrop,
                                blendColors = blendColors,
                            ) {
                                SettingItem(
                                    imageVector = null,
                                    title = "开源代码",
                                    onClick = { mainVm.openUrl(REPOSITORY_URL) },
                                )
                                if (META.isGkdChannel) {
                                    SettingItem(
                                        imageVector = null,
                                        title = "捐赠支持",
                                        onClick = { mainVm.navigateWebPage(ShortUrlSet.URL10) },
                                    )
                                }
                                SettingItem(
                                    imageVector = null,
                                    title = "使用协议",
                                    onClick = { mainVm.navigateWebPage(ShortUrlSet.URL12) },
                                )
                                SettingItem(
                                    imageVector = null,
                                    title = "隐私政策",
                                    onClick = { mainVm.navigateWebPage(ShortUrlSet.URL11) },
                                )
                            }

                            AboutSectionCard(
                                title = "反馈",
                                enableBlur = enableBlur,
                                backdrop = backdrop,
                                blendColors = blendColors,
                            ) {
                                SettingItem(
                                    imageVector = null,
                                    title = "问题反馈",
                                    onClick = throttle(mainVm.viewModelScope.launchAsFn {
                                        mainVm.dialogFlow.waitResult(
                                            title = "反馈须知",
                                            textContent = {
                                                Text(text = buildAnnotatedString {
                                                    val highlightStyle = SpanStyle(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MiuixTheme.colorScheme.primary,
                                                    )
                                                    append("感谢您愿意花时间反馈，")
                                                    withStyle(style = highlightStyle) {
                                                        append("GKD 默认不携带任何规则，只接受应用本体功能相关的反馈")
                                                    }
                                                    append("\n\n")
                                                    append("请先判断是不是第三方规则订阅的问题，如果是，您应该向规则提供者反馈，而不是在此处反馈。")
                                                    withStyle(style = highlightStyle) {
                                                        append("如果您已经确信是 GKD 应用本体的问题")
                                                    }
                                                    append("，可点击下方继续反馈")
                                                })
                                            },
                                            confirmText = "继续",
                                            dismissRequest = true,
                                        )
                                        mainVm.openUrl(ISSUES_URL)
                                    }),
                                )
                                SettingItem(
                                    title = "导出日志",
                                    imageVector = PerfIcon.Share,
                                    onClick = {
                                        mainVm.showShareLogDlgFlow.value = true
                                    },
                                )
                            }

                            if (mainVm.updateStatus != null) {
                                AboutSectionCard(
                                    title = "更新",
                                    enableBlur = enableBlur,
                                    backdrop = backdrop,
                                    blendColors = blendColors,
                                ) {
                                    TextMenu(
                                        title = "更新渠道",
                                        option = UpdateChannelOption.objects.findOption(store.updateChannel),
                                    ) {
                                        if (mainVm.updateStatus.checkUpdatingFlow.value) return@TextMenu
                                        if (it.value == UpdateChannelOption.Beta.value) {
                                            mainVm.viewModelScope.launchTry {
                                                mainVm.dialogFlow.waitResult(
                                                    title = "版本渠道",
                                                    text = "测试版本渠道更新快\n但不稳定可能存在较多BUG\n请谨慎使用",
                                                )
                                                storeFlow.update { s -> s.copy(updateChannel = it.value) }
                                            }
                                        } else {
                                            storeFlow.update { s -> s.copy(updateChannel = it.value) }
                                        }
                                    }
                                    BasicComponent(
                                        title = "检查更新",
                                        onClick = throttle {
                                            mainVm.updateStatus.checkUpdate(true)
                                        },
                                        endActions = {
                                            RotatingLoadingIcon(
                                                loading = mainVm.updateStatus.checkUpdatingFlow
                                                    .collectAsState().value,
                                            )
                                        },
                                    )
                                }
                            }

                            Spacer(
                                Modifier.height(
                                    WindowInsets.navigationBars.asPaddingValues()
                                        .calculateBottomPadding() +
                                        WindowInsets.captionBar.asPaddingValues()
                                            .calculateBottomPadding() + 24.dp,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    if (showShareAppDlg) {
        TextListDialog(
            onDismiss = { showShareAppDlg = false },
            textList = listOf(
                "分享到其他应用" to mainVm.viewModelScope.launchAsFn(Dispatchers.IO) {
                    if (!META.isGkdChannel) {
                        mainVm.dialogFlow.waitResult(
                            title = "分享提示",
                            textContent = { Text(text = exportPlayTipTemplate()) },
                            confirmText = "继续",
                        )
                    }
                    context.shareFile(getShareApkFile(), "分享安装文件")
                },
                "保存到下载" to mainVm.viewModelScope.launchAsFn(Dispatchers.IO) {
                    if (!META.isGkdChannel) {
                        mainVm.dialogFlow.waitResult(
                            title = "保存提示",
                            textContent = { Text(text = exportPlayTipTemplate()) },
                            confirmText = "继续",
                        )
                    }
                    context.saveFileToDownloads(getShareApkFile())
                },
                "Google Play" to {
                    mainVm.openUrl(PLAY_STORE_URL)
                },
            ),
        )
    }
}

@Composable
private fun AboutSectionCard(
    title: String,
    enableBlur: Boolean,
    backdrop: LayerBackdrop,
    blendColors: List<BlendColorEntry>,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.padding(bottom = 8.dp)) {
        SmallTitle(text = title)
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .then(
                    if (enableBlur) {
                        Modifier.textureBlur(
                            backdrop = backdrop,
                            shape = RoundedCornerShape(16.dp),
                            blurRadius = 60f,
                            colors = BlurColors(blendColors = blendColors),
                            enabled = true,
                        )
                    } else {
                        Modifier
                    },
                ),
            colors = CardDefaults.defaultColors(
                color = if (enableBlur) Color.Transparent else MiuixTheme.colorScheme.surfaceContainer,
            ),
            content = content,
        )
    }
}

@Composable
private fun exportPlayTipTemplate(): AnnotatedString {
    return buildAnnotatedString {
        append("当前导出的 APK 文件只能在已安装 Google 框架的设备上才能使用，否则安装打开后会提示报错，")
        withLink(
            LinkAnnotation.Url(
                ShortUrlSet.URL13,
                TextLinkStyles(
                    style = SpanStyle(
                        fontWeight = FontWeight.Bold,
                        color = MiuixTheme.colorScheme.primary,
                    ),
                ),
            ),
        ) {
            append("建议点此从官网下载")
        }
        append("，或点击下方继续操作")
    }
}
