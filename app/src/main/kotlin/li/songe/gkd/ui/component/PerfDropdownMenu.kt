package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.BasicComponentColors
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ListPopupDefaults
import top.yukonga.miuix.kmp.basic.PopupPositionProvider
import top.yukonga.miuix.kmp.window.WindowListPopup

/**
 * MIUIX 下拉菜单。使用 [WindowListPopup]，避免在毛玻璃顶栏内开 Overlay 触发 RenderThread 崩溃。
 * [anchor] 与弹层须同一父级以便定位。
 */
@Composable
fun PerfDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: PopupPositionProvider.Align = PopupPositionProvider.Align.End,
    enableWindowDim: Boolean = true,
    anchor: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    if (anchor != null) {
        Box(
            modifier = modifier.wrapContentSize(Alignment.TopEnd),
        ) {
            anchor()
            DropdownPopup(
                expanded = expanded,
                onDismissRequest = onDismissRequest,
                alignment = alignment,
                enableWindowDim = enableWindowDim,
                content = content,
            )
        }
    } else {
        // 兼容旧用法：调用方自备锚点 Box，且须把按钮放进该 Box
        DropdownPopup(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            alignment = alignment,
            enableWindowDim = enableWindowDim,
            content = content,
        )
    }
}

@Composable
private fun DropdownPopup(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    alignment: PopupPositionProvider.Align,
    enableWindowDim: Boolean,
    content: @Composable () -> Unit,
) {
    WindowListPopup(
        show = expanded,
        popupModifier = modifier,
        popupPositionProvider = ListPopupDefaults.dropdownPositionProvider(verticalMargin = 0.dp),
        alignment = alignment,
        enableWindowDim = enableWindowDim,
        onDismissRequest = onDismissRequest,
    ) {
        ListPopupColumn {
            content()
        }
    }
}

@Composable
fun PerfDropdownMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    titleColor: Color = Color.Unspecified,
    startAction: (@Composable () -> Unit)? = null,
    endActions: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = if (titleColor == Color.Unspecified) {
        BasicComponentDefaults.titleColor()
    } else {
        BasicComponentColors(
            color = titleColor,
            disabledColor = titleColor.copy(alpha = 0.4f),
        )
    }
    BasicComponent(
        modifier = modifier,
        title = text,
        titleColor = colors,
        enabled = enabled,
        onClick = onClick,
        startAction = startAction,
        endActions = endActions,
    )
}
