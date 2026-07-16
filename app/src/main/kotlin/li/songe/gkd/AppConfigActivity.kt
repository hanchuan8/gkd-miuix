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
import li.songe.gkd.data.ActionLog
import li.songe.gkd.ui.AppConfigPage
import li.songe.gkd.ui.AppConfigRoute
import li.songe.gkd.ui.component.BuildDialog
import li.songe.gkd.ui.share.LocalMainViewModel
import li.songe.gkd.ui.style.AppTheme
import li.songe.gkd.util.json
import java.lang.ref.WeakReference
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * 「最近触发」等入口打开的应用配置页：独立 Activity + 系统转场，避免 NavDisplay 掉帧。
 */
class AppConfigActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        instanceRef = WeakReference(this)
        val route = intent.toAppConfigRoute()
        setContent {
            val mainVm = remember { MainViewModel.instance }
            CompositionLocalProvider(LocalMainViewModel provides mainVm) {
                AppTheme {
                    AppConfigPage(route)
                    BuildDialog(mainVm.dialogFlow)
                    mainVm.ruleGroupState.Render()
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
        private const val EXTRA_APP_ID = "appId"
        private const val EXTRA_FOCUS_LOG = "focusLog"
        private var instanceRef: WeakReference<AppConfigActivity>? = null

        val isShowing: Boolean get() = instanceRef?.get() != null

        fun start(context: Context, route: AppConfigRoute) {
            context.startActivity(
                Intent(context, AppConfigActivity::class.java).apply {
                    if (context !is Activity) {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    putExtra(EXTRA_APP_ID, route.appId)
                    route.focusLog?.let {
                        putExtra(EXTRA_FOCUS_LOG, json.encodeToString(it))
                    }
                },
            )
        }

        fun finishIfShowing(): Boolean {
            val activity = instanceRef?.get() ?: return false
            activity.finish()
            return true
        }

        private fun Intent.toAppConfigRoute(): AppConfigRoute = AppConfigRoute(
            appId = getStringExtra(EXTRA_APP_ID).orEmpty(),
            focusLog = getStringExtra(EXTRA_FOCUS_LOG)?.let {
                runCatching { json.decodeFromString<ActionLog>(it) }.getOrNull()
            },
        )
    }
}
