package li.songe.gkd.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import li.songe.gkd.util.stopCoroutine
import li.songe.gkd.util.throttle
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.coroutines.resume

data class AlertDialogOptions(
    val title: String? = null,
    val summary: String? = null,
    val textContent: (@Composable () -> Unit)? = null,
    val onDismissRequest: (() -> Unit)? = null,
    val confirmText: String,
    val confirmAction: () -> Unit,
    val confirmError: Boolean = false,
    val dismissText: String? = null,
    val dismissAction: (() -> Unit)? = null,
)

private fun buildDialogOptions(
    title: String,
    text: String?,
    textContent: (@Composable (() -> Unit))?,
    confirmText: String,
    confirmAction: () -> Unit,
    dismissText: String? = null,
    dismissAction: (() -> Unit)? = null,
    onDismissRequest: (() -> Unit)? = null,
    error: Boolean = false,
): AlertDialogOptions {
    return AlertDialogOptions(
        title = title,
        summary = if (textContent == null) text else null,
        textContent = textContent,
        onDismissRequest = onDismissRequest,
        confirmText = confirmText,
        confirmAction = confirmAction,
        confirmError = error,
        dismissText = dismissText,
        dismissAction = dismissAction,
    )
}

@Composable
fun BuildDialog(stateFlow: MutableStateFlow<AlertDialogOptions?>) {
    val options by stateFlow.collectAsState()
    val opt = options
    WindowDialog(
        show = opt != null,
        title = opt?.title,
        summary = opt?.summary,
        onDismissRequest = opt?.onDismissRequest ?: { stateFlow.value = null },
    ) {
        if (opt == null) return@WindowDialog
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            opt.textContent?.invoke()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (opt.dismissText != null && opt.dismissAction != null) {
                    TextButton(
                        text = opt.dismissText,
                        onClick = throttle(fn = opt.dismissAction),
                        modifier = Modifier.weight(1f),
                    )
                }
                TextButton(
                    text = opt.confirmText,
                    onClick = throttle(fn = opt.confirmAction),
                    modifier = Modifier.weight(1f),
                    colors = if (opt.confirmError) {
                        ButtonDefaults.textButtonColors(
                            textColor = MiuixTheme.colorScheme.error,
                        )
                    } else {
                        ButtonDefaults.textButtonColorsPrimary()
                    },
                )
            }
        }
    }
}

fun MutableStateFlow<AlertDialogOptions?>.updateDialogOptions(
    title: String,
    text: String? = null,
    textContent: (@Composable (() -> Unit))? = null,
    confirmText: String = DEFAULT_IK_TEXT,
    confirmAction: (() -> Unit)? = null,
    dismissText: String? = null,
    dismissAction: (() -> Unit)? = null,
    onDismissRequest: (() -> Unit)? = null,
    error: Boolean = false,
) {
    value = buildDialogOptions(
        title = title,
        text = text,
        textContent = textContent,
        confirmText = confirmText,
        confirmAction = confirmAction ?: { value = null },
        dismissText = dismissText,
        dismissAction = dismissAction ?: { value = null },
        onDismissRequest = onDismissRequest,
        error = error,
    )
}

private const val DEFAULT_IK_TEXT = "我知道了"
private const val DEFAULT_CONFIRM_TEXT = "确定"
private const val DEFAULT_DISMISS_TEXT = "取消"

suspend fun MutableStateFlow<AlertDialogOptions?>.getResult(
    title: String,
    text: String? = null,
    textContent: (@Composable (() -> Unit))? = null,
    dismissRequest: Boolean = false,
    confirmText: String = DEFAULT_CONFIRM_TEXT,
    dismissText: String = DEFAULT_DISMISS_TEXT,
    error: Boolean = false,
): Boolean {
    return suspendCancellableCoroutine { s ->
        val dismiss = {
            if (s.isActive) {
                s.resume(false)
            }
            this.value = null
        }
        updateDialogOptions(
            title = title,
            text = text,
            textContent = textContent,
            onDismissRequest = if (dismissRequest) dismiss else ({}),
            confirmText = confirmText,
            confirmAction = {
                if (s.isActive) {
                    s.resume(true)
                }
                this.value = null
            },
            dismissText = dismissText,
            dismissAction = dismiss,
            error = error,
        )
    }
}

suspend fun MutableStateFlow<AlertDialogOptions?>.waitResult(
    title: String,
    text: String? = null,
    textContent: (@Composable (() -> Unit))? = null,
    dismissRequest: Boolean = false,
    confirmText: String = DEFAULT_CONFIRM_TEXT,
    dismissText: String = DEFAULT_DISMISS_TEXT,
    error: Boolean = false,
) {
    val r = getResult(
        title = title,
        text = text,
        textContent = textContent,
        dismissRequest = dismissRequest,
        confirmText = confirmText,
        dismissText = dismissText,
        error = error,
    )
    if (!r) {
        stopCoroutine()
    }
}
