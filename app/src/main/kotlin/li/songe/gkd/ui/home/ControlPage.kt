package li.songe.gkd.ui.home

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import top.yukonga.miuix.kmp.basic.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import li.songe.gkd.MainActivity
import li.songe.gkd.R
import li.songe.gkd.data.SubsConfig
import li.songe.gkd.permission.appOpsRestrictedFlow
import li.songe.gkd.permission.writeSecureSettingsState
import li.songe.gkd.service.A11yService
import li.songe.gkd.service.ActivityService
import li.songe.gkd.service.StatusService
import li.songe.gkd.service.a11yPartDisabledFlow
import li.songe.gkd.service.switchAutomatorService
import li.songe.gkd.service.topAppIdFlow
import li.songe.gkd.shizuku.shizukuContextFlow
import li.songe.gkd.shizuku.uiAutomationFlow
import li.songe.gkd.store.actualA11yScopeAppList
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.ActionLogRoute
import li.songe.gkd.ui.ActivityLogRoute
import li.songe.gkd.ui.AppConfigRoute
import li.songe.gkd.ui.AuthA11yRoute
import li.songe.gkd.ui.WebViewRoute
import li.songe.gkd.ui.component.GroupNameText
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.PerfTopAppBar
import li.songe.gkd.ui.component.PreferenceGroup
import li.songe.gkd.ui.component.SettingItem
import li.songe.gkd.ui.component.TextSwitch
import li.songe.gkd.ui.component.textSize
import li.songe.gkd.ui.component.useScrollBehaviorState
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.util.HOME_PAGE_URL
import li.songe.gkd.util.ShortUrlSet
import li.songe.gkd.util.latestRecordDescFlow
import li.songe.gkd.util.latestRecordFlow
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun useControlPage(): ScaffoldExt {
    val context = LocalActivity.current as MainActivity
    val mainVm = LocalMainViewModel.current
    val vm = viewModel<HomeVm>()
    val scrollKey = rememberSaveable { mutableIntStateOf(0) }
    val scrollState = useScrollBehaviorState(scrollKey)
    val miuixScrollBehavior = MiuixScrollBehavior()
    val appTitle = stringResource(R.string.app_name)
    LaunchedEffect(null) {
        mainVm.resetPageScrollEvent.collect {
            if (it == BottomNavItem.Control) {
                scrollKey.intValue++
            }
        }
    }
    return ScaffoldExt(
        navItem = BottomNavItem.Control,
        modifier = Modifier.nestedScroll(miuixScrollBehavior.nestedScrollConnection),
        topBar = {
            PerfTopAppBar(
                titleText = appTitle,
                miuixScrollBehavior = miuixScrollBehavior,
                actions = {
                    PerfIconButton(
                        imageVector = PerfIcon.RocketLaunch,
                        onClickLabel = "前往工作模式页面",
                        contentDescription = "工作模式",
                        onClick = throttle {
                            mainVm.navigatePage(AuthA11yRoute)
                        },
                    )
                },
            )
        },
    ) { contentPadding ->
        val store by storeFlow.collectAsState()

        val a11yRunning by A11yService.isRunning.collectAsState()
        val manageRunning by StatusService.isRunning.collectAsState()
        val writeSecureSettings by writeSecureSettingsState.stateFlow.collectAsState()
        val appOpsRestricted by appOpsRestrictedFlow.collectAsState()

        Column(
            modifier = Modifier
                .verticalScroll(scrollState)
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (appOpsRestricted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .semantics(mergeDescendants = true) {
                            this.onClick(label = "前往解除限制页面", action = null)
                        },
                    colors = CardDefaults.defaultColors(
                        color = MiuixTheme.colorScheme.errorContainer,
                        contentColor = MiuixTheme.colorScheme.onErrorContainer,
                    ),
                    onClick = throttle {
                        mainVm.navigateWebPage(ShortUrlSet.URL2)
                    },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PerfIcon(
                            imageVector = PerfIcon.WarningAmber,
                            tint = MiuixTheme.colorScheme.onErrorContainer,
                        )
                        Text(
                            modifier = Modifier.weight(1f),
                            text = "检测到权限受限制，请前往解除",
                            style = MiuixTheme.textStyles.body1,
                            color = MiuixTheme.colorScheme.onErrorContainer,
                        )
                        PerfIcon(
                            imageVector = PerfIcon.KeyboardArrowRight,
                            tint = MiuixTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            PreferenceGroup(title = "服务", showTop = appOpsRestricted) {
                if (store.useA11y || actualA11yScopeAppList.contains(topAppIdFlow.collectAsState().value)) {
                    TextSwitch(
                        title = "服务状态",
                        subtitle = if (a11yRunning) {
                            "无障碍正在运行"
                        } else if (mainVm.a11yServiceEnabledFlow.collectAsState().value) {
                            "无障碍发生故障"
                        } else if (writeSecureSettings) {
                            if (store.enableAutomator && a11yPartDisabledFlow.collectAsState().value) {
                                "无障碍局部关闭"
                            } else {
                                "无障碍已关闭"
                            }
                        } else {
                            "无障碍未授权"
                        },
                        checked = a11yRunning,
                        onCheckedChange = { newEnabled ->
                            if (newEnabled && !writeSecureSettingsState.value) {
                                mainVm.navigatePage(AuthA11yRoute)
                            } else {
                                switchAutomatorService()
                            }
                        },
                    )
                } else {
                    TextSwitch(
                        title = "服务状态",
                        subtitle = if (uiAutomationFlow.collectAsState().value != null) {
                            "自动化正在运行"
                        } else if (!shizukuContextFlow.collectAsState().value.ok) {
                            "自动化未授权"
                        } else {
                            if (store.enableAutomator && a11yPartDisabledFlow.collectAsState().value) {
                                "自动化局部关闭"
                            } else {
                                "自动化已关闭"
                            }
                        },
                        checked = uiAutomationFlow.collectAsState().value != null,
                        onCheckedChange = vm.viewModelScope.launchAsFn(Dispatchers.IO) { newEnabled ->
                            if (newEnabled) {
                                mainVm.guardShizukuContext()
                            }
                            switchAutomatorService()
                        },
                    )
                }

                TextSwitch(
                    title = "常驻通知",
                    subtitle = "显示运行状态及统计数据",
                    checked = manageRunning && store.enableStatusService,
                    onCheckedChange = vm.viewModelScope.launchAsFn<Boolean> {
                        if (it) {
                            StatusService.requestStart(context)
                        } else {
                            StatusService.stop()
                            storeFlow.value = store.copy(
                                enableStatusService = false
                            )
                        }
                    },
                )
            }

            ServerStatusSection()

            PreferenceGroup(title = "快捷入口") {
                SettingItem(
                    title = "触发记录",
                    subtitle = "规则误触可定位关闭",
                    onClickLabel = "打开触发记录页面",
                    onClick = {
                        mainVm.navigatePage(ActionLogRoute())
                    },
                )

                if (ActivityService.isRunning.collectAsState().value) {
                    SettingItem(
                        title = "界面日志",
                        subtitle = "记录打开的应用及界面",
                        onClickLabel = "打开界面日志页面",
                        onClick = {
                            mainVm.navigatePage(ActivityLogRoute)
                        },
                    )
                }

                SettingItem(
                    title = "了解 GKD",
                    subtitle = "查阅规则文档和常见问题",
                    onClickLabel = "打开规则文档页面",
                    onClick = {
                        mainVm.navigatePage(WebViewRoute(initUrl = HOME_PAGE_URL))
                    },
                )
            }

            Spacer(modifier = Modifier.height(EmptyHeight))
        }
    }
}

@Composable
private fun ServerStatusSection() {
    val mainVm = LocalMainViewModel.current
    val vm = viewModel<HomeVm>()
    val usedSubsItemCount by vm.usedSubsItemCountFlow.collectAsState()
    val subsStatus by vm.subsStatusFlow.collectAsState()
    val latestRecordDesc by latestRecordDescFlow.collectAsState()
    val latestRecord by latestRecordFlow.collectAsState()

    PreferenceGroup(title = "数据概览") {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AnimatedVisibility(usedSubsItemCount > 0) {
                Text(
                    text = "已开启 $usedSubsItemCount 条订阅",
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            AnimatedVisibility(subsStatus.isNotEmpty()) {
                Text(
                    text = subsStatus,
                    style = MiuixTheme.textStyles.body2,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                )
            }
            if (latestRecordDesc != null) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClickLabel = "前往应用的规则汇总页面", onClick = throttle {
                            latestRecord?.let {
                                mainVm.navigatePage(
                                    AppConfigRoute(
                                        appId = it.appId,
                                        focusLog = it,
                                    )
                                )
                            }
                        })
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    GroupNameText(
                        modifier = Modifier.weight(1f),
                        preText = "最近触发: ",
                        isGlobal = latestRecord?.groupType == SubsConfig.GlobalGroupType,
                        text = latestRecordDesc ?: "",
                        style = MiuixTheme.textStyles.body2,
                        color = MiuixTheme.colorScheme.primary,
                    )
                    PerfIcon(
                        imageVector = PerfIcon.KeyboardArrowRight,
                        modifier = Modifier.textSize(style = MiuixTheme.textStyles.body2),
                        tint = MiuixTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}
