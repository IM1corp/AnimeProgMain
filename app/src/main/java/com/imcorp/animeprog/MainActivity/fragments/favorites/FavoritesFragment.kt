package com.imcorp.animeprog.MainActivity.fragments.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import com.google.android.material.tabs.TabLayout
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.local.Favorites
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelAbs
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelDownloads
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelFavorites
import com.imcorp.animeprog.MainActivity.search.SearchFragment
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.FragmentFavoritesBinding

class FavoritesFragment : SimpleFragment<FragmentFavoritesBinding>(R.layout.fragment_favorites, buildT = { FragmentFavoritesBinding.bind(it)}) {
    override val viewModel: SaveStateModelAbs by navGraphViewModels<SaveStateModelFavorites>(R.id.nav_graph)
    lateinit var adapter: FavoritesTabsPagerAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // Inflate the layout for this fragment
        val view = super.onCreateView(inflater, container, savedInstanceState)!!
        viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager.also {
            this.adapter = FavoritesTabsPagerAdapter(childFragmentManager,FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT,this);
            it.adapter=this.adapter
        })
        return view
    }
    override fun isSearching() = true
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter.updateBadges(viewBinding.tabLayout)
        var position=0
        arguments?.run {
            if(containsKey(Config.GO_TO_FRAGMENT)){
                val itemId = getInt(Config.GO_TO_FRAGMENT)
                updateMenuSelectionItem(activity as MainActivity,itemId)
                position = Favorites.FavoritesType.idItems.indexOf(itemId)
                remove(Config.GO_TO_FRAGMENT)
            }
        }?:updateMenuSelectionItem(activity as MainActivity,R.id.favoritesFavItem)
        viewBinding.tabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.run {
                    updateMenuSelectionItem(activity as MainActivity,Favorites.FavoritesType.idItems[viewBinding.tabLayout.selectedTabPosition])
                }
            }
        })
        viewBinding.tabLayout.getTabAt(position)?.select()
    }
    override fun searchClick() = NavHostFragment.findNavController(this)
            .navigate(FavoritesFragmentDirections.actionFavoritesFragmentToSearchFragment(SearchFragment.CameFrom.FAVORITES.ordinal.toLong()))

    override fun saveInstanceState(bundle: Bundle) {}

}