package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 全屏设置类弹窗：MIUIX Scaffold + TopAppBar */
@Composable
fun ScaffoldDialog(
    title: String,
    onClose: () -> Unit,
    content: @Composable (ColumnScope.() -> Unit),
) = FullscreenDialog(onDismissRequest = onClose) {
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MiuixTheme.colorScheme.surface,
        topBar = {
            TopAppBar(
                title = title,
                color = MiuixTheme.colorScheme.surface,
                actions = {
                    PerfIconButton(
                        imageVector = PerfIcon.Close,
                        onClick = onClose,
                    )
                },
                scrollBehavior = scrollBehavior,
                defaultWindowInsetsPadding = true,
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
                content = content,
            )
        },
    )
}
