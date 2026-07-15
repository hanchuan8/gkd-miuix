package li.songe.gkd.permission

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import li.songe.gkd.MainActivity
import li.songe.gkd.ui.component.PerfAlertDialog
import li.songe.gkd.util.stopCoroutine
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import androidx.compose.ui.Modifier

data class AuthReason(
    val text: () -> String,
    val confirm: ((Activity) -> Unit)? = null,
)

@Composable
fun AuthDialog(authReasonFlow: MutableStateFlow<AuthReason?>) {
    val authAction = authReasonFlow.collectAsState().value
    val context = LocalActivity.current as MainActivity
    if (authAction != null) {
        PerfAlertDialog(
            title = {
                Text(text = "权限请求")
            },
            text = {
                Text(text = authAction.text())
            },
            onDismissRequest = { authReasonFlow.value = null },
            confirmButton = {
                TextButton(
                    text = "确认",
                    onClick = {
                        authReasonFlow.value = null
                        authAction.confirm?.invoke(context)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColorsPrimary(),
                )
            },
            dismissButton = {
                TextButton(
                    text = "取消",
                    onClick = { authReasonFlow.value = null },
                    modifier = Modifier.weight(1f),
                )
            }
        )
    }
}

sealed class PermissionResult {
    data object Granted : PermissionResult()
    data class Denied(val doNotAskAgain: Boolean) : PermissionResult()
}

suspend fun requiredPermission(
    context: MainActivity,
    permissionState: PermissionState
) {
    if (permissionState.updateAndGet()) return
    val result = permissionState.request?.invoke(context)
    if (result == null) {
        context.mainVm.authReasonFlow.value = permissionState.reason
        stopCoroutine()
    } else if (result is PermissionResult.Denied) {
        if (result.doNotAskAgain) {
            context.mainVm.authReasonFlow.value = permissionState.reason
        }
        stopCoroutine()
    }
}
