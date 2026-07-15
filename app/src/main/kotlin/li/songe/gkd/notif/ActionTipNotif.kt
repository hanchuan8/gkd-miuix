package li.songe.gkd.notif

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
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
 * 触发提示「实时通知」。
 *
 * Google Live Update API 在一部分国产 Android 16 上 [SDK_INT] 已到 36，但 framework
 * 可能缺方法（如 isRequestPromotedOngoing），直接调用会 NoSuchMethodError。
 * 因此全部经反射探测，缺啥就跳过，保证至少能发出普通 ongoing 通知。
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

    /** 设备 framework 是否具备 Live Update 构建能力（与 SDK_INT 解耦） */
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
        LogUtils.d(
            "ActionTipNotif liveBuild=$liveUpdateBuildSupport " +
                "canPostPromoted=$canPromote promotable=$promotable",
        )
        @SuppressLint("MissingPermission")
        NotificationManagerCompat.from(app).notify(ID, notification)
        scheduleCancel(dismissMs)

        val tips = when {
            !liveUpdateBuildSupport ->
                "已发送状态通知（本机 framework 不完整，无法走 Google Live Update API）"
            canPromote == false ->
                "系统未允许本应用「实时更新」，请在通知设置中开启"
            promotable == false ->
                "通知可能未满足 Live Update 特征；仍已发送状态通知"
            canPromote == true || promotable == true ->
                "已请求 Live Update（状态栏芯片取决于系统与厂商实现）"
            else ->
                "已发送实时/状态通知"
        }
        return PostResult(
            posted = true,
            canPostPromoted = canPromote,
            hasPromotableCharacteristics = promotable,
            message = tips,
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
        return if (liveUpdateBuildSupport) {
            try {
                buildLiveUpdateSafe(text, chipText, contentIntent, durationSec)
            } catch (t: Throwable) {
                LogUtils.d("ActionTipNotif live build failed: ${t.message}")
                buildCompat(text, contentIntent, durationSec)
            }
        } else {
            buildCompat(text, contentIntent, durationSec)
        }
    }

    /**
     * 仅通过反射调用 Live Update Builder API，避免 OEM 缺方法时直接 linkage 崩溃。
     */
    private fun buildLiveUpdateSafe(
        text: String,
        chipText: String,
        contentIntent: PendingIntent,
        durationSec: Int,
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
        attachOemIslandExtras(notification, text, chipText, durationSec)
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

    private fun buildCompat(text: String, contentIntent: PendingIntent, durationSec: Int): Notification {
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
        attachOemIslandExtras(notification, text, text.take(6), durationSec)
        return notification
    }

    private fun attachOemIslandExtras(
        notification: Notification,
        text: String,
        chipText: String,
        durationSec: Int,
    ) {
        try {
            val timeoutMin = maxOf(1, (durationSec + 59) / 60)
            val island = JSONObject()
                .put("business", "gkd_action_tip")
                .put("timeout", timeoutMin)
                .put("updatable", true)
                .put("islandFirstFloat", false)
                .put("enableFloat", false)
                .put(
                    "param_island",
                    JSONObject()
                        .put("islandTimeout", durationSec)
                        .put(
                            "smallIslandArea",
                            JSONObject().put(
                                "textPicArea",
                                JSONObject()
                                    .put("title", chipText)
                                    .put("desc", text.take(32)),
                            ),
                        )
                        .put(
                            "bigIslandArea",
                            JSONObject().put(
                                "textArea",
                                JSONObject()
                                    .put("title", META.appName)
                                    .put("content", text),
                            ),
                        ),
                )
                .put(
                    "baseInfo",
                    JSONObject()
                        .put("title", META.appName)
                        .put("content", text),
                )
            notification.extras.putString(
                "miui.focus.param",
                JSONObject().put("param_v2", island).toString(),
            )
        } catch (_: Exception) {
        }
    }
}
