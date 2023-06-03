package com.imcorp.animeprog.DownloadManager.DownloadService

import android.R
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.SimpleService
import com.imcorp.animeprog.DownloadManager.DownloadService.DownloadService.DownloadingInstance
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import java.util.*

@SuppressLint("RestrictedApi")
class DownloadNotification(private val instance: DownloadingInstance) {
    private val notificationManager: NotificationManagerCompat
    private var builder: NotificationCompat.Builder
    private val context: SimpleService
    private val process = TimeProcess()
    fun show(): DownloadNotification {
        builder.setContentText(context.getString(com.imcorp.animeprog.R.string.notification_get_info))
                .setSmallIcon(R.drawable.stat_sys_download)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
        setActions()
        showNotification(true)
        return this
    }

    private fun setActions() {
        if (builder.mActions.size == 2) return  // if more than two - nothing to add
        val content_intent = Intent(context, DownloadService::class.java)
                .setAction(Config.PAUSE_BUTTON_CLICKED)
                .putExtra(Config.DOWNLOADS_FRAGMENT_ID, instance.ID)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val cancel_intent = Intent(context, DownloadService::class.java)
                .setAction(Config.CANCEL_BUTTON_CLICKED)
                .putExtra(Config.DOWNLOADS_FRAGMENT_ID, instance.ID)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val pausePendingIntent = PendingIntent.getService(context, 0, content_intent, PendingIntent.FLAG_MUTABLE)
        val cancelPendingIntent = PendingIntent.getService(context, 0, cancel_intent, PendingIntent.FLAG_MUTABLE)
        builder.addAction(0, context.getString(com.imcorp.animeprog.R.string.pause), pausePendingIntent)
        builder.addAction(0, context.getString(com.imcorp.animeprog.R.string.cancel), cancelPendingIntent)
    }

    fun updateDownloadCount(anime: OneAnime, count: Int, download_count: Int) {
        runInUiThread {
            builder.setNumber(download_count - count)
            builder.setContentTitle(context.getString(com.imcorp.animeprog.R.string.notification_title, anime.title, count, download_count))
            showNotification(true)
        }
    }

    fun updateProgress(now_: Long, max_: Long) {
        runInUiThread {
            var now = now_
            var max = max_
            if (!process.updateTime(now, max)) return@runInUiThread
            while (max > Int.MAX_VALUE) {
                max /= 1000
                now /= 1000
            }
            builder.setProgress(max.toInt(), now.toInt(), false)
            showNotification(true)
        }
    }

    fun showSuccessDownload(name: String?, download_c: Int, count: Int) {
        runInUiThread {
            context.stopForeground(true)
            val content = context.getString(com.imcorp.animeprog.R.string.success_download_content, name, download_c, count)
            builder = NotificationCompat.Builder(context, Config.DOWNLOADS_CHANNEL_ID)
                    .setContentTitle(context.getString(com.imcorp.animeprog.R.string.success_download_title))
                    .setContentText(content)
                    .setAutoCancel(false)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(content))
                    .setDeleteIntent(PendingIntent.getService(context, 0,
                            Intent(context, DownloadService::class.java)
                                    .setAction(Config.DESTROY_SERVICE)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_MUTABLE)
                    ).setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setOngoing(false)
                    .setSmallIcon(com.imcorp.animeprog.R.drawable.ic_download_grey)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
            showNotification(false)
        }
    }

    //todo: show better notification
    private val finalNotif: Unit
        private get() {
            //todo: show better notification
        }

    fun showInvalidDownload(canceled: Boolean) {
        runInUiThread {
            val msg = Config.getText(context, if (canceled) com.imcorp.animeprog.R.string.download_canceled_content else com.imcorp.animeprog.R.string.invalid_download_content)
            context.stopForeground(true)
            builder = NotificationCompat.Builder(context, Config.DOWNLOADS_CHANNEL_ID)
                    .setContentTitle(context.getString(com.imcorp.animeprog.R.string.invalid_download_title))
                    .setContentText(msg)
                    .setAutoCancel(false).setDeleteIntent(PendingIntent.getService(context, 0,
                            Intent(context, DownloadService::class.java)
                                    .setAction(Config.DESTROY_SERVICE)
                                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK), PendingIntent.FLAG_MUTABLE)
                    ).setVibrate(longArrayOf(1000, 1000, 1000, 1000, 1000))
                    .setOngoing(false)
                    .setSmallIcon(com.imcorp.animeprog.R.drawable.ic_download_grey)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
            showNotification(false)
        }
    }

    private fun showNotification(service_one: Boolean) {
        if (service_one && instance.isMainNotification) context.startForeground(instance.notificationId, builder.build()) else notificationManager.notify(instance.notificationId, builder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelName: String) {
        val chan = NotificationChannel(Config.DOWNLOADS_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = context.getColor(com.imcorp.animeprog.R.color.colorPrimary) //TODO: change color
        chan.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        chan.description = channelName
        val service = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service?.createNotificationChannel(chan)
    }

    fun onPause(paused: Boolean) {
        runInUiThread {
            builder.mActions[0].title = if (paused) context.getString(com.imcorp.animeprog.R.string.resume) else context.getString(com.imcorp.animeprog.R.string.pause)
            showNotification(true)
        }
    }

    private inner class TimeProcess internal constructor() {
        private var lastC: Long = -1
        private var lastTime: Long = 0
        fun updateTime(now: Long, max: Long): Boolean {
            val time_now = Calendar.getInstance().timeInMillis
            if (lastC == -1L) {
                lastC = now
                lastTime = time_now
                return true
            }
            if (time_now - lastTime >= Config.DOWNLOADS_NOTIF_UPDATE_PROGRESS_TIME_MS || now < lastC) {
                //сколько байт загружается за 1 секунду.
                val download_byte_for_one_sec = (now - lastC) * (1000.0 / Config.DOWNLOADS_NOTIF_UPDATE_PROGRESS_TIME_MS)
                val msg: CharSequence = context.getString(com.imcorp.animeprog.R.string.notification_download_progress,
                        Config.getSizeFromByte(now.toDouble()), Config.getSizeFromByte(max.toDouble()), Config.getSizeFromByte(download_byte_for_one_sec))
                val smallMsg: CharSequence = (now.toFloat() / max * 100).toInt().toString()+"%"
                builder.setContentText(smallMsg)
                        .setStyle(NotificationCompat.BigTextStyle().bigText(msg))
                lastTime = time_now
                lastC = now
                return true
            }
            return false
        }
    }

    private fun runInUiThread(r: Runnable) {
        if (Looper.myLooper() != Looper.getMainLooper()) context.threadCallback.post(r) else r.run()
    }

    init {
        notificationManager = NotificationManagerCompat.from(instance.service)
        builder = NotificationCompat.Builder(instance.service, Config.DOWNLOADS_CHANNEL_ID)
        context = instance.service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context.getString(com.imcorp.animeprog.R.string.download_anime_service))
        }
    }
}