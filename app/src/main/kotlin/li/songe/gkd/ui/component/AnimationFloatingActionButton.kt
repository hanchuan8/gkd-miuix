package li.songe.gkd.ui.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationConstants.DefaultDurationMillis
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.liquid.lens
import li.songe.gkd.ui.liquid.vibrancy
import li.songe.gkd.ui.share.LocalDarkTheme
import li.songe.gkd.ui.share.LocalLayerBackdrop
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.blur.blur
import top.yukonga.miuix.kmp.blur.drawBackdrop
import top.yukonga.miuix.kmp.blur.highlight.BloomStroke
import top.yukonga.miuix.kmp.blur.highlight.Highlight
import top.yukonga.miuix.kmp.blur.highlight.LightPosition
import top.yukonga.miuix.kmp.blur.highlight.LightSource
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val elevationDurationMillis = 50

private val FabGlassHighlight = Highlight(
    width = 1.dp,
    alpha = 1f,
    style = BloomStroke(
        color = Color.White.copy(alpha = 0.16f),
        innerBlurRadius = 2.0.dp,
        primaryLight = LightSource(
            position = LightPosition(0.5f, -0.3f, -0.05f),
            color = Color.White,
            intensity = 1f,
        ),
        secondaryLight = LightSource(
            position = LightPosition(0.5f, 0.8f, -0.5f),
            color = Color.White,
            intensity = 0.4f,
        ),
        dualPeak = true,
    ),
)

@Composable
fun AnimationFloatingActionButton(
    visible: Boolean,
    onClick: () -> Unit,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
    onClickLabel: String? = null,
    contentDescription: String? = getIconDefaultDesc(imageVector),
) {
    val density = LocalDensity.current
    val maxTranslationX = remember(density.density) { density.run { 24.dp.toPx() } }
    var innerVisible by remember { mutableStateOf(visible) }
    val percent = remember { Animatable(if (visible) 1f else 0f) }
    LaunchedEffect(visible) {
        if (visible != innerVisible) {
            if (visible) {
                innerVisible = true
                percent.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = DefaultDurationMillis - elevationDurationMillis)
                )
            } else {
                percent.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = DefaultDurationMillis - elevationDurationMillis)
                )
                innerVisible = false
            }
        }
    }
    if (innerVisible) {
        val fabModifier = modifier
            .graphicsLayer(
                alpha = percent.value,
                translationX = (1f - percent.value) * maxTranslationX
            )
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
                if (onClickLabel != null) {
                    this.onClick(label = onClickLabel, action = null)
                }
            }
        TooltipIconButtonBox(contentDescription) {
            LiquidGlassFab(
                onClick = throttle(onClick),
                imageVector = imageVector,
                modifier = fabModifier,
            )
        }
    }
}

@Composable
private fun LiquidGlassFab(
    onClick: () -> Unit,
    imageVector: ImageVector,
    modifier: Modifier = Modifier,
) {
    val store by storeFlow.collectAsState()
    val backdrop = LocalLayerBackdrop.current
    val isDark = LocalDarkTheme.current
    val shaderOk = isRuntimeShaderSupported()
    val liquidOk = store.enableLiquidGlass && store.enableMiuixBlur && shaderOk && backdrop != null
    val shape = remember { CircleShape }
    val surfaceContainer = MiuixTheme.colorScheme.surfaceContainer
    val containerColor = if (liquidOk) surfaceContainer.copy(alpha = 0.4f) else surfaceContainer
    val iconTint = MiuixTheme.colorScheme.onSurface
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(56.dp)
            .dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 10.dp,
                    color = Color.Black,
                    alpha = if (isDark) 0.22f else 0.12f,
                ),
            )
            .then(
                if (liquidOk) {
                    Modifier.drawBackdrop(
                        backdrop = backdrop!!,
                        shape = { shape },
                        effects = {
                            vibrancy()
                            blur(4.dp.toPx(), 4.dp.toPx())
                            padding = maxOf(padding, 28.dp.toPx())
                            lens(
                                refractionHeight = 14.dp.toPx(),
                                refractionAmount = 16.dp.toPx(),
                            )
                        },
                        highlight = { FabGlassHighlight.copy(alpha = 0.75f) },
                        onDrawSurface = { drawRect(containerColor) },
                    )
                } else {
                    Modifier
                        .clip(shape)
                        .background(containerColor)
                },
            )
            .clickable(
                interactionSource = interaction,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        PerfIcon(
            imageVector = imageVector,
            contentDescription = null,
            tint = iconTint,
        )
    }
}
