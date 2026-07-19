package li.songe.gkd.notif

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import li.songe.gkd.META
import li.songe.gkd.MainActivity
import li.songe.gkd.R
import li.songe.gkd.app
import li.songe.gkd.permission.notificationState
import li.songe.gkd.util.LogUtils
import li.songe.gkd.util.componentName
import org.json.JSONObject

/**
 * 触发提示「实时通知」—— ColorOS 流体云 + HyperOS 超级岛 共存。
 *
 * 同一条 ongoing 通知叠两层能力，各系统只认自己的字段：
 * 1. Google Live Update（ProgressStyle + setRequestPromotedOngoing）
 *    → ColorOS 16 流体云 / AOSP 状态栏芯片
 * 2. HyperOS 客户端岛参数（miui.focus.param + miui.focus.pics）
 *    → 小米超级岛 / 焦点通知
 *
 * Live Update 相关 API 一律反射调用，避免国产 ROM 缺方法时 linkage 崩溃。
 */
object ActionTipNotif {
    const val ID = 200
    const val DEFAULT_DURATION_SEC = 8
    const val MIN_DURATION_SEC = 2
    const val MAX_DURATION_SEC = 120

    /** AOSP：Notification.EXTRA_REQUEST_PROMOTED_ONGOING 字面值 */
    private const val EXTRA_REQUEST_PROMOTED_ONGOING = "android.requestPromotedOngoing"

    /** AOSP：Settings.ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS */
    private const val ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS =
        "android.settings.APP_NOTIFICATION_PROMOTION_SETTINGS"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var cancelRunnable: Runnable? = null

    /** ColorOS / AOSP：framework 是否具备 Live Update 构建能力（与 SDK_INT 解耦） */
    private val liveUpdateBuildSupport: Boolean by lazy {
        if (Build.VERSION.SDK_INT < 36) return@lazy false
        try {
            Notification.Builder::class.java.getMethod(
                "setRequestPromotedOngoing",
                Boolean::class.javaPrimitiveType,
            )
            true
        } catch (_: Throwable) {
            false
        }
    }

    private val hasPromotableMethod: Boolean by lazy {
        try {
            Notification::class.java.getMethod("hasPromotableCharacteristics")
            true
        } catch (_: Throwable) {
            false
        }
    }

    private val canPostPromotedMethod: Boolean by lazy {
        try {
            NotificationManager::class.java.getMethod("canPostPromotedNotifications")
            true
        } catch (_: Throwable) {
            false
        }
    }

    private val progressStyleClass: Class<*>? by lazy {
        try {
            Class.forName("android.app.Notification\$ProgressStyle")
        } catch (_: Throwable) {
            null
        }
    }

    data class PostResult(
        val posted: Boolean,
        val canPostPromoted: Boolean? = null,
        val hasPromotableCharacteristics: Boolean? = null,
        val message: String = "",
    )

    fun show(text: CharSequence, autoDismissMs: Long = DEFAULT_DURATION_SEC * 1000L): PostResult {
        if (!notificationState.updateAndGet()) {
            return PostResult(posted = false, message = "未授予通知权限")
        }
        ensureActionChannel()
        val content = text.toString().ifBlank { META.appName }
        val dismissMs = autoDismissMs.coerceIn(
            MIN_DURATION_SEC * 1000L,
            MAX_DURATION_SEC * 1000L,
        )
        val notification = buildNotification(content, dismissMs)
        val canPromote = safeCanPostPromoted()
        val promotable = safeHasPromotable(notification)
        val focusProtocol = readFocusProtocolVersion()
        val canShowFocus = readCanShowFocus()
        val hyperOs = focusProtocol >= 2 || canShowFocus != null
        LogUtils.d(
            "ActionTipNotif liveBuild=$liveUpdateBuildSupport " +
                "canPostPromoted=$canPromote promotable=$promotable " +
                "focusProtocol=$focusProtocol canShowFocus=$canShowFocus",
        )
        @SuppressLint("MissingPermission")
        NotificationManagerCompat.from(app).notify(ID, notification)
        scheduleCancel(dismissMs)

        val parts = mutableListOf<String>()
        when {
            liveUpdateBuildSupport && canPromote == false ->
                parts += "未开「实时更新/流体云」权限"
            liveUpdateBuildSupport && (canPromote == true || promotable != false) ->
                parts += "已请求流体云/Live Update"
            liveUpdateBuildSupport ->
                parts += "已发 Live Update 通知"
            else ->
                parts += "本机无完整 Live Update API"
        }
        if (hyperOs) {
            parts += "已附超级岛参数(protocol=$focusProtocol" +
                (canShowFocus?.let { ",focus=$it" } ?: "") + ")"
        } else {
            parts += "已附超级岛参数(非 HyperOS 会被忽略)"
        }
        return PostResult(
            posted = true,
            canPostPromoted = canPromote,
            hasPromotableCharacteristics = promotable,
            message = parts.joinToString("；"),
        )
    }

