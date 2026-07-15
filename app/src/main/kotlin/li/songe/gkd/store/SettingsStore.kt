package li.songe.gkd.store

import kotlinx.serialization.Serializable
import li.songe.gkd.META
import li.songe.gkd.notif.ActionTipNotif
import li.songe.gkd.util.ActionTipStyleOption
import li.songe.gkd.util.AppGroupOption
import li.songe.gkd.util.AppSortOption
import li.songe.gkd.util.AutomatorModeOption
import li.songe.gkd.util.RuleSortOption
import li.songe.gkd.util.UpdateChannelOption
import li.songe.gkd.util.UpdateTimeOption
import li.songe.gkd.util.findOption

@Serializable
data class SettingsStore(
    val enableAutomator: Boolean = false,
    val automatorMode: Int = AutomatorModeOption.A11yMode.value,
    val enableShizuku: Boolean = false,
    val enableMatch: Boolean = true,
    val enableStatusService: Boolean = false,
    val excludeFromRecents: Boolean = false,
    val captureScreenshot: Boolean = false,
    val screenshotTargetAppId: String = "",
    val screenshotEventSelector: String = "",
    val httpServerPort: Int = 8888,
    val updateSubsInterval: Long = UpdateTimeOption.Everyday.value,
    val captureVolumeChange: Boolean = false,
    val toastWhenClick: Boolean = true,
    val actionToast: String = META.appName,
    val autoClearMemorySubs: Boolean = false,
    val hideSnapshotStatusBar: Boolean = false,
    val enableDarkTheme: Boolean? = null,
    val enableDynamicColor: Boolean = true,
    /** MIUIX：顶栏/底栏模糊（需 RuntimeShader） */
    val enableMiuixBlur: Boolean = true,
    /** MIUIX：悬浮底栏（类 Apple / FloatingNavigationBar） */
    val useFloatingNavBar: Boolean = true,
    /** MIUIX：悬浮底栏液态玻璃高光（依赖模糊） */
    val enableLiquidGlass: Boolean = true,
    val showSaveSnapshotToast: Boolean = true,
    /** @deprecated 由 [actionTipStyle] 接管；读取时见 resolve */
    val useSystemToast: Boolean = false,
    /** 触发提示样式，见 [li.songe.gkd.util.ActionTipStyleOption] */
    val actionTipStyle: Int = 0,
    /** 实时通知自动消失时间（秒），见 [li.songe.gkd.notif.ActionTipNotif] 范围 */
    val actionTipLiveDurationSec: Int = ActionTipNotif.DEFAULT_DURATION_SEC,
    val useCustomNotifText: Boolean = false,
    val customNotifTitle: String = META.appName,
    val customNotifText: String = $$"${i}全局/${k}应用/${u}规则/${n}触发",
    val updateChannel: Int = if (META.isBeta) UpdateChannelOption.Beta.value else UpdateChannelOption.Stable.value,
    val appSort: Int = AppSortOption.ByUsedTime.value,
    val showBlockApp: Boolean = true,
    val appRuleSort: Int = RuleSortOption.ByDefault.value,
    val subsAppSort: Int = AppSortOption.ByUsedTime.value,
    val subsCategorySort: Int = AppSortOption.ByUsedTime.value,
    val subsAppShowUninstall: Boolean = false,
    val subsAppGroupType: Int = AppGroupOption.UserGroup.value or AppGroupOption.SystemGroup.value,
    val subsCategoryGroupType: Int = AppGroupOption.UserGroup.value or AppGroupOption.SystemGroup.value,
    val subsAppShowBlock: Boolean = false,
    val subsCategoryShowBlock: Boolean = false,
    val subsExcludeSort: Int = AppSortOption.ByUsedTime.value,
    val subsExcludeShowBlockApp: Boolean = true,
    val subsExcludeShowInnerDisabledApp: Boolean = true,
    val subsPowerWarn: Boolean = true,
    val enableBlockA11yAppList: Boolean = false,
    val blockA11yAppListFollowMatch: Boolean = true,
    val a11yAppSort: Int = AppSortOption.ByUsedTime.value,
    val a11yScopeAppSort: Int = AppSortOption.ByUsedTime.value,
    val appGroupType: Int = (1 shl AppGroupOption.normalObjects.size) - 1,
    val a11yAppGroupType: Int = appGroupType,
    val a11yScopeAppGroupType: Int = appGroupType,
    val subsExcludeAppGroupType: Int = appGroupType,
    val showDisabledRule: Boolean = true,
) {
    val useA11y get() = automatorMode == AutomatorModeOption.A11yMode.value
    val useAutomation get() = automatorMode == AutomatorModeOption.AutomationMode.value

    /** 兼容旧版 [useSystemToast]：未改过 [actionTipStyle] 时沿用 Toast 开关 */
    fun resolveActionTipStyle(): ActionTipStyleOption {
        if (actionTipStyle != ActionTipStyleOption.Overlay.value) {
            return ActionTipStyleOption.objects.findOption(actionTipStyle)
        }
        return if (useSystemToast) {
            ActionTipStyleOption.SystemToast
        } else {
            ActionTipStyleOption.Overlay
        }
    }

    fun resolveActionTipLiveDurationSec(): Int =
        actionTipLiveDurationSec.coerceIn(
            ActionTipNotif.MIN_DURATION_SEC,
            ActionTipNotif.MAX_DURATION_SEC,
        )

    val actionTipLiveDurationMs: Long
        get() = resolveActionTipLiveDurationSec() * 1000L
}