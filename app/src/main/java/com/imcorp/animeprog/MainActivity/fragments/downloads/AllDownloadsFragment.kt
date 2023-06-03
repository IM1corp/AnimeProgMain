package com.imcorp.animeprog.MainActivity.fragments.downloads

import android.os.AsyncTask
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.AnimePreviewItem
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.AnimePreviewItem.*
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.Events
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime

class AllDownloadsFragment() : SimpleDownloadFragment(R.string.down_nothing_found_all), Events {

    private lateinit var adapter: AnimePreviewItem
    private val activity: MainActivity get() = getActivity() as MainActivity

    private val itemsRecycleView:RecyclerView get() = requireView().findViewById(R.id.itemsRecycleView)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun refresh() {
            adapter.clear()
            adapter.fullyLoaded = false
            AsyncTask.execute { loadMore(0) }
        }

        with(itemsRecycleView) {
            layoutManager=LinearLayoutManager(context,LinearLayoutManager.VERTICAL,false)
            setHasFixedSize(false)
            adapter = AnimePreviewItem(activity, ENABLE_REORDER or SHOW_PROGRESS or
                                SHOW_DOWNLOAD_COUNT or SHOW_MENU_BUTTON or ENABLE_DELETING_FROM_MENU or SHOW_HOST_IMAGE_VIEW,
                    itemsRecycleView, findViewById(R.id.nestedSrollView))
                    .setEvents(this@AllDownloadsFragment)
                    .setOnBind { model: AnimePreviewItem.FavoriteItemModel ->
                        model.itemView.findViewById<TextView>(R.id.downloadsCount).text = SpannableStringBuilder()
                                .bold{append(model.anime.epDownloaded.toString())}
                                .append('\n')
                                .append(getString(R.string.downloads_out_of))
                                .append('\n')
                                .bold { append(model.anime.epCount.toString()) }
                    }
                    .also { this@AllDownloadsFragment.adapter = it }
            //isDrawingCacheEnabled = true;drawingCacheQuality = View.DRAWING_CACHE_QUALITY_HIGH
        }
        refresh()

    }

    override fun onItemClick(anime: OneAnime.OneAnimeWithId, index: Int) = with(activity) {
        dataBase.downloads.loadVideosToAnime(anime)
        showOneAnimeWindow(anime.anime)
    }
    override fun onErrorLoadingImage(e: Exception) =activity.showNoInternetException() //TODO: change
    override fun loadMore(offset: Int): Boolean = with(activity) {
        try {
            val items = dataBase.downloads.getDownloads(Config.DOWNLOADS_LOAD_COUNT, offset)
            threadCallback.post {
                adapter.addItems(items)
                (parentFragment as? DownloadsFragment)?.adapterFragments?.updateBadge(this@AllDownloadsFragment, items.size)
            }
            if (items.size == 0) super.showNothingFound(true)
            else super.showNothingFound(false)
        }catch(e: IllegalStateException) {
            Log.e("DownloadsError", e.message, e);

        }
        return true//items.size == 0
    }
    override fun onItemMove(fromPosition: Int, toPosition: Int) = AsyncTask.execute {
        activity.dataBase.downloads.moveDownloadItem(fromPosition, toPosition)
    }
    override fun onItemDelete(position: Int) {
        class DeleteAnimeActions(anime: OneAnime.OneAnimeWithId, pos: Int) : MaterialAlertDialogBuilder(requireContext()) {
            private var onSuccess: Runnable?=null
            private val anime: OneAnime = anime.anime
            private val id: Int = anime.id
            private val index: Int = pos
            fun setData() : DeleteAnimeActions{
                val s = Config.getText(activity, R.string.shure_delete_anime_desc, anime.title)
                setIcon(R.drawable.ic_error)
                setTitle(R.string.shure_delete_anime)
                setMessage(s)
                setPositiveButton(R.string.delete) { dialog, _ ->
                    dialog.dismiss()
                    activity.dataBase.downloads.deleteAnimeFromDownloads(id, anime)
                    onSuccess?.run()
                }
                setNegativeButton(R.string.cancel) { dialog, _ ->
                    adapter.items.add(index, OneAnime.OneAnimeWithId(id, anime))
                    adapter.notifyItemInserted(index)
                    dialog.dismiss()
                }
                return this
            }
            fun setOnSuccessListener(runnable: Runnable): DeleteAnimeActions {this.onSuccess = runnable;return this}
        }
        DeleteAnimeActions(adapter.items[position], position)
                .setData()
                .setOnSuccessListener{
                    (parentFragment as? DownloadsFragment)?.adapterFragments?.updateBadge(this@AllDownloadsFragment,adapter.itemCount-1)
                    if(adapter.itemCount-1==0)super.showNothingFound(true)
                }
                .show()
    }
}