    fun cancel() {
        cancelRunnable?.let { mainHandler.removeCallbacks(it) }
        cancelRunnable = null
        NotificationManagerCompat.from(app).cancel(ID)
    }

    fun openPromotedSettings(): Boolean {
        return try {
            app.startActivity(
                Intent(ACTION_APP_NOTIFICATION_PROMOTION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, app.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
            true
        } catch (_: Throwable) {
            try {
                app.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, app.packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    },
                )
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    fun canPostPromoted(): Boolean? = safeCanPostPromoted()

    private fun safeCanPostPromoted(): Boolean? {
        if (!canPostPromotedMethod) return null
        return try {
            val nm = app.getSystemService(NotificationManager::class.java)
            NotificationManager::class.java
                .getMethod("canPostPromotedNotifications")
                .invoke(nm) as Boolean
        } catch (_: Throwable) {
            null
        }
    }

    private fun safeHasPromotable(notification: Notification): Boolean? {
        if (!hasPromotableMethod) return null
        return try {
            Notification::class.java
                .getMethod("hasPromotableCharacteristics")
                .invoke(notification) as Boolean
        } catch (_: Throwable) {
            null
        }
    }

    private fun scheduleCancel(delayMs: Long) {
        cancelRunnable?.let { mainHandler.removeCallbacks(it) }
        val r = Runnable {
            NotificationManagerCompat.from(app).cancel(ID)
            cancelRunnable = null
        }
        cancelRunnable = r
        mainHandler.postDelayed(r, delayMs)
    }

    private fun ensureActionChannel() {
        val nm = NotificationManagerCompat.from(app)
        val existing = nm.getNotificationChannel(NotifChannel.Action.id)
        if (existing != null && existing.importance < NotificationManager.IMPORTANCE_DEFAULT) {
            nm.deleteNotificationChannel(NotifChannel.Action.id)
        }
        if (nm.getNotificationChannel(NotifChannel.Action.id) == null) {
            initChannel()
        }
    }

    private fun buildNotification(text: String, dismissMs: Long): Notification {
        val contentIntent = PendingIntent.getActivity(
            app,
            ID,
            Intent().apply {
                component = MainActivity::class.componentName
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val chipText = text.take(6).ifBlank { "触发" }
        val durationSec = (dismissMs / 1000L).toInt().coerceAtLeast(MIN_DURATION_SEC)
        // 优先走 Live Update（ColorOS 流体云）；失败则降级为普通 ongoing
        val notification = if (liveUpdateBuildSupport) {
            try {
                buildLiveUpdateLayer(text, chipText, contentIntent)
            } catch (t: Throwable) {
                LogUtils.d("ActionTipNotif live build failed: ${t.message}")
                buildBaseOngoing(text, contentIntent)
            }
        } else {
            buildBaseOngoing(text, contentIntent)
        }
        // 始终附加 HyperOS 超级岛 extras（ColorOS 忽略未知字段）
        attachHyperOsIslandExtras(notification, text, chipText, durationSec)
        return notification
    }

    /**
     * ColorOS / AOSP Live Update 层：ProgressStyle + promoted ongoing。
     */
    private fun buildLiveUpdateLayer(
        text: String,
        chipText: String,
        contentIntent: PendingIntent,
    ): Notification {
        val builder = Notification.Builder(app, NotifChannel.Action.id)
            .setSmallIcon(R.drawable.ic_status)
            .setContentTitle(META.appName)
            .setContentText(text)
            .setSubText("触发提示")
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(Notification.CATEGORY_STATUS)
            .setColorized(false)

        applyProgressStyleReflect(builder, text)
        invokeBuilderBool(builder, "setRequestPromotedOngoing", true)
        invokeBuilderString(builder, "setShortCriticalText", chipText)

        val notification = builder.build()
        notification.extras.putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true)
        return notification
    }

    private fun applyProgressStyleReflect(builder: Notification.Builder, text: String) {
        val styleClz = progressStyleClass
        if (styleClz == null) {
            builder.setStyle(Notification.BigTextStyle().bigText(text))
            return
        }
        try {
            val style = styleClz.getDeclaredConstructor().newInstance()
            styleClz.getMethod("setStyledByProgress", Boolean::class.javaPrimitiveType)
                .invoke(style, true)
            styleClz.getMethod("setProgress", Int::class.javaPrimitiveType)
                .invoke(style, 50)
            val segmentClz = Class.forName("android.app.Notification\$ProgressStyle\$Segment")
            val segment = segmentClz.getConstructor(Int::class.javaPrimitiveType)
                .newInstance(100)
            styleClz.getMethod("addProgressSegment", segmentClz).invoke(style, segment)
            try {
                val icon = Icon.createWithResource(app, R.drawable.ic_status)
                styleClz.getMethod("setProgressTrackerIcon", Icon::class.java)
                    .invoke(style, icon)
            } catch (_: Throwable) {
            }
            builder.setStyle(style as Notification.Style)
            builder.setCategory(Notification.CATEGORY_PROGRESS)
        } catch (t: Throwable) {
            LogUtils.d("ActionTipNotif ProgressStyle skip: ${t.message}")
            builder.setStyle(Notification.BigTextStyle().bigText(text))
        }
    }

    private fun invokeBuilderBool(builder: Notification.Builder, name: String, value: Boolean) {
        try {
            Notification.Builder::class.java
                .getMethod(name, Boolean::class.javaPrimitiveType)
                .invoke(builder, value)
        } catch (t: Throwable) {
            LogUtils.d("ActionTipNotif skip $name: ${t.message}")
        }
    }

    private fun invokeBuilderString(builder: Notification.Builder, name: String, value: String) {
        try {
            Notification.Builder::class.java
                .getMethod(name, String::class.java)
                .invoke(builder, value)
        } catch (t: Throwable) {
            LogUtils.d("ActionTipNotif skip $name: ${t.message}")
        }
    }

    /** 无 Live Update API 时的基线 ongoing 通知（仍可挂超级岛 extras） */
    private fun buildBaseOngoing(text: String, contentIntent: PendingIntent): Notification {
        val builder = NotificationCompat.Builder(app, NotifChannel.Action.id)
            .setSmallIcon(R.drawable.ic_status)
            .setContentTitle(META.appName)
            .setContentText(text)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSilent(true)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
        val notification = builder.build()
        notification.extras.putBoolean(EXTRA_REQUEST_PROMOTED_ONGOING, true)
        return notification
    }

    /**
     * HyperOS 超级岛层：官方摘要态模板 + miui.focus.pics。
     * 在 ColorOS 上写入无害，系统会忽略。
     */
    private fun attachHyperOsIslandExtras(
        notification: Notification,
        text: String,
        chipText: String,
        durationSec: Int,
    ) {
        try {
            val shortTitle = chipText.take(4).ifBlank { "触发" }
            val shortContent = text.take(4).ifBlank { shortTitle }
            val timeoutMin = maxOf(1, (durationSec + 59) / 60)
            val textInfo = JSONObject()
                .put("title", shortTitle)
                .put("content", shortContent)
                .put("useHighLight", false)
            val picInfo = JSONObject()
                .put("type", 1)
                .put("pic", "miui.focus.pic_imageText")
            val paramV2 = JSONObject()
                .put("protocol", 1)
                .put("business", "gkd_action_tip")
                .put("timeout", timeoutMin)
                .put("updatable", true)
                .put("islandFirstFloat", true)
                .put("enableFloat", true)
                .put("ticker", text.take(16).ifBlank { shortTitle })
                .put("tickerPic", "miui.focus.pic_ticker")
                .put("aodTitle", shortTitle)
                .put("aodPic", "miui.focus.pic_aod")
                .put(
                    "param_island",
                    JSONObject()
                        .put("islandProperty", 1)
                        .put("islandTimeout", durationSec)
                        .put(
                            "bigIslandArea",
                            JSONObject()
                                .put(
                                    "imageTextInfoLeft",
                                    JSONObject()
                                        .put("type", 1)
                                        .put("picInfo", picInfo)
                                        .put("textInfo", textInfo),
                                )
                                .put(
                                    "textInfo",
                                    JSONObject()
                                        .put("title", shortContent)
                                        .put("content", text.take(8)),
                                ),
                        )
                        .put(
                            "smallIslandArea",
                            JSONObject().put("picInfo", picInfo),
                        ),
                )
                .put(
                    "baseInfo",
                    JSONObject()
                        .put("title", META.appName)
                        .put("content", text.take(32))
                        .put("type", 1),
                )
            val pics = Bundle().apply {
                val icon = Icon.createWithResource(app, R.drawable.ic_status)
                putParcelable("miui.focus.pic_imageText", icon)
                putParcelable("miui.focus.pic_ticker", icon)
                putParcelable("miui.focus.pic_aod", icon)
            }
            notification.extras.putBundle("miui.focus.pics", pics)
            notification.extras.putString(
                "miui.focus.param",
                JSONObject().put("param_v2", paramV2).toString(),
            )
        } catch (t: Exception) {
            LogUtils.d("ActionTipNotif island extras skip: ${t.message}")
        }
    }

    /** HyperOS：1=OS1 / 2=OS2 / 3=OS3；0=不支持或未知 */
    private fun readFocusProtocolVersion(): Int = try {
        Settings.System.getInt(app.contentResolver, "notification_focus_protocol", 0)
    } catch (_: Throwable) {
        0
    }

    /** HyperOS：是否允许展示焦点通知 / 超级岛 */
    private fun readCanShowFocus(): Boolean? = try {
        val uri = Uri.parse("content://miui.statusbar.notification.public")
        val extras = Bundle().apply { putString("package", app.packageName) }
        val bundle = app.contentResolver.call(uri, "canShowFocus", null, extras)
        bundle?.getBoolean("canShowFocus", false)
    } catch (_: Throwable) {
        null
    }
}
