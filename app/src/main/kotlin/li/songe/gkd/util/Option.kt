package li.songe.gkd.util

import androidx.compose.ui.graphics.vector.ImageVector
import li.songe.gkd.ui.component.PerfIcon

sealed interface Option<T> {
    val value: T
    val label: String
    val options: List<Option<T>>
}

sealed interface OptionIcon {
    val icon: ImageVector
}

sealed interface OptionMenuLabel {
    val menuLabel: String
}

fun <V, T : Option<V>> Iterable<T>.findOption(value: V): T {
    return find { it.value == value } ?: first()
}

sealed class AppSortOption(override val value: Int, override val label: String) : Option<Int> {
    override val options get() = objects

    data object ByAppName : AppSortOption(0, "按应用名称")
    data object ByActionTime : AppSortOption(2, "按最近触发")
    data object ByUsedTime : AppSortOption(3, "按最近使用")

    companion object {
        val objects by lazy { listOf(ByAppName, ByUsedTime, ByActionTime) }
    }
}

sealed class UpdateTimeOption(
    override val value: Long,
    override val label: String
) : Option<Long> {
    override val options get() = objects

    data object Pause : UpdateTimeOption(-1, "暂停")
    data object Everyday : UpdateTimeOption(24 * 60 * 60_000, "每天")
    data object Every3Days : UpdateTimeOption(24 * 60 * 60_000 * 3, "每3天")
    data object Every7Days : UpdateTimeOption(24 * 60 * 60_000 * 7, "每7天")

    companion object {
        val objects by lazy { listOf(Pause, Everyday, Every3Days, Every7Days) }
    }
}

sealed class DarkThemeOption(
    override val value: Boolean?,
    override val label: String,
    override val menuLabel: String,
    override val icon: ImageVector
) : Option<Boolean?>, OptionIcon, OptionMenuLabel {
    override val options get() = objects

    data object FollowSystem : DarkThemeOption(null, "自动", "自动", PerfIcon.AutoMode)
    data object AlwaysEnable : DarkThemeOption(true, "启用", "深色", PerfIcon.DarkMode)
    data object AlwaysDisable : DarkThemeOption(false, "关闭", "浅色", PerfIcon.LightMode)

    companion object {
        val objects by lazy { listOf(FollowSystem, AlwaysEnable, AlwaysDisable) }
    }
}

sealed class EnableGroupOption(
    override val value: Boolean?,
    override val label: String
) : Option<Boolean?> {
    override val options get() = objects

    data object FollowSubs : EnableGroupOption(null, "跟随订阅")
    data object AllEnable : EnableGroupOption(true, "全部启用")
    data object AllDisable : EnableGroupOption(false, "全部关闭")

    companion object {
        val objects by lazy { listOf(FollowSubs, AllEnable, AllDisable) }
    }
}

sealed class RuleSortOption(override val value: Int, override val label: String) : Option<Int> {
    override val options get() = objects

    data object ByDefault : RuleSortOption(0, "按默认顺序")
    data object ByActionTime : RuleSortOption(1, "按最近触发")
    data object ByRuleName : RuleSortOption(2, "按规则名称")

    companion object {
        val objects by lazy { listOf(ByDefault, ByActionTime, ByRuleName) }
    }
}

sealed class UpdateChannelOption(
    override val value: Int,
    override val label: String,
    val url: String
) : Option<Int> {
    override val options get() = objects

    data object Stable : UpdateChannelOption(
        0,
        "稳定版",
        "https://raw.githubusercontent.com/hanchuan8/gkd-miuix/main/version/stable.json"
    )

    data object Beta : UpdateChannelOption(
        1,
        "测试版",
        "https://raw.githubusercontent.com/hanchuan8/gkd-miuix/main/version/beta.json"
    )

    companion object {
        val objects by lazy { listOf(Stable, Beta) }
    }
}

sealed interface BinaryOption : Option<Int> {
    fun include(flag: Int): Boolean = (value and flag) != 0
    fun invert(flag: Int): Int = value xor flag

    companion object {
        fun combine(options: Collection<BinaryOption>): Int {
            return options.fold(0) { a, b -> a or b.value }
        }
    }
}


sealed class AppGroupOption(
    override val value: Int,
    override val label: String
) : BinaryOption {
    override val options get() = allObjects

    data object SystemGroup : AppGroupOption(1 shl 0, "系统应用")
    data object UserGroup : AppGroupOption(1 shl 1, "用户应用")
    data object UnInstalledGroup : AppGroupOption(1 shl 2, "未安装应用")

    companion object {
        val normalObjects by lazy { listOf(SystemGroup, UserGroup) }
        val allObjects by lazy { listOf(SystemGroup, UserGroup, UnInstalledGroup) }
    }
}

sealed class AutomatorModeOption(
    override val value: Int,
    override val label: String,
) : Option<Int> {
    override val options get() = objects

    data object A11yMode : AutomatorModeOption(1, "无障碍")
    data object AutomationMode : AutomatorModeOption(2, "自动化")

    companion object {
        val objects by lazy { listOf(A11yMode, AutomationMode) }
    }
}

sealed class ActionTipStyleOption(
    override val value: Int,
    override val label: String,
    override val menuLabel: String,
) : Option<Int>, OptionMenuLabel {
    override val options get() = objects

    data object Overlay : ActionTipStyleOption(0, "悬浮窗提示", "悬浮窗")
    data object SystemToast : ActionTipStyleOption(1, "系统 Toast", "Toast")
    data object LiveNotif : ActionTipStyleOption(2, "实时通知（灵动岛）", "实时通知")

    companion object {
        val objects by lazy { listOf(Overlay, SystemToast, LiveNotif) }
    }
}

sealed class ActionTipLiveDurationOption(
    override val value: Int,
    override val label: String,
    override val menuLabel: String,
) : Option<Int>, OptionMenuLabel {
    override val options get() = objects

    data object Sec3 : ActionTipLiveDurationOption(3, "3 秒", "3 秒")
    data object Sec5 : ActionTipLiveDurationOption(5, "5 秒", "5 秒")
    data object Sec8 : ActionTipLiveDurationOption(8, "8 秒", "8 秒")
    data object Sec15 : ActionTipLiveDurationOption(15, "15 秒", "15 秒")
    data object Sec30 : ActionTipLiveDurationOption(30, "30 秒", "30 秒")
    data object Sec60 : ActionTipLiveDurationOption(60, "60 秒", "60 秒")

    companion object {
        val objects by lazy { listOf(Sec3, Sec5, Sec8, Sec15, Sec30, Sec60) }

        fun resolve(sec: Int): ActionTipLiveDurationOption =
            objects.find { it.value == sec } ?: ActionTipLiveDurationCustom(sec)

        fun labelOf(sec: Int): String = resolve(sec).label
    }
}

private data class ActionTipLiveDurationCustom(
    private val sec: Int,
) : ActionTipLiveDurationOption(sec, "${sec} 秒", "${sec} 秒")

