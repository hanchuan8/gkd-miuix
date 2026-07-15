package li.songe.gkd.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import li.songe.gkd.store.storeFlow
import li.songe.gkd.ui.component.AppPageScaffold
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.ui.component.PerfIconButton
import li.songe.gkd.ui.component.PreferenceGroup
import li.songe.gkd.ui.component.TextMenu
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.EmptyHeight
import li.songe.gkd.util.DarkThemeOption
import li.songe.gkd.util.findOption
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Serializable
data object DesignRoute : NavKey

@Composable
fun DesignPage() {
    val mainVm = LocalMainViewModel.current
    val store by storeFlow.collectAsState()
    val scrollState = rememberScrollState()
    val shaderOk = isRuntimeShaderSupported()

    AppPageScaffold(
        title = "主题设置",
        navigationIcon = {
            PerfIconButton(
                imageVector = PerfIcon.ArrowBack,
                onClick = { mainVm.popPage() },
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(contentPadding),
        ) {
            PreferenceGroup(title = "主题设置", showTop = false) {
                TextMenu(
                    title = "主题",
                    option = DarkThemeOption.objects.findOption(store.enableDarkTheme),
                    onOptionChange = {
                        storeFlow.update { s -> s.copy(enableDarkTheme = it.value) }
                    },
                )
                SwitchPreference(
                    title = "模糊",
                    summary = if (shaderOk) {
                        "启用顶栏和底栏的模糊效果"
                    } else {
                        "当前设备不支持 RuntimeShader，无法启用模糊"
                    },
                    checked = store.enableMiuixBlur && shaderOk,
                    enabled = shaderOk,
                    onCheckedChange = {
                        storeFlow.update { s -> s.copy(enableMiuixBlur = it) }
                    },
                )
                SwitchPreference(
                    title = "悬浮底栏",
                    summary = "使用类 Apple 风格的悬浮底栏",
                    checked = store.useFloatingNavBar,
                    onCheckedChange = {
                        storeFlow.update { s -> s.copy(useFloatingNavBar = it) }
                    },
                )
                AnimatedVisibility(visible = store.useFloatingNavBar) {
                    SwitchPreference(
                        title = "液态玻璃",
                        summary = "启用 iOS 风格悬浮底栏液态玻璃效果",
                        checked = store.enableLiquidGlass && store.enableMiuixBlur && shaderOk,
                        enabled = store.enableMiuixBlur && shaderOk,
                        onCheckedChange = {
                            storeFlow.update { s -> s.copy(enableLiquidGlass = it) }
                        },
                    )
                }
            }
            Spacer(modifier = Modifier.height(EmptyHeight))
        }
    }
}
