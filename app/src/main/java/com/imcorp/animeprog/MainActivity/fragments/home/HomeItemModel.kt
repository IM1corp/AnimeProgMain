package com.imcorp.animeprog.MainActivity.fragments.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.AnimePreviewItem
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.Events
import com.imcorp.animeprog.MainActivity.fragments.Adapters.smallCardItems.HorizontalAnimeCardsAdapter
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.Requests.JsonObj.mainPage.Container
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList
import com.imcorp.animeprog.databinding.HomeOneRecycleviewBinding

class HomeItemModel(val itemView: View, val activity: MainActivity) {
    private val binding = HomeOneRecycleviewBinding.bind(itemView)
    private val titleView: TextView = binding.titleTextView
    private val recycleView: RecyclerView = binding.recyclerView
    private fun onItemClick(item: OneAnime) = activity.showOneAnimeWindow(item)
    private fun setTitle(link: OneAnime.Link){
        if(!link.link.isNullOrBlank())
            Config.setTextViewLink(titleView,link.title){
                activity.loadFromLink(link)
            }
        else titleView.text = link.title
    }
    fun bindWithContainer(container: Container){
        //val g = 10;
        setTitle(container.title)
        with(recycleView){
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
            adapter = HomeContainerAdapter(activity as MyApp)
                    .apply { data = container.list }
        }
    }
    fun bindWithRowList(row: RowList) {
        setTitle(row.title)
        with(recycleView){
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
            adapter = HorizontalAnimeCardsAdapter(activity as MyApp,true)
                    .setOnClickListener(::onItemClick)
                    .addItems(row.list)
        }
    }
    fun bindWithBigRow(row: RowList) {
        setTitle(row.title)
        with(recycleView) {
            layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
            isNestedScrollingEnabled = true
            adapter = AnimePreviewItem(activity as MyApp, AnimePreviewItem.SHOW_MENU_BUTTON, recycleView, null)
                    .setEvents(object : Events {
                        override fun onErrorLoadingImage(e: Exception?) = (activity as MyApp).showNoInternetException()
                        override fun onItemMove(fromPosition: Int, toPosition: Int) {}
                        override fun onItemDelete(position: Int) {}
                        override fun loadMore(offset: Int): Boolean = true
                        override fun onItemClick(anime: OneAnime.OneAnimeWithId, index: Int) = onItemClick(anime.anime)
                    })
                    .addItems(row.list.mapIndexed {index,anime-> OneAnime.OneAnimeWithId(index,anime) })
        }
    }
}