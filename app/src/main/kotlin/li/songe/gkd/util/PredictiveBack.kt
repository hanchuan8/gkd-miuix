package li.songe.gkd.util

import android.content.Context
import android.content.pm.ApplicationInfo

/**
 * 运行时开关预测式返回。依赖系统 [ApplicationInfo.setEnableOnBackInvokedCallback]（@hide），
 * 需在 Activity [android.app.Activity.onCreate] 之前调用一次，切换后建议 [android.app.Activity.recreate]。
 */
fun Context.applyPredictiveBackEnabled(enabled: Boolean) {
    if (!AndroidTarget.TIRAMISU) return
    try {
        val method = ApplicationInfo::class.java.getMethod(
            "setEnableOnBackInvokedCallback",
            Boolean::class.javaPrimitiveType,
        )
        method.invoke(applicationInfo, enabled)
    } catch (e: Throwable) {
        LogUtils.d("applyPredictiveBackEnabled failed", e)
    }
}
