package com.imcorp.animeprog.DownloadManager.DownloadService

import android.content.Intent
import android.os.*
import android.util.Log
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.ParcelableSparseIntArray
import com.imcorp.animeprog.Default.SimpleService
import com.imcorp.animeprog.Default.indexOfSet
import com.imcorp.animeprog.DownloadManager.DownloadWorker.Worker
import com.imcorp.animeprog.DownloadManager.RecycleViewAdapter.EpisodeSelected
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import java.util.*
typealias OnUpdateProgress = (instance: DownloadService.DownloadingInstance) -> Unit

class DownloadService : SimpleService() {
    private var binder: Callback? = null
    private val downloadItems = HashMap<Int, DownloadingInstance>()
    private fun onCallback(intent: Intent): Int {
        val ID = intent.getIntExtra(Config.DOWNLOADS_FRAGMENT_ID, 0)
        val item = downloadItems[ID]
        if (item != null) {
            when (Objects.requireNonNull(intent.action)) {
                Config.CANCEL_BUTTON_CLICKED -> if (item.worker != null) item.worker!!.notifyCancelButtonClicked()
                Config.PAUSE_BUTTON_CLICKED -> if (item.worker != null) item.worker!!.notifyPauseButtonClicked()
            }
        } else stopService(ID)
        return START_STICKY
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        //if notification callback
        if (intent.action != null) return onCallback(intent)
        if (intent.extras == null) return START_STICKY
        val thread = HandlerThread(Config.DOWNLOADS_SERVICE_NAME, Process.THREAD_PRIORITY_BACKGROUND)
        thread.start()
        val instance = DownloadingInstance(
                startId,
                thread,
                intent.extras!!.getParcelable(Config.FRAGMENT_ANIME_OBJ),
                downloadItems.size == 0
        )
        downloadItems[startId] = instance
        instance.map = intent.extras!!.getParcelable(Config.EPISODES_MAP)
        if (instance.map == null || instance.anime == null) return START_NOT_STICKY
        val msg = instance.serviceHandler.obtainMessage(startId)
        instance.serviceHandler.sendMessage(msg)
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        //this.unregisterReceiver(receiver);
        dataBase.close()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): Callback? {
        return Callback().also { binder = it }
        //return binder;
    }

    override fun onRebind(intent: Intent) {
        binder = Callback()
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        binder = null
        return super.onUnbind(intent)
    }

    inner class ServiceHandler internal constructor(looper: Looper?) : Handler(looper!!) {
        private var id = 0
        private val downloadInstance: DownloadingInstance?
            private get() = downloadItems[id]

        override fun handleMessage(msg: Message) {
            id = msg.what
            startThread()
        }

        private fun startThread() {
            val instance = downloadInstance
            if (instance == null || instance.worker != null) {
                if (Config.NEED_LOG) Log.e(Config.DOWNLOAD_SERVICE_TAG, "Instance is null or worker is null")
                return
            }
            instance.worker = Worker(
                    instance.map!!.arr,
                    this@DownloadService,
                    instance
            )
            try {
                instance.worker!!.start()
            } catch (ignored: InterruptedException) {
                if (Config.NEED_LOG) Log.e(Config.DOWNLOAD_SERVICE_TAG, "Interruped", ignored)
            } catch (e: Exception) {
                if (Config.NEED_LOG) Log.e(Config.DOWNLOAD_SERVICE_TAG, "Undefined error: " + e.message, e)
            }
            stop()
        }

        private fun stop() {
            val instance = downloadInstance
            if (instance!!.worker != null) {
                when (instance.worker!!.success) {
                    Worker.WorkerState.SUCCESS -> {
                        val count_and_length = dataBase.downloads.getDownloadsC(instance.anime)
                        instance.downloadNotification.showSuccessDownload(instance.anime!!.title, count_and_length[0], count_and_length[1])
                    }
                    Worker.WorkerState.CANCELED -> instance.downloadNotification.showInvalidDownload(true)
                    else -> instance.downloadNotification.showInvalidDownload(false)
                }
            } else {
                instance.downloadNotification.showInvalidDownload(false)
            }
            stopService(id)
            //            startForeground();
        }
    }

    private fun stopService(id: Int) {
        threadCallback.post {
            val item = downloadItems.remove(id)
            if (binder != null && binder!!.onDownloadFinished != null) binder!!.onDownloadFinished!!(item)
            if (downloadItems.size == 0) stopSelf() else downloadItems.values.iterator().next().isMainNotification = true
        }
    }

    inner class Callback : Binder() {
        var updateProgress: OnUpdateProgress? = null
        var updateDownloadCount: OnUpdateProgress? = null
        var onDownloadFinished: ((instance: DownloadingInstance?)->Any)? = null
            private set
        val items: Array<DownloadingInstance?> get() = downloadItems.values.toTypedArray()

        fun setOnDownloadFinished(listener:  ((instance: DownloadingInstance?)->Any)?) {
            onDownloadFinished = listener
        }

        fun setOnUpdateProgress(listener: OnUpdateProgress?) {
            updateProgress = listener
        }

        fun setOnUpdateDownloadCount(listener: OnUpdateProgress?) {
            updateDownloadCount = listener
        }
    }
    inner class DownloadingInstance(var ID: Int, thread: HandlerThread, anime: OneAnime?, isMainNotification: Boolean) {
        @JvmField
        var anime: OneAnime?
        var map: ParcelableSparseIntArray<EpisodeSelected>? = null
        var worker: Worker? = null
        @JvmField
        var downloadNotification: DownloadNotification
        var serviceHandler: ServiceHandler
        @JvmField
        var serviceLooper: Looper
        @JvmField
        var isMainNotification: Boolean
        val service: DownloadService
            get() = this@DownloadService
        val notificationId: Int
            get() = Config.DOWNLOAD_NOTIFICATION_ID + indexOfSet(downloadItems.keys, ID)

        fun updateProgress(progress: Long, size: Long) {
            this.progress = progress
            maxProgress = size
            if (binder != null && binder!!.updateProgress != null) binder!!.updateProgress!!(this)
            downloadNotification.updateProgress(progress, size)
        }

        fun updateDownloadCount(count: Int, download_count: Int) {
            currentCount = (count - 1).toLong()
            //this.maxCount =download_count;
            if (binder != null && binder!!.updateDownloadCount != null) binder!!.updateDownloadCount!!(this)
            downloadNotification.updateDownloadCount(anime!!, count, download_count)
        }

        val maxCount: Int
            get() = map!!.arr.size()
        var currentCount: Long = 0
        var progress: Long = 0
        var maxProgress: Long = 0

        init {
            serviceLooper = thread.looper
            serviceHandler = ServiceHandler(serviceLooper)
            downloadNotification = DownloadNotification(this)
            this.anime = anime
            this.isMainNotification = isMainNotification
        }
    }

}