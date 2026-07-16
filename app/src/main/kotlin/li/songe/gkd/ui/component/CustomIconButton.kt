package li.songe.gkd.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
private fun CustomIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
    size: Dp = 40.dp,
    enabled: Boolean = true,
    contentColor: Color = defaultIconTint(),
    containerColor: Color = Color.Transparent,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Box(
        modifier =
            modifier
                .size(size)
                .clip(CircleShape)
                .background(color = containerColor)
                .clickable(
                    onClick = onClick,
                    onClickLabel = onClickLabel,
                    enabled = enabled,
                    role = Role.Button,
                    interactionSource = source,
                    indication = ripple(bounded = false, radius = size / 2)
                ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (enabled) contentColor else contentColor.copy(alpha = 0.38f),
            content = content,
        )
    }
}

@Composable
fun PerfCustomIconButton(
    onClick: () -> Unit,
    size: Dp,
    iconSize: Dp,
    onClickLabel: String? = null,
    @DrawableRes id: Int,
    contentDescription: String? = null,
    tint: Color = defaultIconTint(),
) = TooltipIconButtonBox(
    contentDescription = contentDescription,
) {
    CustomIconButton(
        size = size,
        onClickLabel = onClickLabel,
        onClick = onClick,
    ) {
        PerfIcon(
            modifier = Modifier.size(iconSize),
            id = id,
            contentDescription = contentDescription,
            tint = tint,
        )
    }
}
