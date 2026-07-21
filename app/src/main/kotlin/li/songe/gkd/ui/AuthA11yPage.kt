package li.songe.gkd.ui

import android.Manifest
import android.app.AppOpsManagerHidden
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import li.songe.gkd.META
import li.songe.gkd.permission.Manifest_permission_GET_APP_OPS_STATS
import li.songe.gkd.permission.writeSecureSettingsState
import li.songe.gkd.service.A11yService
import li.songe.gkd.service.fixRestartAutomatorService
import li.songe.gkd.shizuku.SafeAppOpsService
import li.songe.gkd.shizuku.shizukuUsedFlow
import li.songe.gkd.store.updateEnableAutomator
import li.songe.gkd.ui.component.AnimatedBooleanContent
import li.songe.gkd.ui.component.AppPageScaffold
import li.songe.gkd.ui.component.ManualAuthDialog
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.updateDialogOptions
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.ui.style.cardHorizontalPadding
import li.songe.gkd.ui.style.lineHeightDp
import li.songe.gkd.util.AndroidTarget
import li.songe.gkd.util.AutomatorModeOption
import li.songe.gkd.util.ShortUrlSet
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.openA11ySettings
import li.songe.gkd.util.shFolder
import li.songe.gkd.util.throttle
import li.songe.gkd.util.toast
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Serializable
data object AuthA11yRoute : NavKey

@Composable
fun AuthA11yPage() {
    val mainVm = LocalMainViewModel.current
    val vm = viewModel<AuthA11yVm>()
    val showCopyDlg by vm.showCopyDlgFlow.collectAsState()
    val writeSecureSettings by writeSecureSettingsState.stateFlow.collectAsState()
    val a11yRunning by A11yService.isRunning.collectAsState()
    val automatorMode by mainVm.automatorModeFlow.collectAsState()
    AppPageScaffold(
        title = "工作模式",
        navigationIcon = {
            PerfIconButton(
                imageVector = PerfIcon.ArrowBack,
                onClick = {
                    mainVm.popPage()
                })
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = cardHorizontalPadding)
                    .fillMaxWidth(),
                onClick = throttle { mainVm.updateAutomatorMode(AutomatorModeOption.A11yMode) },
                colors = CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = automatorMode == AutomatorModeOption.A11yMode,
                        onClick = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = AutomatorModeOption.A11yMode.label,
                        style = MiuixTheme.textStyles.title3,
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(horizontal = cardHorizontalPadding)
                        .padding(start = 4.dp),
                    text = "基础",
                    style = MiuixTheme.textStyles.subtitle,
                )
                TextListItem(
                    modifier = Modifier
                        .padding(horizontal = cardHorizontalPadding)
                        .padding(start = 8.dp, top = 4.dp),
                    style = MiuixTheme.textStyles.body1,
                    list = listOf(
                        "授予「无障碍权限」",
                        "无障碍关闭后需重新授权"
                    ),
                )
                AnimatedBooleanContent(
                    targetState = writeSecureSettings || a11yRunning,
                    contentTrue = {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = cardHorizontalPadding)
                                .padding(start = 8.dp, top = 4.dp),
                            text = "已持有「无障碍权限」可继续使用",
                            style = MiuixTheme.textStyles.body2,
                        )
                    },
                    contentFalse = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = cardHorizontalPadding)
                                .padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                text = "手动授权",
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                                onClick = throttle { openA11ySettings() },
                            )
                            Text(
                                modifier = Modifier
                                    .align(Alignment.End)
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable(onClick = throttle {
                                        mainVm.navigateWebPage(ShortUrlSet.URL2)
                                    })
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                text = "无法开启无障碍?",
                                style = MiuixTheme.textStyles.body2,
                                color = MiuixTheme.colorScheme.primary,
                            )
                        }
                    }
                )
                Text(
                    modifier = Modifier
                        .padding(horizontal = cardHorizontalPadding)
                        .padding(start = 4.dp, top = 8.dp),
                    text = "增强",
                    style = MiuixTheme.textStyles.subtitle,
                )
                TextListItem(
                    modifier = Modifier
                        .padding(horizontal = cardHorizontalPadding)
                        .padding(start = 8.dp, top = 4.dp),
                    style = MiuixTheme.textStyles.body1,
                    list = listOf(
                        "授予「写入安全设置权限」",
                        "应用可自行控制开关无障碍",
                    ),
                )
                AnimatedBooleanContent(
                    targetState = writeSecureSettings,
                    contentTrue = {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = cardHorizontalPadding)
                                .padding(start = 8.dp, top = 4.dp),
                            text = "已持有「写入安全设置权限」 优先使用此项",
                            style = MiuixTheme.textStyles.body2,
                        )
                    },
                    contentFalse = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = cardHorizontalPadding)
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            ShizukuAuthButton(modifier = Modifier.weight(1f))
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = "命令授权",
                                onClick = { vm.showCopyDlgFlow.value = true },
                            )
                        }
                    }
                )
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = cardHorizontalPadding)
                        .padding(top = 8.dp),
                    text = "无感保活",
                    onClick = throttle {
                        if (!writeSecureSettings) {
                            toast("请先授予「${writeSecureSettingsState.name}」")
                        }
                        mainVm.dialogFlow.updateDialogOptions(
                            title = "无感保活",
                            text = "添加通知栏快捷开关\n\n1. 下拉通知栏至「快捷开关」标界面\n2. 找到名称为 ${META.appName} 的快捷开关\n3. 添加此开关到通知面板 \n\n只要此快捷开关在通知面板可见\n无论是系统杀后台还是自身崩溃\n简单下拉打开通知即可重启"
                        )
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .padding(horizontal = cardHorizontalPadding)
                    .fillMaxWidth(),
                onClick = throttle { mainVm.updateAutomatorMode(AutomatorModeOption.AutomationMode) },
                colors = CardDefaults.defaultColors(
                    color = MiuixTheme.colorScheme.surfaceContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = automatorMode == AutomatorModeOption.AutomationMode,
                        onClick = null,
                    )
                    Text(
                        modifier = Modifier.padding(start = 12.dp),
                        text = AutomatorModeOption.AutomationMode.label,
                        style = MiuixTheme.textStyles.title3,
                    )
                }
                TextListItem(
                    modifier = Modifier
                        .padding(horizontal = cardHorizontalPadding)
                        .padding(start = 8.dp),
                    style = MiuixTheme.textStyles.body1,
                    list = listOf(
                        "自动化驱动的无障碍",
                        "不会导致界面显示异常",
                        "不会被应用检测为无障碍",
                        "若不兼容可配置「局部无障碍」",
                    ),
                )
                AnimatedBooleanContent(
                    targetState = shizukuUsedFlow.collectAsState().value,
                    contentTrue = {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = cardHorizontalPadding)
                                .padding(start = 8.dp, top = 8.dp),
                            text = "已连接 Shizuku 服务，可继续使用",
                            style = MiuixTheme.textStyles.body2,
                        )
                    },
                    contentFalse = {
                        ShizukuAuthButton(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = cardHorizontalPadding)
                                .padding(top = 8.dp),
                        )
                    }
                )
                TextButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = cardHorizontalPadding)
                        .padding(top = 8.dp),
                    text = "局部无障碍",
                    onClick = throttle {
                        mainVm.navigatePage(A11YScopeAppListRoute)
                    },
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            Spacer(modifier = Modifier.height(EmptyHeight))
        }
    }

    ManualAuthDialog(
        commandText = gkdStartCommandText,
        show = showCopyDlg,
        onUpdateShow = {
            vm.showCopyDlgFlow.value = it
        },
    )
}

