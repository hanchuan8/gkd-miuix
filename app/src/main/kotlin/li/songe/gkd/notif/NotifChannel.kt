package li.songe.gkd.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import li.songe.gkd.META
import li.songe.gkd.app

sealed class NotifChannel(
    val id: String,
    val name: String? = null,
    val desc: String? = null,
    val importance: Int = NotificationManager.IMPORTANCE_LOW,
) {
    data object Default : NotifChannel(
        id = "0",
    )

    data object Snapshot : NotifChannel(
        id = "1",
        name = "保存快照通知",
    )

    /** 触发提示 / 实时通知（默认重要性，便于系统与厂商提升为岛/胶囊） */
    data object Action : NotifChannel(
        id = "2",
        name = "触发提示",
        desc = "规则触发时的实时状态通知，可显示在状态栏芯片或厂商灵动岛",
        importance = NotificationManager.IMPORTANCE_DEFAULT,
    )
}

fun initChannel() {
    val channels = arrayOf(
        NotifChannel.Default,
        NotifChannel.Snapshot,
        NotifChannel.Action,
    )
    val manager = NotificationManagerCompat.from(app)
    // delete old channels
    manager.notificationChannels.filter { channels.none { c -> c.id == it.id } }.forEach {
        manager.deleteNotificationChannel(it.id)
    }
    // create/update new channels
    channels.forEach {
        val channel = NotificationChannel(
            it.id,
            it.name ?: META.appName,
            it.importance,
        ).apply {
            description = it.desc
        }
        manager.createNotificationChannel(channel)
    }
}
