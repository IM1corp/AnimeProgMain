package com.imcorp.animeprog.MainActivity.fragments.downloads.current

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Config.getSizeFromByte
import com.imcorp.animeprog.Default.LoadImageEvents
import com.imcorp.animeprog.DownloadManager.DownloadService.DownloadService
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.DownloadPreviewItemBinding

class CurrentDownloadsAdapter(private val activity: MainActivity) : RecyclerView.Adapter<CurrentDownloadsAdapter.OneItem>() {

    public val dataItems:ArrayList<Structure> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = OneItem(
        DownloadPreviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//            .inflate(R.layout.download_preview_item, parent, false)
    )
    override fun onBindViewHolder(holder: CurrentDownloadsAdapter.OneItem, position: Int) = holder.bind(position)
    override fun getItemCount() = dataItems.size
    override fun onViewRecycled(holder: OneItem) {
        super.onViewRecycled(holder)
        holder.removeCallback()
    }
    inner class OneItem(private val binding: DownloadPreviewItemBinding) : RecyclerView.ViewHolder(binding.root){
        private var elPos:Int=-1
        private val structure:Structure get() = dataItems[elPos]
        fun bind(position: Int) {
            this.elPos = position
            structure.downloadingInstance!!.anime!!.loadCover(activity, activity.request, object : LoadImageEvents {
                override fun onSuccess(bitmap: Bitmap?) = binding.imageView.setImageBitmap(bitmap)
                override fun onFail(exception: Exception?) = true
            })
            this.binding.titleTextView.text = structure.downloadingInstance!!.anime!!.title
            this.binding.showMore.setOnClickListener {
                binding.recyclerViewDownload.visibility = when( binding.recyclerViewDownload.visibility ){
                    View.GONE -> {
                        it.rotation = 270f
                        View.VISIBLE
                    }
                    else->{
                        it.rotation = 90f
                        View.GONE
                    }
                }
            }
            with(binding.recyclerViewDownload){
                layoutManager = LinearLayoutManager(activity,LinearLayoutManager.VERTICAL,false)
                adapter = CurrentDownloadProgressAdapter(activity).apply {
                    data.addAll((0 until structure.downloadingInstance!!.maxCount).map{ CurrentDownloadProgressAdapter.SmallProgressItem() })
                    notifyDataSetChanged()
                }
            }
            this.itemView.setOnClickListener {
                activity.showOneAnimeWindow(structure.downloadingInstance!!.anime)
            }
            runnableCallback.run()
        }
        @SuppressLint("SetTextI18n")
        fun updateProgress() {
            val struct = this.structure
            synchronized(struct) {
                this.binding.progressBarMain.apply {
                    max = (struct.maxProgress/1000).toInt()
                    progress= (struct.currentProgress/1000).toInt()
                }
                val speed = (struct.currentProgress - struct.lastProgress) * Config.DOWNLOADS_VIEW_UPDATE_PROGRESS_TIME_MS / 1000.0
                val proc = (struct.currentProgress.toFloat() / struct.maxProgress * 100).toInt().toString();
                this.binding.textViewProgress.text = "${struct.currentDownloadItemsCount} ${itemView.context.getString(R.string.from)} ${struct.downloadedItemsCount}\n" +
                        Config.getText(itemView.context,R.string.notification_download_progress,
                                getSizeFromByte(struct.currentProgress.toDouble()), getSizeFromByte(struct.maxProgress.toDouble()),getSizeFromByte(speed))+
                        "\n${proc}%"
                (this.binding.recyclerViewDownload.adapter as CurrentDownloadProgressAdapter).apply{
                    val pos = struct.currentDownloadItemsCount .coerceAtLeast(0).toInt()
                    data[pos].currentProgress = struct.currentProgress
                    data[pos].maxProgress = struct.maxProgress
                    (binding.recyclerViewDownload.findViewHolderForAdapterPosition(pos) as? CurrentDownloadProgressAdapter.ProgressItem)?.update()
                }
                struct.lastProgress = struct.currentProgress
            }
        }
        fun removeCallback()=activity.threadCallback.removeCallbacks(runnableCallback)
        private val runnableCallback = object: Runnable{
            override fun run() {
                if(dataItems.size>elPos) {
                    updateProgress()
                    activity.threadCallback.removeCallbacks(this)
                    activity.threadCallback.postDelayed(this, Config.DOWNLOADS_VIEW_UPDATE_PROGRESS_TIME_MS.toLong())
                }
            }
        }
    }
    class Structure(
            public var downloadingInstance: DownloadService.DownloadingInstance? = null,
            public var currentProgress: Long = 0,
            public var lastProgress: Long = 0,
            public var maxProgress: Long = 1,

            public var downloadedItemsCount: Long = 0,
            public var currentDownloadItemsCount: Long = 0,
    )
}