package li.songe.gkd.ui.component

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import li.songe.gkd.MainActivity
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun MultiTextField(
    modifier: Modifier = Modifier,
    textFlow: MutableStateFlow<String>,
    immediateFocus: Boolean = false,
    indicatorSize: Int? = null,
    placeholderText: String? = null,
) {
    val text by textFlow.collectAsState()
    Box(modifier = modifier) {
        TextField(
            value = text,
            onValueChange = { textFlow.value = it },
            modifier = Modifier
                .autoFocus(immediateFocus = immediateFocus)
                .fillMaxSize()
                .optimizedImePadding(),
            label = placeholderText.orEmpty(),
            useLabelAsPlaceholder = true,
            textStyle = MiuixTheme.textStyles.main,
        )
        val actualSize = indicatorSize ?: text.length
        if (actualSize > 0 && text.isNotEmpty()) {
            Text(
                text = actualSize.toString(),
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MiuixTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 2.dp),
                style = MiuixTheme.textStyles.body2,
                color = MiuixTheme.colorScheme.secondary,
            )
        }
    }
}

private fun Modifier.optimizedImePadding() = composed {
    val context = LocalActivity.current as MainActivity
    if (context.imePlayingFlow.collectAsState().value) {
        this
    } else {
        imePadding()
    }
}
