package li.songe.gkd.service

import androidx.compose.foundation.shape.RoundedCornerShape
import top.yukonga.miuix.kmp.theme.MiuixTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import li.songe.gkd.appScope
import li.songe.gkd.notif.StopServiceReceiver
import li.songe.gkd.notif.buttonNotif
import li.songe.gkd.permission.canDrawOverlaysState
import li.songe.gkd.ui.component.PerfIcon
import li.songe.gkd.util.SnapshotExt
import li.songe.gkd.util.launchTry
import li.songe.gkd.util.startForegroundServiceByClass
import li.songe.gkd.util.stopServiceByClass

class ButtonService : OverlayWindowService(
    positionKey = "button"
) {
    override fun onClickView() = appScope.launchTry {
        SnapshotExt.captureSnapshot()
    }.let { }

    override fun onLongClickView() = stopSelf()

    @Composable
    override fun ComposeContent() {
        val alpha = 0.75f
        PerfIcon(
            imageVector = PerfIcon.CenterFocusWeak,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MiuixTheme.colorScheme.primaryContainer.copy(alpha = alpha))
                .size(40.dp),
            tint = MiuixTheme.colorScheme.primary.copy(alpha = alpha),
        )
    }

    init {
        useAliveFlow(isRunning)
        useAliveToast("快照按钮服务")
        onCreated { buttonNotif.notifyService() }
        StopServiceReceiver.autoRegister()
    }

    companion object {
        val isRunning = MutableStateFlow(false)
        fun start() {
            if (!canDrawOverlaysState.checkOrToast()) return
            startForegroundServiceByClass(ButtonService::class)
        }

        fun stop() = stopServiceByClass(ButtonService::class)
    }
}