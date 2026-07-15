package li.songe.gkd.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import li.songe.gkd.MainActivity
import li.songe.gkd.ui.component.AppPageScaffold
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.autoFocus
import li.songe.gkd.ui.component.waitResult
import li.songe.gkd.ui.share.LocalDarkTheme
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.getJson5Transformation
import li.songe.gkd.ui.style.scaffoldPadding
import li.songe.gkd.util.launchAsFn
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TextFieldDefaults
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Serializable
data class UpsertRuleGroupRoute(
    val subsId: Long,
    val groupKey: Int? = null,
    val appId: String? = null,
    val forward: Boolean = false,
) : NavKey

@Composable
fun UpsertRuleGroupPage(route: UpsertRuleGroupRoute) {
    val subsId = route.subsId
    val appId = route.appId
    val forward = route.forward

    val mainVm = LocalMainViewModel.current
    val context = LocalActivity.current as MainActivity
    val vm = viewModel { UpsertRuleGroupVm(route) }
    val text by vm.textFlow.collectAsState()

    val checkIfSaveText = throttle(mainVm.viewModelScope.launchAsFn(Dispatchers.Default) {
        if (vm.hasTextChanged()) {
            context.justHideSoftInput()
            mainVm.dialogFlow.waitResult(
                title = "提示",
                text = "当前内容未保存，是否放弃编辑？",
            )
        } else {
            context.hideSoftInput()
        }
        mainVm.popPage()
    })

    val onClickSave = throttle(vm.viewModelScope.launchAsFn(Dispatchers.Main) {
        withContext(Dispatchers.Default) { vm.saveRule() }
        context.hideSoftInput()
        if (forward) {
            if (appId == null) {
                mainVm.navigatePage(
                    SubsGlobalGroupListRoute(subsItemId = subsId),
                    replaced = true
                )
            } else {
                mainVm.navigatePage(
                    SubsAppGroupListRoute(
                        subsItemId = subsId,
                        vm.addAppId ?: appId
                    ),
                    replaced = true
                )
            }
        } else {
            mainVm.popPage()
        }
    })
    BackHandler(true, checkIfSaveText)
    AppPageScaffold(
        title = if (vm.isEdit) "编辑规则" else "添加规则",
        navigationIcon = {
            PerfIconButton(imageVector = PerfIcon.ArrowBack, onClick = checkIfSaveText)
        },
        actions = {
            PerfIconButton(
                imageVector = PerfIcon.Save,
                onClick = onClickSave,
                enabled = text.isNotBlank()
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .scaffoldPadding(paddingValues)
                .fillMaxSize(),
        ) {
            val imeShowing by context.imePlayingFlow.collectAsState()
            val modifier = Modifier
                .autoFocus()
                .fillMaxSize()
                .run {
                    if (imeShowing) {
                        this
                    } else {
                        imePadding()
                    }
                }
            TextField(
                value = text,
                onValueChange = { vm.textFlow.value = it },
                modifier = modifier,
                label = if (vm.isApp) "请输入应用规则" else "请输入全局规则",
                useLabelAsPlaceholder = true,
                textStyle = MiuixTheme.textStyles.main,
                cornerRadius = 0.dp,
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.Transparent,
                    borderColor = Color.Transparent,
                ),
                visualTransformation = getJson5Transformation(LocalDarkTheme.current),
            )
            if (text.isNotEmpty()) {
                Text(
                    text = text.length.toString(),
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
}