@Composable
private fun ShizukuAuthButton(
    modifier: Modifier = Modifier,
) {
    val mainVm = LocalMainViewModel.current
    val vm = viewModel<AuthA11yVm>()
    TextButton(
        modifier = modifier,
        text = "Shizuku 授权",
        onClick = throttle(vm.viewModelScope.launchAsFn(Dispatchers.IO) {
            mainVm.guardShizukuContext()
            if (writeSecureSettingsState.value) {
                toast("授权成功")
                updateEnableAutomator(true)
                fixRestartAutomatorService()
            }
        }),
    )
}

private val Int.appopsAllow get() = "appops set ${META.appId} ${AppOpsManagerHidden.opToName(this)} allow"
private val String.pmGrant get() = "pm grant ${META.appId} $this"

val gkdStartCommandText by lazy {
    val commandText = listOfNotNull(
        "set -euo pipefail",
        "echo '> start start.sh'",
        Manifest.permission.WRITE_SECURE_SETTINGS.pmGrant,
        Manifest_permission_GET_APP_OPS_STATS.pmGrant,
        if (AndroidTarget.TIRAMISU) Manifest.permission.POST_NOTIFICATIONS.pmGrant else null,
        AppOpsManagerHidden.OP_POST_NOTIFICATION.appopsAllow,
        AppOpsManagerHidden.OP_SYSTEM_ALERT_WINDOW.appopsAllow,
        if (AndroidTarget.Q) AppOpsManagerHidden.OP_ACCESS_ACCESSIBILITY.appopsAllow else null,
        if (AndroidTarget.TIRAMISU) AppOpsManagerHidden.OP_ACCESS_RESTRICTED_SETTINGS.appopsAllow else null,
        if (AndroidTarget.UPSIDE_DOWN_CAKE) AppOpsManagerHidden.OP_FOREGROUND_SERVICE_SPECIAL_USE.appopsAllow else null,
        if (SafeAppOpsService.supportCreateA11yOverlay) AppOpsManagerHidden.OP_CREATE_ACCESSIBILITY_OVERLAY.appopsAllow else null,
        "sh ${shFolder.absolutePath}/expose.sh 1",
        "echo '> start.sh end'",
    ).joinToString("\n")
    val file = shFolder.resolve("start.sh")
    file.writeText(commandText)
    "adb shell sh ${file.absolutePath}"
}

@Composable
private fun TextListItem(
    list: List<String>,
    modifier: Modifier = Modifier,
    style: TextStyle = MiuixTheme.textStyles.body1,
) {
    val density = LocalDensity.current
    // MIUIX TextStyle.lineHeight 可能不是 Sp，直接 toDp() 会抛 Only Sp can convert to Px
    val lineHeightDp = style.lineHeightDp(density)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        list.forEach { text ->
            Row {
                Spacer(
                    modifier = Modifier
                        .padding(vertical = (lineHeightDp - 4.dp) / 2)
                        .clip(CircleShape)
                        .background(MiuixTheme.colorScheme.primary)
                        .size(4.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, style = style)
            }
        }
    }
}
