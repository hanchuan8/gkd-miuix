package li.songe.gkd.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Text as MiuixText
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TextSwitch(
    modifier: Modifier = Modifier,
    title: String,
    paddingDisabled: Boolean = false,
    subtitle: String? = null,
    suffix: String? = null,
    suffixUnderline: Boolean = false,
    onSuffixClick: (() -> Unit)? = null,
    suffixIcon: (@Composable () -> Unit)? = null,
    checked: Boolean = true,
    enabled: Boolean = true,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = { onCheckedChange?.invoke(!checked) },
    onClickLabel: String? = "切换${title}状态",
) {
    val throttledChange = onCheckedChange?.let { throttle(fn = it) }
    val simpleToggle = onClickLabel == "切换${title}状态" && suffixIcon == null
    val hasSuffixLink = suffix != null && onSuffixClick != null
    val summaryText = when {
        hasSuffixLink -> null
        subtitle != null && suffix != null -> "$subtitle $suffix"
        else -> subtitle
    }
    if (simpleToggle && !hasSuffixLink) {
        SwitchPreference(
            modifier = modifier,
            title = title,
            summary = summaryText,
            checked = checked,
            enabled = enabled,
            onCheckedChange = { throttledChange?.invoke(it) },
        )
    } else {
        BasicComponent(
            modifier = modifier,
            title = title,
            summary = summaryText,
            enabled = enabled,
            onClick = onClick,
            onClickLabel = onClickLabel,
            role = Role.Switch,
            endActions = {
                suffixIcon?.invoke()
                PerfSwitch(
                    checked = checked,
                    enabled = enabled,
                    onCheckedChange = throttledChange,
                    modifier = Modifier.semantics {
                        this.stateDescription = title + if (checked) "已开启" else "已关闭"
                    },
                )
            },
            bottomAction = if (hasSuffixLink && subtitle != null) {
                {
                    Row {
                        MiuixText(
                            text = subtitle,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        MiuixText(
                            text = suffix!!,
                            style = MiuixTheme.textStyles.body2,
                            color = MiuixTheme.colorScheme.primary,
                            modifier = Modifier.clickable(
                                onClick = throttle(fn = onSuffixClick!!),
                            ),
                        )
                    }
                }
            } else {
                null
            },
        )
    }
}
