package li.songe.gkd.ui.style

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import li.songe.gkd.ui.share.LocalDarkTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme

/** 服务状态卡：运行中 / 已停止 的容器色与图标色（浅色 / 深色）。 */
@Immutable
data class ServiceStatusColors(
    val container: Color,
    val icon: Color,
)

object StatusColors {
    private val RunningLightContainer = Color(0xFFDFFAE4)
    private val RunningDarkContainer = Color(0xFF1A3825)
    private val RunningIcon = Color(0xFF36D167)

    private val StoppedLightContainer = Color(0xFFFDECEC)
    private val StoppedDarkContainer = Color(0xFF3A2424)

    @Composable
    fun serviceStatus(running: Boolean): ServiceStatusColors {
        val darkTheme = LocalDarkTheme.current
        val error = MiuixTheme.colorScheme.error
        return remember(running, darkTheme, error) {
            if (running) {
                ServiceStatusColors(
                    container = if (darkTheme) RunningDarkContainer else RunningLightContainer,
                    icon = RunningIcon,
                )
            } else {
                ServiceStatusColors(
                    container = if (darkTheme) StoppedDarkContainer else StoppedLightContainer,
                    icon = error.copy(alpha = 0.75f),
                )
            }
        }
    }
}
