package li.songe.gkd.ui.home

import androidx.compose.foundation.shape.RoundedCornerShape
import android.view.KeyEvent
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import li.songe.gkd.ui.component.PerfAlertDialog
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import li.songe.gkd.MainActivity
import li.songe.gkd.R
import li.songe.gkd.notif.ActionTipNotif
import li.songe.gkd.permission.canDrawOverlaysState
import li.songe.gkd.permission.foregroundServiceSpecialUseState
import li.songe.gkd.permission.ignoreBatteryOptimizationsState
import li.songe.gkd.permission.notificationState
import li.songe.gkd.permission.requiredPermission
import li.songe.gkd.service.StatusService
import li.songe.gkd.service.TrackService
import li.songe.gkd.service.fixRestartAutomatorService
import li.songe.gkd.shizuku.shizukuContextFlow
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.AboutRoute
import li.songe.gkd.ui.AdvancedPageRoute
import li.songe.gkd.ui.BlockA11yAppListRoute
import li.songe.gkd.ui.DesignRoute
import li.songe.gkd.ui.component.FullscreenDialog
import li.songe.gkd.ui.component.PerfCustomIconButton
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.PerfTopAppBar
import li.songe.gkd.ui.component.PreferenceGroup
import li.songe.gkd.ui.component.SettingItem
import li.songe.gkd.ui.component.TextListDialog
import li.songe.gkd.ui.component.TextMenu
import li.songe.gkd.ui.component.TextSwitch
import li.songe.gkd.ui.component.autoFocus
import li.songe.gkd.ui.component.updateDialogOptions
import li.songe.gkd.ui.component.useScrollBehaviorState
import li.songe.gkd.ui.component.waitResult
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.share.asMutableState
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.ui.style.iconTextSize
import li.songe.gkd.ui.style.itemHorizontalPadding
import li.songe.gkd.ui.style.lineHeightDp
import li.songe.gkd.util.ActionTipLiveDurationOption
import li.songe.gkd.util.ActionTipStyleOption
import li.songe.gkd.util.AndroidTarget
import li.songe.gkd.util.BackupUtils
import li.songe.gkd.util.DarkThemeOption
import li.songe.gkd.util.findOption
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.mapState
import li.songe.gkd.util.openAppDetailsSettings
import li.songe.gkd.util.saveFileToDownloads
import li.songe.gkd.util.shareFile
import li.songe.gkd.util.showActionTip
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toast
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun useSettingsPage(): ScaffoldExt {
    val mainVm = LocalMainViewModel.current
    val context = LocalActivity.current as MainActivity
    val store by storeFlow.collectAsState()
    val vm = viewModel<HomeVm>()

    var showToastInputDlg by vm.showToastInputDlgFlow.asMutableState()

    if (showToastInputDlg) {
        var value by remember {
            mutableStateOf(store.actionToast)
        }
        val maxCharLen = 64
        PerfAlertDialog(
            properties = DialogProperties(dismissOnClickOutside = false),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "触发提示")
                    PerfIconButton(
                        imageVector = PerfIcon.HelpOutline,
                        contentDescription = "文案规则",
                        onClickLabel = "打开文案规则弹窗",
                        onClick = throttle {
                            showToastInputDlg = false
                            val confirmAction = {
                                mainVm.dialogFlow.value = null
                                showToastInputDlg = true
                            }
                            mainVm.dialogFlow.updateDialogOptions(
                                title = "文案规则",
                                text = $$"触发文案支持变量替换，规则如下\n${1} 子规则名称\n${2} 规则名称\n${3} 触发次数\n\n示例模板\n${1}/${2}/${3}\n\n替换结果\n子规则a/规则A/3",
                                confirmAction = confirmAction,
                                onDismissRequest = confirmAction,
                            )
                        },
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = value,
                        onValueChange = {
                            value = it.take(maxCharLen)
                        },
                        label = "请输入提示内容",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .autoFocus(),
                    )
                    Text(
                        text = "${value.length} / $maxCharLen",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                }
            },
            onDismissRequest = { showToastInputDlg = false },
            confirmButton = {
                TextButton(
                    text = "确认",
                    enabled = value.isNotEmpty(),
                    onClick = {
                        if (value != storeFlow.value.actionToast) {
                            storeFlow.update { it.copy(actionToast = value) }
                            toast("更新成功")
                        }
                        showToastInputDlg = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            },
            dismissButton = {
                TextButton(
                    text = "取消",
                    onClick = { showToastInputDlg = false },
                    modifier = Modifier.weight(1f),
                )
            }
        )
    }

    var showLiveDurationDlg by remember { mutableStateOf(false) }
    if (showLiveDurationDlg) {
        var value by remember {
            mutableStateOf(store.resolveActionTipLiveDurationSec().toString())
        }
        val parsedSec = value.toIntOrNull()
        val validSec = parsedSec != null &&
            parsedSec in ActionTipNotif.MIN_DURATION_SEC..ActionTipNotif.MAX_DURATION_SEC
        PerfAlertDialog(
            properties = DialogProperties(dismissOnClickOutside = false),
            title = { Text(text = "实时通知存在时间") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TextField(
                        value = value,
                        onValueChange = { newValue ->
                            value = newValue.filter(Char::isDigit).take(3)
                        },
                        label = "秒数（${ActionTipNotif.MIN_DURATION_SEC}-${ActionTipNotif.MAX_DURATION_SEC}）",
                        useLabelAsPlaceholder = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .autoFocus(),
                    )
                    Text(
                        text = "到期后自动取消通知；常用 3 / 5 / 8 / 15 / 30 / 60",
                        modifier = Modifier.padding(top = 8.dp),
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                }
            },
            onDismissRequest = { showLiveDurationDlg = false },
            confirmButton = {
                TextButton(
                    text = "确认",
                    enabled = validSec,
                    onClick = {
                        if (parsedSec != null && parsedSec != store.resolveActionTipLiveDurationSec()) {
                            storeFlow.update {
                                it.copy(actionTipLiveDurationSec = parsedSec)
                            }
                            toast("已设置为 ${parsedSec} 秒")
                        }
                        showLiveDurationDlg = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            },
            dismissButton = {
                TextButton(
                    text = "取消",
                    onClick = { showLiveDurationDlg = false },
                    modifier = Modifier.weight(1f),
                )
            },
        )
    }

    var showNotifTextInputDlg by vm.showNotifTextInputDlgFlow.asMutableState()
    if (showNotifTextInputDlg) {
        var titleValue by remember { mutableStateOf(store.customNotifTitle) }
        var textValue by remember { mutableStateOf(store.customNotifText) }
        PerfAlertDialog(
            properties = DialogProperties(dismissOnClickOutside = false),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = "通知文案")
                    PerfIconButton(
                        imageVector = PerfIcon.HelpOutline,
                        contentDescription = "文案规则",
                        onClickLabel = "打开文案规则弹窗",
                        onClick = throttle {
                            showNotifTextInputDlg = false
                            val confirmAction = {
                                mainVm.dialogFlow.value = null
                                showNotifTextInputDlg = true
                            }
                            mainVm.dialogFlow.updateDialogOptions(
                                title = "文案规则",
                                text = $$"通知文案支持变量替换，规则如下\n${i} 全局规则数\n${k} 应用数\n${u} 应用规则数\n${n} 触发次数\n\n示例模板\n${i}全局/${k}应用/${u}规则/${n}触发\n\n替换结果\n0全局/1应用/2规则/3触发",
                                confirmAction = confirmAction,
                                onDismissRequest = confirmAction,
                            )
                        },
                    )
                }
            },
            text = {
                val titleMaxLen = 32
                val textMaxLen = 64
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextField(
                        value = titleValue,
                        onValueChange = {
                            titleValue = (if (it.length > titleMaxLen) it.take(titleMaxLen) else it)
                                .filter { c -> c !in "\n\r" }
                        },
                        label = "主标题",
                        useLabelAsPlaceholder = true,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = "${titleValue.length} / $titleMaxLen",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                    TextField(
                        value = textValue,
                        onValueChange = {
                            textValue = if (it.length > textMaxLen) it.take(textMaxLen) else it
                        },
                        label = "副标题",
                        useLabelAsPlaceholder = true,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .autoFocus(),
                    )
                    Text(
                        text = "${textValue.length} / $textMaxLen",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                    )
                }
            },
            onDismissRequest = {
                showNotifTextInputDlg = false
            },
            confirmButton = {
                TextButton(
                    text = "确认",
                    onClick = {
                        context.justHideSoftInput()
                        if (store.customNotifTitle != textValue || store.customNotifText != textValue) {
                            storeFlow.update {
                                it.copy(
                                    customNotifTitle = titleValue,
                                    customNotifText = textValue
                                )
                            }
                            toast("更新成功")
                        }
                        showNotifTextInputDlg = false
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            },
            dismissButton = {
                TextButton(
                    text = "取消",
                    onClick = { showNotifTextInputDlg = false },
                    modifier = Modifier.weight(1f),
                )
            })
    }


    var showA11yBlockDlg by vm.showA11yBlockDlgFlow.asMutableState()
    if (showA11yBlockDlg) {
        BlockA11yDialog(onDismissRequest = { showA11yBlockDlg = false })
    }
    if (vm.showBackupDlgFlow.collectAsState().value) {
        TextListDialog(
            onDismiss = { vm.showBackupDlgFlow.value = false },
            textList = listOf(
                "导入备份" to vm.viewModelScope.launchAsFn(Dispatchers.IO) {
                    val uri = context.pickFile("application/zip")
                    if (uri != null) {
                        BackupUtils.importBackUpData(uri)
                    }
                },
                "导出备份" to {
                    vm.showExportBackupDlgFlow.value = true
                },
            )
        )
    }
    if (vm.showExportBackupDlgFlow.collectAsState().value) {
        TextListDialog(
            onDismiss = { vm.showExportBackupDlgFlow.value = false },
            textList = listOf(
                "分享到其他应用" to vm.viewModelScope.launchAsFn(Dispatchers.IO) {
                    val file = BackupUtils.exportBackUpData()
                    context.shareFile(file, "分享备份文件")
                },
                "保存到下载" to vm.viewModelScope.launchAsFn(Dispatchers.IO) {
                    val file = BackupUtils.exportBackUpData()
                    context.saveFileToDownloads(file)
                },
            )
        )
    }

    val scrollKey = rememberSaveable { mutableIntStateOf(0) }
    val scrollState = useScrollBehaviorState(scrollKey)
    val miuixScrollBehavior = MiuixScrollBehavior()
    LaunchedEffect(null) {
        mainVm.resetPageScrollEvent.collect {
            if (it == BottomNavItem.Settings) {
                scrollKey.intValue++
            }
        }
    }
    return ScaffoldExt(
        navItem = BottomNavItem.Settings,
        modifier = Modifier.nestedScroll(miuixScrollBehavior.nestedScrollConnection),
        topBar = {
            PerfTopAppBar(
                titleText = BottomNavItem.Settings.label,
                miuixScrollBehavior = miuixScrollBehavior,
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(contentPadding)
        ) {

            PreferenceGroup(title = "常规", showTop = false) {
                val showToastSettingsDlg by vm.showToastSettingsDlgFlow.asMutableState()
                TextSwitch(
                    title = "触发提示",
                    subtitle = store.actionToast,
                    checked = store.toastWhenClick,
                    onClickLabel = "打开触发提示弹窗",
                    onClick = {
                        showToastInputDlg = true
                    },
                    suffixIcon = {
                        PerfCustomIconButton(
                            size = 32.dp,
                            iconSize = 20.dp,
                            onClickLabel = "打开提示设置弹窗",
                            onClick = { vm.showToastSettingsDlgFlow.update { !it } },
                            id = R.drawable.ic_page_info,
                            contentDescription = "提示设置",
                            tint = if (showToastSettingsDlg) MiuixTheme.colorScheme.primary else LocalContentColor.current,
                        )
                    },
                    onCheckedChange = {
                        storeFlow.value = store.copy(
                            toastWhenClick = it
                        )
                    })

                AnimatedVisibility(visible = showToastSettingsDlg) {
                    Column {
                        TextMenu(
                            title = "提示样式",
                            option = store.resolveActionTipStyle(),
                            onOptionChange = { option ->
                                val style = option as ActionTipStyleOption
                                storeFlow.update {
                                    it.copy(
                                        actionTipStyle = style.value,
                                        useSystemToast = style == ActionTipStyleOption.SystemToast,
                                    )
                                }
                            },
                        )
                        if (store.resolveActionTipStyle() == ActionTipStyleOption.LiveNotif) {
                            TextMenu(
                                title = "存在时间",
                                option = ActionTipLiveDurationOption.resolve(
                                    store.resolveActionTipLiveDurationSec(),
                                ),
                                onOptionChange = { option ->
                                    storeFlow.update {
                                        it.copy(
                                            actionTipLiveDurationSec = (option as ActionTipLiveDurationOption).value,
                                        )
                                    }
                                },
                            )
                            SettingItem(
                                title = "自定义存在时间",
                                subtitle = "当前 ${ActionTipLiveDurationOption.labelOf(store.resolveActionTipLiveDurationSec())}，可输入 ${ActionTipNotif.MIN_DURATION_SEC}-${ActionTipNotif.MAX_DURATION_SEC} 秒",
                                onClick = { showLiveDurationDlg = true },
                            )
                        }
                        SettingItem(
                            title = "样式说明",
                            subtitle = "悬浮窗 / Toast / Google Live Update 实时通知；可进入系统开关页",
                            onClick = {
                                mainVm.dialogFlow.updateDialogOptions(
                                    title = "提示样式说明",
                                    text = "• 悬浮窗：无障碍/悬浮窗绘制，兼容最好\n" +
                                        "• 系统 Toast：受系统频率限制，高触发规则可能不显示\n" +
                                        "• 实时通知：同一条通知同时适配\n" +
                                        "  - ColorOS：Google Live Update → 流体云\n" +
                                        "  - HyperOS：miui.focus 模板 → 超级岛\n" +
                                        "  请在系统通知设置中开启「实时更新/流体云」或焦点通知相关开关。",
                                )
                            },
                        )
                        SettingItem(
                            title = "实时更新系统开关",
                            subtitle = "打开系统里本应用的 Live Updates / 实时更新设置",
                            onClick = throttle {
                                if (!ActionTipNotif.openPromotedSettings()) {
                                    toast("当前系统无此设置页")
                                }
                            },
                        )
                        SettingItem(
                            title = "发送测试通知",
                            subtitle = "应用内不会上岛，请下拉通知栏查看是否存在实时通知",
                            onClick = throttle(vm.viewModelScope.launchAsFn {
                                if (store.resolveActionTipStyle() == ActionTipStyleOption.LiveNotif) {
                                    requiredPermission(context, notificationState)
                                }
                                val sample = store.actionToast
                                    .replace($$"${1}", "子规则")
                                    .replace($$"${2}", "规则组")
                                    .replace($$"${3}", "1")
                                val result = showActionTip(sample)
                                if (result != null) {
                                    val tip = if (store.resolveActionTipStyle() == ActionTipStyleOption.LiveNotif) {
                                        "已发送；应用内不会上岛，请在通知栏查看是否存在实时通知"
                                    } else {
                                        result.message
                                    }
                                    toast(tip)
                                    if (result.canPostPromoted == false) {
                                        ActionTipNotif.openPromotedSettings()
                                    }
                                } else {
                                    toast("已发送测试提示")
                                }
                            }),
                        )
                        TextSwitch(
                            title = "轨迹提示",
                            subtitle = "显示触发位置信息",
                            checked = TrackService.isRunning.collectAsState().value,
                            onCheckedChange = vm.viewModelScope.launchAsFn<Boolean> {
                                if (it) {
                                    mainVm.dialogFlow.waitResult(
                                        title = "使用须知",
                                        text = "开启「轨迹提示」后点击或滑动后会在屏幕上使用悬浮窗绘制轨迹(一段时间后消失)，如果新触摸事件恰好在悬浮窗区域内，可能会被目标应用拒绝，从而导致点击或滑动无响应",
                                        confirmText = "继续",
                                    )
                                    requiredPermission(context, foregroundServiceSpecialUseState)
                                    requiredPermission(context, notificationState)
                                    requiredPermission(context, canDrawOverlaysState)
                                    TrackService.start()
                                } else {
                                    TrackService.stop()
                                }
                            }
                        )
                    }
                }

                val subsStatus by vm.subsStatusFlow.collectAsState()
                TextSwitch(
                    title = "通知文案",
                    subtitle = if (store.useCustomNotifText) {
                        store.customNotifTitle + " / " + store.customNotifText
                    } else {
                        subsStatus
                    },
                    checked = store.useCustomNotifText,
                    onClickLabel = "打开修改通知文案弹窗",
                    onClick = { showNotifTextInputDlg = true },
                    onCheckedChange = {
                        storeFlow.value = store.copy(
                            useCustomNotifText = it
                        )
                    })

                TextSwitch(
                    title = "后台隐藏",
                    subtitle = "在「最近任务」隐藏卡片",
                    checked = store.excludeFromRecents,
                    onCheckedChange = vm.viewModelScope.launchAsFn<Boolean> {
                        if (it) {
                            mainVm.dialogFlow.waitResult(
                                title = "后台隐藏",
                                text = "隐藏卡片后可能导致部分设备无法给任务卡片加锁后台，建议先加锁后再隐藏，若已加锁或没有锁后台机制请继续",
                                confirmText = "继续",
                            )
                        }
                        storeFlow.value = store.copy(
                            excludeFromRecents = !store.excludeFromRecents
                        )
                    })
            }

            val scope = rememberCoroutineScope()
            val lazyOn = remember {
                storeFlow.mapState(scope) { it.enableBlockA11yAppList }.debounce(300)
                    .stateIn(scope, SharingStarted.Eagerly, store.enableBlockA11yAppList)
            }.collectAsState()
            PreferenceGroup(title = "无障碍") {
                TextSwitch(
                    title = "局部关闭",
                    subtitle = "白名单内关闭服务",
                    checked = store.enableBlockA11yAppList && shizukuContextFlow.collectAsState().value.ok,
                    onCheckedChange = vm.viewModelScope.launchAsFn<Boolean> {
                        if (it) {
                            showA11yBlockDlg = true
                        } else {
                            storeFlow.value = store.copy(enableBlockA11yAppList = false)
                            fixRestartAutomatorService()
                        }
                    },
                )
                AnimatedVisibility(visible = lazyOn.value) {
                    SettingItem(title = "白名单", onClickLabel = "进入无障碍白名单页面", onClick = {
                        mainVm.navigatePage(BlockA11yAppListRoute)
                    })
                }
            }

            PreferenceGroup(title = "外观") {
                SettingItem(title = "设计", subtitle = "主题 / 模糊 / 液态玻璃 / 预测式返回", onClick = {
                    mainVm.navigatePage(DesignRoute)
                })

                TextMenu(
                    title = "深色模式",
                    option = DarkThemeOption.objects.findOption(store.enableDarkTheme),
                    onOptionChange = {
                        storeFlow.update { s -> s.copy(enableDarkTheme = it.value) }
                    }
                )

                if (AndroidTarget.S) {
                    TextSwitch(
                        title = "动态配色",
                        checked = store.enableDynamicColor,
                        onCheckedChange = {
                            storeFlow.update { s -> s.copy(enableDynamicColor = it) }
                        }
                    )
                }
            }

            PreferenceGroup(title = "其他") {
                SettingItem(title = "高级设置", onClick = {
                    mainVm.navigatePage(AdvancedPageRoute)
                })
                SettingItem(title = "备份恢复", onClick = {
                    vm.showBackupDlgFlow.value = true
                })

                SettingItem(title = "关于", onClick = {
                    mainVm.navigatePage(AboutRoute)
                })
            }

            Spacer(modifier = Modifier.height(EmptyHeight))
        }
    }
}

@Composable
private fun BlockA11yDialog(onDismissRequest: () -> Unit) = FullscreenDialog(onDismissRequest) {
    val mainVm = LocalMainViewModel.current
    val statusRunning by StatusService.isRunning.collectAsState()
    val shizukuContext by shizukuContextFlow.collectAsState()
    val ignoreBatteryOptimizations by ignoreBatteryOptimizationsState.stateFlow.collectAsState()
    val context = LocalActivity.current as MainActivity
    Scaffold(
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            PerfTopAppBar(
                titleText = "局部关闭",
                navigationIcon = {
                    PerfIconButton(
                        imageVector = PerfIcon.Close,
                        onClickLabel = "关闭弹窗",
                        onClick = onDismissRequest,
                    )
                },
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = itemHorizontalPadding, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    text = "继续",
                    enabled = shizukuContext.ok && statusRunning && ignoreBatteryOptimizations,
                    onClick = mainVm.viewModelScope.launchAsFn {
                        onDismissRequest()
                        delay(200)
                        storeFlow.update { it.copy(enableBlockA11yAppList = true) }
                    },
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            }
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(horizontal = itemHorizontalPadding)
        ) {
            CompositionLocalProvider(LocalTextStyle provides MiuixTheme.textStyles.body2) {
                Text(text = "「局部关闭」可在白名单应用内关闭服务，来解决界面异常，游戏掉帧或无障碍检测的问题")
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "使用须知", style = MiuixTheme.textStyles.title3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RequiredTextItem(text = "切换服务会造成短暂触摸卡顿，请自行测试后再编辑白名单")
                    RequiredTextItem(text = "使用其它无障碍应用可能导致优化无效，可在服务关闭后自行确认")
                    RequiredTextItem(text = "必须确保服务关闭后的持续后台运行，否则会被系统暂停或结束运行导致重启失败")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "使用条件", style = MiuixTheme.textStyles.title3)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    RequiredTextItem(
                        text = "Shizuku 授权",
                        enabled = !shizukuContext.ok,
                        imageVector = if (shizukuContext.ok) PerfIcon.Check else PerfIcon.ArrowForward,
                        onClick = mainVm.viewModelScope.launchAsFn(Dispatchers.IO) {
                            mainVm.guardShizukuContext()
                        },
                    )
                    RequiredTextItem(
                        text = "开启「常驻通知」",
                        enabled = !statusRunning,
                        imageVector = if (statusRunning) PerfIcon.Check else PerfIcon.ArrowForward,
                        onClick = mainVm.viewModelScope.launchAsFn {
                            StatusService.requestStart(context)
                        },
                    )
                    RequiredTextItem(
                        text = "省电策略设置为无限制",
                        enabled = !ignoreBatteryOptimizations,
                        imageVector = if (ignoreBatteryOptimizations) PerfIcon.Check else PerfIcon.ArrowForward,
                        onClickLabel = "打开忽略电池优化设置页面",
                        onClick = mainVm.viewModelScope.launchAsFn {
                            requiredPermission(context, ignoreBatteryOptimizationsState)
                        },
                    )
                    RequiredTextItem(
                        text = "(可选) 允许自启动",
                        enabled = true,
                        imageVector = PerfIcon.OpenInNew,
                        onClickLabel = "打开应用详情页面",
                        onClick = {
                            openAppDetailsSettings()
                        },
                    )
                    RequiredTextItem(
                        text = "(可选) 在「最近任务」锁定",
                        enabled = true,
                        imageVector = PerfIcon.OpenInNew,
                        onClickLabel = "打开应用详情页面",
                        onClick = {
                            val m = shizukuContextFlow.value.inputManager
                            if (m != null) {
                                m.key(KeyEvent.KEYCODE_APP_SWITCH)
                            } else {
                                toast("请先授权 Shizuku")
                            }
                        },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "某些场景下服务刚启动时概率不工作，如多次遇到此情况则不建议使用此功能")
            }
            Spacer(modifier = Modifier.height(EmptyHeight))
        }
    }
}

@Composable
private fun RequiredTextItem(
    text: String,
    imageVector: ImageVector? = null,
    enabled: Boolean = false,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .run {
                if (onClick != null) {
                    clickable(
                        enabled = enabled,
                        onClick = throttle(onClick),
                        onClickLabel = onClickLabel
                    )
                } else {
                    this
                }
            }
            .padding(horizontal = 4.dp),
    ) {
        val lineHeightDp = LocalDensity.current.let { LocalTextStyle.current.lineHeightDp(it) }
        Spacer(
            modifier = Modifier
                .padding(vertical = (lineHeightDp - 4.dp) / 2)
                .clip(CircleShape)
                .background(MiuixTheme.colorScheme.secondary)
                .size(4.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text)
        if (imageVector != null) {
            PerfIcon(
                imageVector = imageVector,
                modifier = Modifier.iconTextSize(),
            )
        }
    }

}
