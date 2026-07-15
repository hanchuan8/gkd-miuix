package li.songe.gkd.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import li.songe.gkd.ui.component.AppPageScaffold
import li.songe.gkd.ui.component.CopyTextCard
import li.songe.gkd.ui.component.EmptyText
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.ui.style.itemHorizontalPadding
import li.songe.gkd.ui.style.itemVerticalPadding
import li.songe.gkd.util.ISSUES_URL
import li.songe.gkd.util.throttle


@Serializable
data object CrashReportRoute : NavKey

@Composable
fun CrashReportPage() {
    val mainVm = LocalMainViewModel.current
    val vm = viewModel<CrashReportVm>()
    val scrollState = rememberScrollState()
    AppPageScaffold(
        title = "崩溃记录",
        navigationIcon = {
            PerfIconButton(
                imageVector = PerfIcon.ArrowBack,
                onClick = mainVm::popPage,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(scrollState)
                    .fillMaxSize()
                    .padding(bottom = if (vm.crashDataList.isNotEmpty()) 56.dp else 0.dp),
                verticalArrangement = Arrangement.spacedBy(itemVerticalPadding)
            ) {
                if (vm.crashDataList.isNotEmpty()) {
                    vm.crashDataList.forEach { crashData ->
                        CopyTextCard(
                            text = crashData.stackTrace,
                            modifier = Modifier.padding(horizontal = 8.dp),
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(EmptyHeight))
                    EmptyText()
                }
                Spacer(modifier = Modifier.height(EmptyHeight))
            }
            if (vm.crashDataList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = itemHorizontalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        text = "问题反馈",
                        onClick = throttle { mainVm.openUrl(ISSUES_URL) },
                    )
                    Spacer(modifier = Modifier.width(itemHorizontalPadding))
                    TextButton(
                        text = "导出日志",
                        onClick = { mainVm.showShareLogDlgFlow.value = true },
                    )
                }
            }
        }
    }
}
