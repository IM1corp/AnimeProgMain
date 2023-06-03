package com.imcorp.animeprog.MainActivity.fragments.favorites

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.imcorp.animeprog.DB.local.Favorites
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.Default.Notificator
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.AnimePreviewItem
import com.imcorp.animeprog.OneAnimeActivity.OneAnimeActivity
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime.OneAnimeWithId

class FavoritesTabsPagerAdapter(fm: FragmentManager, behavior: Int, val context: FavoritesFragment) : FragmentPagerAdapter(fm, behavior) {
    val activity: OneAnimeActivity get() = context.activity as OneAnimeActivity
    override fun getItem(position: Int): Fragment = FragmentFavoritesItem(
            Favorites.FavoritesType.orderToShow[position]
    )
    override fun getCount(): Int = Favorites.FavoritesType.valuesTitles.size
    override fun getPageTitle(position: Int)= this.context.getString(
                Favorites.FavoritesType.valuesTitles[Favorites.FavoritesType.orderToShow[position]] ?: R.string.undefined)

    fun onItemClick(anime: OneAnimeWithId) = (context.activity as MainActivity).showOneAnimeWindow(anime.anime)
    fun onErrorLoadingImage() = (context.activity as MainActivity).showNoInternetException()
    fun onItemMove(fromId: Int, toId: Int) = Thread {
        (context.activity as MyApp).dataBase.favorites.moveFavoritesItems(fromId, toId)
    }.start()
    fun onItemDelete(position: Int,adapter: AnimePreviewItem,to:Favorites.FavoritesType) {

        fun addIndex(forward:Boolean) {
            activity.binding.tabLayout.getTabAt(Favorites.FavoritesType.orderToShow.indexOf(to))?.run{
                with(getOrCreateBadge()) {
                    when {
                        forward -> number++
                        number > 1 -> number--
                        else -> removeBadge()
                    }
                }
            }
        }
        addIndex(false);
        val item: OneAnimeWithId = adapter.items[position]
        with(context.activity as MyApp){
            threadCallback.post {
                notificator.showAddedAnimeToFavorites(Notificator.STATE_DELETED, {
                    // on cancel
                    addIndex(true)
                    dataBase.favorites.addToFavorites(item)
                    adapter.items.add(position, item)
                    adapter.notifyItemInserted(position)
                }){ //on dismiss
                    Thread { dataBase.favorites.deleteFromFavorites(item.anime.getPath(), item.anime.HOST.toInt()) }.start()
                }
            }
        }
    }
    fun updateBadges(tabLayout: TabLayout) = Thread{
        with(context.activity as MyApp) {
            val items = dataBase.favorites.count
            threadCallback.post {
                for ((index, item) in items.withIndex()) {
                    val tab: TabLayout.Tab = tabLayout.getTabAt(index) ?: break;
                    if (item != 0)
                        tab.getOrCreateBadge().number = item;
                    else tab.removeBadge()
                }
            }
        }
    }.start()

}