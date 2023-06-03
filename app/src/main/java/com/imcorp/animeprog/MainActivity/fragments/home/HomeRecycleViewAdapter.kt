package com.imcorp.animeprog.MainActivity.fragments.home

import android.view.LayoutInflater
import android.view.ViewGroup
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
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList
import com.imcorp.animeprog.databinding.HomeOneRecycleviewBinding

class HomeRecycleViewAdapter(private val activity: MyApp) : RecyclerView.Adapter<HomeRecycleViewAdapter.OneItemModel>() {
    var dataItems:MainPage? = null; set(value) {
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneItemModel = OneItemModel(
        HomeOneRecycleviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
    override fun getItemCount(): Int = dataItems?.run {
        ((rows?.size?:0)+(containers?.size?:0) + (if(bigRow!=null)1 else 0))
    }?:0
    override fun onBindViewHolder(holder: OneItemModel, position: Int) {
        holder.setIsRecyclable(false)
        dataItems?.run {
            if(bigRow!=null && position == itemCount-1)
                holder.bindWithBigRow(bigRow)
            else {
                val index:Int = position / 2
                if (this.rows!=null && ((position % 2 == 0 && this.rows.size > index) ||
                        (this.containers?.size ?: 0) <= index) )
                    holder.bindWithRowList(this.rows[index])
                else if((this.containers?.size?:0) > index)
                    holder.bindWithContainer(this.containers[index])
            }
        }
    }
    override fun getItemViewType(position: Int): Int = 1
    inner class OneItemModel(itemView: HomeOneRecycleviewBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val titleView: TextView = itemView.titleTextView
        private val recycleView: RecyclerView = itemView.recyclerView
        private fun onItemClick(item: OneAnime) = (activity as MainActivity).showOneAnimeWindow(item)
        private fun setTitle(link: OneAnime.Link){
            if(!link.link.isNullOrBlank())
                Config.setTextViewLink(titleView,link.title){
                    (activity as MainActivity).loadFromLink(link)
                }
            else titleView.text = link.title
        }
        fun bindWithContainer(container: Container){
            //val g = 10;
            setTitle(container.title)
            with(recycleView){
                layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
                adapter = HomeContainerAdapter(activity)
                        .apply { data = container.list }
            }
        }
        fun bindWithRowList(row: RowList) {
            setTitle(row.title)
            with(recycleView){
                layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = HorizontalAnimeCardsAdapter(activity,true)
                        .setOnClickListener(::onItemClick)
                        .addItems(row.list)
            }
        }
        fun bindWithBigRow(row: RowList) {
            setTitle(row.title)
            with(recycleView) {
                layoutManager = LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
                isNestedScrollingEnabled = true
                adapter = AnimePreviewItem(activity, AnimePreviewItem.SHOW_MENU_BUTTON, recycleView, null)
                        .setEvents(object : Events {
                            override fun onErrorLoadingImage(e: Exception?) = activity.showNoInternetException()
                            override fun onItemMove(fromPosition: Int, toPosition: Int) {}
                            override fun onItemDelete(position: Int) {}

                            override fun loadMore(offset: Int): Boolean = true

                            override fun onItemClick(anime: OneAnime.OneAnimeWithId, index: Int) = onItemClick(anime.anime)
                        })
                        .addItems(row.list.mapIndexed {index,anime-> OneAnime.OneAnimeWithId(index,anime) })
            }
        }
    }
}
