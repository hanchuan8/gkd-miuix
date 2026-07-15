package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun TowLineText(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    showApp: Boolean = false,
    appFallbackName: String? = null,
) {
    Column(
        modifier = modifier,
    ) {
        Text(
            text = title,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.MiddleEllipsis,
            style = MiuixTheme.textStyles.title4,
        )
        if (showApp) {
            AppNameText(
                appId = subtitle,
                fallbackName = appFallbackName,
                style = MiuixTheme.textStyles.body2,
            )
        } else {
            Text(
                text = subtitle,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                style = MiuixTheme.textStyles.body2,
            )
        }
    }
}
