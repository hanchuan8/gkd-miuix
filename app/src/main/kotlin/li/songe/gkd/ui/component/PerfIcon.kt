package li.songe.gkd.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import top.yukonga.miuix.kmp.basic.Icon as MiuixIcon
import top.yukonga.miuix.kmp.basic.IconButton as MiuixIconButton
import li.songe.gkd.ui.icon.Rocket
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Pin
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.icon.extended.Add
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Blocklist
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Edit
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.icon.extended.Forward
import top.yukonga.miuix.kmp.icon.extended.GridView
import top.yukonga.miuix.kmp.icon.extended.Help
import top.yukonga.miuix.kmp.icon.extended.Hide
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Layers
import top.yukonga.miuix.kmp.icon.extended.Link
import top.yukonga.miuix.kmp.icon.extended.ListView
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.icon.extended.Messages
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Recent
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Rename
import top.yukonga.miuix.kmp.icon.extended.Report
import top.yukonga.miuix.kmp.icon.extended.Scan
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.icon.extended.Show
import top.yukonga.miuix.kmp.icon.extended.Sort
import top.yukonga.miuix.kmp.icon.extended.Stopwatch
import top.yukonga.miuix.kmp.icon.extended.Store
import top.yukonga.miuix.kmp.icon.extended.Theme
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.icon.extended.Update

/** 跟随明暗主题的默认图标色：优先 LocalContentColor，否则 MIUIX onSurface */
@Composable
fun defaultIconTint(): Color {
    val local = LocalContentColor.current
    return if (local == Color.Unspecified) MiuixTheme.colorScheme.onSurface else local
}

@Composable
fun PerfIcon(
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = defaultIconTint(),
    contentDescription: String? = getIconDefaultDesc(imageVector),
) {
    MiuixIcon(
        imageVector = imageVector,
        modifier = modifier,
        contentDescription = contentDescription,
        tint = tint,
    )
}

@Composable
fun PerfIconButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = defaultIconTint(),
    contentDescription: String? = getIconDefaultDesc(imageVector),
    onClickLabel: String? = null,
) = TooltipIconButtonBox(
    contentDescription = contentDescription,
) {
    val buttonModifier = modifier.semantics {
        if (onClickLabel != null) {
            this.onClick(label = onClickLabel, action = null)
        }
    }
    MiuixIconButton(
        modifier = buttonModifier,
        enabled = enabled,
        onClick = onClick,
    ) {
        PerfIcon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.38f),
        )
    }
}

@Composable
fun PerfIcon(
    @DrawableRes id: Int,
    modifier: Modifier = Modifier,
    tint: Color = defaultIconTint(),
    contentDescription: String? = null,
) = MiuixIcon(
    painter = painterResource(id),
    modifier = modifier,
    contentDescription = contentDescription,
    tint = tint,
)

@Composable
fun PerfIconButton(
    @DrawableRes id: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = defaultIconTint(),
    contentDescription: String? = null,
    onClickLabel: String? = null,
) = TooltipIconButtonBox(
    contentDescription = contentDescription,
) {
    MiuixIconButton(
        modifier = modifier.semantics {
            if (onClickLabel != null) {
                this.onClick(label = onClickLabel, action = null)
            }
        },
        enabled = enabled,
        onClick = onClick,
    ) {
        PerfIcon(
            id = id,
            contentDescription = contentDescription,
            tint = if (enabled) tint else tint.copy(alpha = 0.38f),
        )
    }
}

fun getIconDefaultDesc(imageVector: ImageVector): String? = when (imageVector) {
    PerfIcon.Add -> "添加"
    PerfIcon.Edit -> "编辑"
    PerfIcon.Save -> "保存"
    PerfIcon.Delete -> "删除"
    PerfIcon.Share -> "分享"
    PerfIcon.Settings -> "设置"
    PerfIcon.Close -> "关闭"
    PerfIcon.ArrowBack -> "返回"
    PerfIcon.HelpOutline -> "帮助"
    PerfIcon.ToggleOff -> "关闭"
    PerfIcon.ToggleOn -> "开启"
    PerfIcon.History -> "历史记录"
    PerfIcon.Sort -> "排序筛选"
    PerfIcon.OpenInNew -> "新页面打开"
    PerfIcon.ContentCopy -> "复制文本"
    PerfIcon.MoreVert -> "更多操作"
    else -> null
}

/** 全局图标入口，统一使用 MIUIX 矢量图标 */
object PerfIcon {
    val Block get() = MiuixIcons.Blocklist
    val History get() = MiuixIcons.Recent
    val Sort get() = MiuixIcons.Sort
    val Add get() = MiuixIcons.Add
    val KeyboardArrowRight get() = MiuixIcons.ChevronForward
    val ContentCopy get() = MiuixIcons.Copy
    val MoreVert get() = MiuixIcons.More
    val ArrowBack get() = MiuixIcons.Back
    val Android get() = MiuixIcons.Store
    val Edit get() = MiuixIcons.Edit
    val Save get() = MiuixIcons.Ok
    val Share get() = MiuixIcons.Share
    val Delete get() = MiuixIcons.Delete
    val Eco get() = MiuixIcons.Stopwatch
    val Close get() = MiuixIcons.Close
    val OpenInNew get() = MiuixIcons.Forward
    val Settings get() = MiuixIcons.Settings
    val Home get() = MiuixIcons.Home
    val FormatListBulleted get() = MiuixIcons.ListView
    val Apps get() = MiuixIcons.GridView
    val Info get() = MiuixIcons.Info
    val ToggleOff get() = MiuixIcons.Hide
    val ToggleOn get() = MiuixIcons.Show
    val HelpOutline get() = MiuixIcons.Help
    val ArrowForward get() = MiuixIcons.Forward
    val Image get() = MiuixIcons.Image
    val WarningAmber get() = MiuixIcons.Report
    val RocketLaunch get() = Rocket
    val WhiteList get() = MiuixIcons.Pin
    val CenterFocusWeak get() = MiuixIcons.Scan
    val AutoMode get() = MiuixIcons.Theme
    val LightMode get() = MiuixIcons.Show
    val DarkMode get() = MiuixIcons.Hide
    val VerifiedUser get() = MiuixIcons.Lock
    val Api get() = MiuixIcons.Link
    val Autorenew get() = MiuixIcons.Refresh
    val UnfoldMore get() = MiuixIcons.ExpandMore
    val Memory get() = MiuixIcons.Layers
    val Notifications get() = MiuixIcons.Messages
    val Layers get() = MiuixIcons.Layers
    val Equalizer get() = MiuixIcons.Tune
    val Lock get() = MiuixIcons.Lock
    val Title get() = MiuixIcons.Rename
    val TextFields get() = MiuixIcons.Notes
    val ArrowDownward get() = MiuixIcons.ExpandMore
    val Check get() = MiuixIcons.Ok
    val Update get() = MiuixIcons.Update
}
