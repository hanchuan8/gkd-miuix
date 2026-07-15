package li.songe.gkd.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    suffix: String? = null,
    suffixUnderline: Boolean = false,
    onSuffixClick: (() -> Unit)? = null,
    imageVector: ImageVector? = PerfIcon.KeyboardArrowRight,
    onClick: (() -> Unit)? = null,
    onClickLabel: String? = null,
) {
    val hasSuffixLink = subtitle != null && suffix != null && onSuffixClick != null
    val summaryText = when {
        hasSuffixLink -> null
        subtitle != null && suffix != null -> "$subtitle $suffix"
        else -> subtitle
    }
    val click = onClick?.let { throttle(fn = it) }
    when {
        imageVector == null -> {
            BasicComponent(
                title = title,
                summary = summaryText,
                onClick = click,
                onClickLabel = onClickLabel,
                bottomAction = if (hasSuffixLink) {
                    {
                        SettingSuffixRow(
                            subtitle = subtitle,
                            suffix = suffix,
                            onSuffixClick = onSuffixClick,
                        )
                    }
                } else null,
            )
        }
        imageVector == PerfIcon.KeyboardArrowRight -> {
            ArrowPreference(
                title = title,
                summary = summaryText,
                onClick = click,
                bottomAction = if (hasSuffixLink) {
                    {
                        SettingSuffixRow(
                            subtitle = subtitle,
                            suffix = suffix,
                            onSuffixClick = onSuffixClick,
                        )
                    }
                } else null,
            )
        }
        else -> {
            BasicComponent(
                title = title,
                summary = summaryText,
                onClick = click,
                onClickLabel = onClickLabel ?: "进入${title}页面",
                endActions = {
                    PerfIcon(imageVector = imageVector, contentDescription = null)
                },
            )
        }
    }
}

@Composable
private fun SettingSuffixRow(
    subtitle: String,
    suffix: String,
    onSuffixClick: (() -> Unit)?,
) {
    Row {
        MiuixText(
            text = subtitle,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Spacer(modifier = Modifier.width(4.dp))
        MiuixText(
            text = suffix,
            style = MiuixTheme.textStyles.body2,
            color = MiuixTheme.colorScheme.primary,
            modifier = if (onSuffixClick != null) {
                Modifier.clickable(onClick = throttle(fn = onSuffixClick))
            } else {
                Modifier
            },
        )
    }
}
