package com.imcorp.animeprog.MainActivity.fragments.downloads

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.local.Favorites
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment
import com.imcorp.animeprog.MainActivity.fragments.downloads.current.CurrentDownloadingFragment
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelAbs
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelDownloads
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelProfile
import com.imcorp.animeprog.MainActivity.search.SearchFragment
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.FragmentDownloadsBinding
//import kotlinx.android.synthetic.main.activity_one_anime.*
//import kotlinx.android.synthetic.main.fragment_downloads.tabLayout
//import kotlinx.android.synthetic.main.fragment_downloads.viewPager
import java.util.ArrayList

class DownloadsFragment : SimpleFragment<FragmentDownloadsBinding>(R.layout.fragment_downloads, buildT = { FragmentDownloadsBinding.bind(it) }) {

    public lateinit var adapterFragments: ViewPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fun initFragments() {
            adapterFragments = ViewPagerAdapter(viewBinding.tabLayout,childFragmentManager)
            viewBinding.tabLayout.setupWithViewPager(viewBinding.viewPager.also { it.adapter = adapterFragments })
            viewBinding.tabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
                override fun onTabReselected(tab: TabLayout.Tab?) {}
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.run {
                        updateMenuSelectionItem(activity as MainActivity,
                                if(viewBinding.tabLayout.selectedTabPosition==1)R.id.downloadsFragmentNow else R.id.downloadsFragment)
                    }
                }
            })
        }
        initFragments()
        arguments?.run {
            if(containsKey(Config.GO_TO_FRAGMENT)){
                val itemId = getInt(Config.GO_TO_FRAGMENT)
                updateMenuSelectionItem(activity as MainActivity,itemId)
                viewBinding.tabLayout.selectTab(viewBinding.tabLayout.getTabAt(if(itemId==R.id.downloadsFragmentNow)1 else 0))
                remove(Config.GO_TO_FRAGMENT)
            }
        }
    }
    override fun isSearching()=true
    override val viewModel: SaveStateModelAbs by navGraphViewModels<SaveStateModelDownloads>(R.id.nav_graph)

    override fun searchClick() = NavHostFragment.findNavController(this)
            .navigate(DownloadsFragmentDirections.actionDownloadsFragmentToSearchFragment(SearchFragment.CameFrom.DOWNLOADS.ordinal.toLong()))
    override fun saveInstanceState(bundle: Bundle) {}
    companion object {
        fun getRecyclerView(context: Context) = LinearLayout(context).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            addView(RecyclerView(context).apply {
                id = R.id.recyclerView
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            })
        }
    }
    inner class ViewPagerAdapter(private val tabLayout: TabLayout, fm: FragmentManager?) : FragmentPagerAdapter(fm!!, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        private val titles = arrayListOf(tabLayout.context.getString(R.string.download_completed), tabLayout.context.getString(R.string.download_in_progress))
        private val badgesCount = ArrayList<Int>(2)
        override fun getItem(position: Int)=when(position){
            0-> AllDownloadsFragment()
            else -> CurrentDownloadingFragment()
        }
        override fun getCount(): Int = 2
        override fun getPageTitle(position: Int) = titles[position]
        public fun updateBadge(witch: Fragment, count:Int){
            val item: TabLayout.Tab = tabLayout.getTabAt(if(witch is AllDownloadsFragment)0 else 1)?:return
            if(count>0) item.getOrCreateBadge().number = count
            else item.removeBadge()
        }
    }
}