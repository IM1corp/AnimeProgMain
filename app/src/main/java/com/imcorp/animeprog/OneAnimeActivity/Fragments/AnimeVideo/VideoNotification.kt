package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.KITKAT
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Config.VIDEO_PLAYER_CHANNEL_ID
import com.imcorp.animeprog.Default.LoadImageEvents
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.Video.OneVideo


class VideoNotification(private val fragmentVideoPlayer: FragmentVideoPlayer) {
    init{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val description: String = context.getString(R.string.video_player_notif_desc);//getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(VIDEO_PLAYER_CHANNEL_ID, context.getString(R.string.video_player_notif), importance)
            channel.description = description
            channel.setShowBadge(false)
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager: NotificationManager = myApp.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

    }
    private var bitmap:Bitmap?=null
    private var isPlaying: Boolean=false
    private var title:String?=null
    private var description:String?=null
    private val context get() = fragmentVideoPlayer.requireContext()
    private val myApp get() = (fragmentVideoPlayer.activity as MyApp)
    private val onImageLoaded = object :LoadImageEvents{
        override fun onSuccess(bitmap: Bitmap?) {
            update(bitmap = bitmap)
        }
        //Do nothing: user will request to load cover again
        override fun onFail(exception: Exception?):Boolean =false
    }
    private val notification: NotificationCompat.Builder get(){
        val mediaStyle = androidx.media.app.NotificationCompat.MediaStyle().setShowActionsInCompactView(0, 1, 2)
        fragmentVideoPlayer.videoView.mediaSession?.sessionToken?.let{
            mediaStyle.setMediaSession(it)
        }

        return NotificationCompat.Builder(context, VIDEO_PLAYER_CHANNEL_ID)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setStyle(mediaStyle)
                .setContentTitle(title)
                .setContentText(description)
                .setLargeIcon(bitmap)
                .apply{if(SDK_INT > KITKAT)setSmallIcon(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)}
                .setContentIntent(PendingIntent.getActivity(context, 4, Intent(context, OneAnimeActivity::class.java),
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE))
                .addAction(if(SDK_INT > KITKAT)R.drawable.ic_back else R.mipmap.animego_icon, context.getString(R.string.back),
                        PendingIntent.getActivity(context, 1,
                                Intent(context, OneAnimeActivity::class.java).setAction(Config.BACK_BUTTON_CLICKED)
                                .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                , PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                )
                .addAction(if(SDK_INT > KITKAT)(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play) else R.mipmap.animego_icon, context.getString(R.string.pause),
                        PendingIntent.getActivity(context, 2,
                                Intent(context, OneAnimeActivity::class.java)
                                        .setAction(Config.PAUSE_BUTTON_CLICKED)
                                        .putExtra(Config.IS_PAUSE_EVENT, this.isPlaying)
                                //.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                , PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                )
                .addAction(if(SDK_INT > KITKAT) R.drawable.ic_next else R.mipmap.animego_icon, context.getString(R.string.next),
                        PendingIntent.getActivity(context, 3,
                                Intent(context, OneAnimeActivity::class.java).setAction(Config.NEXT_BUTTON_CLICKED)
                                //.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                                , PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
                )
    }
    fun update(anime: OneAnime? = null, episode: OneVideo? = null, isPlaying: Boolean? = null, bitmap: Bitmap? = null) {
        anime?.let{
            title=anime.title
            anime.loadCover(myApp, myApp.request, onImageLoaded)
        }
        episode?.let{
            description=("${context.getString(R.string.one_episode)} ${episode.num}\n${context.getString(R.string.player)} ${episode.player}\n" +
                    "${context.getString(R.string.voice_studio)} ${episode.voiceStudio}")
        }
        isPlaying?.let{this.isPlaying=isPlaying}
        bitmap?.let{this.bitmap=bitmap}
        showNotification()
    }
    private fun showNotification(){
        NotificationManagerCompat.from(context).notify(Config.VIDEO_PLAYER_NOTIFICATION_ID, notification.build())
    }

    fun hideNotification() =NotificationManagerCompat.from(context).cancel(Config.VIDEO_PLAYER_NOTIFICATION_ID)

}