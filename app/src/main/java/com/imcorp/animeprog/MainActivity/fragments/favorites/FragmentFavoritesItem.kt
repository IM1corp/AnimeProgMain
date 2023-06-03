package com.imcorp.animeprog.MainActivity.fragments.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.local.Favorites
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.Adapters.NothingFoundItem
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.AnimePreviewItem
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.AnimePreviewItem.*
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.Events
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.FragmentFavoritesElBinding
import java.lang.Exception

class FragmentFavoritesItem(favoritesType: Favorites.FavoritesType?=null) : Fragment(){
    private val activity:MainActivity get() = getActivity() as MainActivity
    private lateinit var favoritesType:Favorites.FavoritesType
    private val events: FavoritesTabsPagerAdapter get() = (parentFragment as FavoritesFragment).adapter
    private var nothingFoundItem:NothingFoundItem?=null
    private var _viewBinding: FragmentFavoritesElBinding? = null
    public val viewBinding get() = _viewBinding!!
    init {
        this.favoritesType = favoritesType?: Favorites.FavoritesType.values()[0]

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if(savedInstanceState!=null){
           favoritesType = Favorites.FavoritesType.values()[savedInstanceState.getByte(Config.FAV_TYPE_KEY).toInt()]
        }
        _viewBinding = FragmentFavoritesElBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(viewBinding.itemsRecycleView){
            this.setHasFixedSize(false)
            isNestedScrollingEnabled = true
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = AnimePreviewItem(
                    activity,
                    ENABLE_REORDER or SHOW_PROGRESS or SHOW_MENU_BUTTON or ENABLE_DELETING_FROM_MENU or SHOW_HOST_IMAGE_VIEW,
                    this, null
            ).setEvents(object : Events{
                override fun onErrorLoadingImage(e: Exception?) = events.onErrorLoadingImage()
                override fun onItemMove(fromPosition: Int, toPosition: Int) = events.onItemMove(fromPosition,toPosition)
                override fun onItemDelete(position: Int) {
                    events.onItemDelete(position,this@with.adapter as AnimePreviewItem,favoritesType)
                }
                override fun onItemClick(anime: OneAnime.OneAnimeWithId, index: Int) = events.onItemClick(anime)
                override fun loadMore(offset: Int): Boolean = this@FragmentFavoritesItem.loadMore(offset)
            })
        }
        this.loadMore(0)
    }
    private fun loadMore(offset: Int): Boolean{
        val dataList = (events.context.activity as MyApp).dataBase.favorites.getFavorites(Config.FAVORITES_ITEMS_LOAD_COUNT,
                offset,favoritesType)
        (activity as MyApp).threadCallback.post {
            if(offset==0&&dataList.size==0) {
                if(nothingFoundItem==null)nothingFoundItem=NothingFoundItem(activity)
                nothingFoundItem!!.setVisibly(view as ConstraintLayout,true)
            }else if(nothingFoundItem!=null)nothingFoundItem!!.setVisibly(view as ConstraintLayout,false)
            (viewBinding.itemsRecycleView.adapter as AnimePreviewItem).addItems(dataList)
        }
        return dataList.size!=0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putByte(Config.FAV_TYPE_KEY, favoritesType.ordinal.toByte())
    }
}