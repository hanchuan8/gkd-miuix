package li.songe.gkd

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import li.songe.gkd.ui.ActionLogPage
import li.songe.gkd.ui.ActionLogRoute
import li.songe.gkd.ui.component.BuildDialog
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.AppTheme
import java.lang.ref.WeakReference

/**
 * ColorOSNotifyIcon「管理规则」同款：触发记录走独立 Activity + 系统窗口转场，
 * 首页进入 onPause，不再和 Compose NavDisplay 同帧抢绘制。
 */
class ActionLogActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        instanceRef = WeakReference(this)
        val route = intent.toActionLogRoute()
        setContent {
            val mainVm = remember { MainViewModel.instance }
            CompositionLocalProvider(LocalMainViewModel provides mainVm) {
                AppTheme {
                    ActionLogPage(route)
                    BuildDialog(mainVm.dialogFlow)
                }
            }
        }
    }

    override fun onDestroy() {
        if (instanceRef?.get() === this) {
            instanceRef = null
        }
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_SUBS_ID = "subsId"
        private const val EXTRA_APP_ID = "appId"
        private var instanceRef: WeakReference<ActionLogActivity>? = null

        val isShowing: Boolean get() = instanceRef?.get() != null

        fun start(context: Context, route: ActionLogRoute) {
            context.startActivity(
                Intent(context, ActionLogActivity::class.java).apply {
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (route.subsId != null) {
                        putExtra(EXTRA_SUBS_ID, route.subsId)
                    }
                    if (route.appId != null) {
                        putExtra(EXTRA_APP_ID, route.appId)
                    }
                },
            )
        }

        /** @return true 若已关闭当前触发记录页 */
        fun finishIfShowing(): Boolean {
            val activity = instanceRef?.get() ?: return false
            activity.finish()
            return true
        }

        private fun Intent.toActionLogRoute(): ActionLogRoute = ActionLogRoute(
            subsId = if (hasExtra(EXTRA_SUBS_ID)) getLongExtra(EXTRA_SUBS_ID, 0L) else null,
            appId = getStringExtra(EXTRA_APP_ID),
        )
    }
}
