package com.imcorp.animeprog.MainActivity.fragments.downloads.current

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.DownloadManager.DownloadService.DownloadService
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.downloads.DownloadsFragment
import com.imcorp.animeprog.MainActivity.fragments.downloads.SimpleDownloadFragment
import com.imcorp.animeprog.R

class CurrentDownloadingFragment : SimpleDownloadFragment() {
    private val serviceConnection=object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            when(name.className){
                DownloadService::class.qualifiedName-> {
                    downloadServiceCallback = binder as DownloadService.Callback
                }
            }
        }
        override fun onServiceDisconnected(name: ComponentName) {
            when(name.className){
                DownloadService::class.qualifiedName-> {
                    downloadServiceCallback=null
                }
            }
        }
    }
    private var downloadServiceCallback: DownloadService.Callback?=null; set(v){ field=v;updateServiceState()}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //requireActivity().bindService(Intent(context, DownloadService::class.java), this.serviceConnection, 0)
        view.findViewById<RecyclerView>(R.id.itemsRecycleView).apply{
            layoutManager = LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            adapter = CurrentDownloadsAdapter(requireActivity() as MainActivity)
        }
        updateServiceState()
        bindService()
    }
    override fun onDestroyView() {
        if(downloadServiceCallback!=null) unbindService()
        super.onDestroyView()
    }
    private fun updateServiceState(){
        val adapter = (view?.findViewById<RecyclerView>(R.id.itemsRecycleView)?.adapter as? CurrentDownloadsAdapter)
        if(downloadServiceCallback!=null && adapter != null) with(downloadServiceCallback!!) {
            adapter.dataItems.apply {
                clear()
                addAll(items.map { inst -> CurrentDownloadsAdapter.Structure(inst,
                currentProgress = inst!!.progress,maxProgress = inst.maxProgress,downloadedItemsCount = inst.maxCount.toLong(),currentDownloadItemsCount = inst.currentCount) })
            }
            adapter.notifyDataSetChanged()
            setOnDownloadFinished { instance ->
                for ((i, item) in adapter.dataItems.withIndex()) {
                    if (item.downloadingInstance === instance) {
                        adapter.dataItems.removeAt(i)
                        break
                    }
                }
               adapter.notifyDataSetChanged()
            }
            setOnUpdateProgress { instance ->
                adapter.dataItems.firstOrNull {it.downloadingInstance===instance }?.apply {
                    synchronized(this){
                        this.currentProgress=instance.progress
                        this.maxProgress=instance.maxProgress
                    }
                }
            }
            setOnUpdateDownloadCount { instance ->
                adapter.dataItems.firstOrNull {it.downloadingInstance===instance }?.apply {
                    synchronized(this){
                        this.downloadedItemsCount=instance.maxCount.toLong()
                        this.currentDownloadItemsCount=instance.currentCount
                    }
                }

            }
            (parentFragment as? DownloadsFragment)?.adapterFragments?.updateBadge(this@CurrentDownloadingFragment, adapter.dataItems.size)
            showNothingFound(false)
        }
        else {
            adapter?.dataItems?.clear()
            adapter?.notifyDataSetChanged()
            (parentFragment as? DownloadsFragment)?.adapterFragments?.updateBadge(this@CurrentDownloadingFragment,0)
            showNothingFound()
        }
    }

    private fun bindService()=requireActivity().bindService(Intent(context, DownloadService::class.java), this.serviceConnection, 0)
    private fun unbindService(){
        //requireActivity().unbindService(this.serviceConnection)
        this.downloadServiceCallback?.setOnDownloadFinished(null)
        this.downloadServiceCallback?.setOnUpdateProgress(null)
        this.downloadServiceCallback?.setOnUpdateDownloadCount(null)
        this.downloadServiceCallback=null
        updateServiceState()
    }
}