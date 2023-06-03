package com.imcorp.animeprog.MainActivity.search

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.text.bold
import androidx.core.text.color
import androidx.core.text.italic
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.DB.Objects.SearchProvider
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.Adapters.NothingFoundItem
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.AnimePreviewItem
import com.imcorp.animeprog.MainActivity.fragments.Adapters.animePreviewItem.Events
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.FragmentSearchBinding
import java.lang.Exception

class SearchFragment : Fragment(),SimpleSearchAdapter.OnSuccess {
    val activity by lazy {getActivity() as MainActivity}
    private val args : SearchFragmentArgs by navArgs();

    private val searchViewArrayAdapter:ArrayAdapter<CharSequence> by lazy {
        val ad:ArrayAdapter<CharSequence> = ArrayAdapter(activity.applicationContext, android.R.layout.simple_list_item_1);
        viewBinding.listView.adapter = ad
        ad
    }
    private var searchItems:Array<String?>? = null
    lateinit var searchAdapter:SimpleSearchAdapter
    private val animePreviewItem: AnimePreviewItem get() = viewBinding.itemsRecycleView.adapter as AnimePreviewItem;
    private var lastSearchArray:ArrayList<OneAnime.OneAnimeWithId>? =null;
    private var lastCount:Int =-1;
    private var nothingFoundItem:NothingFoundItem? = null
    private var _viewBinding: FragmentSearchBinding? = null
    public val viewBinding: FragmentSearchBinding get() = _viewBinding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        searchAdapter = when(CameFrom.values()[args.cameFrom.toInt()]){
            CameFrom.HOME_FRAGMENT->MainSearch(this)
            CameFrom.DOWNLOADS->DownloadsSearch(this)
            CameFrom.FAVORITES->FavoritesSearch(this)
        }
        _viewBinding = FragmentSearchBinding.inflate(inflater, container, false)
//        return inflater.inflate(R.layout.fragment_search, container, false)

        return viewBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewBinding.listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position: Int, _ -> activity.showOneAnimeWindow(searchItems?.get(position)) }
        with(viewBinding.itemsRecycleView){
            //setHasFixedSize(false)
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            adapter = AnimePreviewItem(activity,
                    AnimePreviewItem.SHOW_PROGRESS,
                    this,
                    this@SearchFragment.view as NestedScrollView
            ).setEvents(object:Events{
                override fun onItemMove(fromPosition: Int, toPosition: Int) {}
                override fun onItemDelete(position: Int) {}

                override fun onErrorLoadingImage(e: Exception?) {/*TODO: show error*/}
                override fun loadMore(offset: Int): Boolean = false/*TODO: make better */
                override fun onItemClick(anime: OneAnime.OneAnimeWithId, index: Int) =activity.showOneAnimeWindow(anime.anime);
            })

        }
        viewLifecycleOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                if(!this.isStateSaved){
                    activity.closeSearchDialog()
                }
            }
        })
        if(savedInstanceState!=null)this.onRestoreInstanceState(savedInstanceState)
        super.onViewCreated(view, savedInstanceState)
    }

    fun onFullSearchClick(q: String) {
        SearchProvider.saveQuery(requireContext(),q)
        return searchAdapter.onBigSearch(q)
    }
    fun onLittleSearch(q:String) =  searchAdapter.onLittleSearch(q)
    fun goBack() {
        val direction = when(CameFrom.values()[args.cameFrom.toInt()]){
            CameFrom.FAVORITES->SearchFragmentDirections.actionSearchFragmentToFavoritesFragment()
            CameFrom.HOME_FRAGMENT->SearchFragmentDirections.actionSearchFragmentToHomeFragment()
            CameFrom.DOWNLOADS->SearchFragmentDirections.actionSearchFragmentToDownloadsFragment()
        }
        findNavController().navigate(direction)
        //findNavController().navigate(SearchNa)
        //requireActivity().onBackPressed()
    }
    fun setLoading(b: Boolean) {
        val visibility = if(b)View.INVISIBLE else View.INVISIBLE;
        viewBinding.listView.visibility = visibility;
    }

    override fun onSuccessLittle(data: ArrayList<OneAnime.OneAnimeWithId>, count: Int) {
        lastSearchArray=data
        lastCount=count
        viewBinding.searchTextView.text = Config.getText(context, R.string.search_find_result, count)
        if(data.size==0){
            viewBinding.searchTextView.visibility = View.INVISIBLE;
            if(nothingFoundItem==null)
                nothingFoundItem=NothingFoundItem(activity,R.string.search_nothind_found_1,
                        if(args.cameFrom==CameFrom.HOME_FRAGMENT.ordinal.toLong())R.string.search_nothind_found_2 else R.string.search_nothind_found_3,
                        R.drawable.ani_tan_searching)
            nothingFoundItem!!.setVisibly(viewBinding.constraintLayout,true)
            viewBinding.listView.visibility=View.INVISIBLE;
            viewBinding.itemsRecycleView.visibility=View.INVISIBLE;
        }
        else{
            viewBinding.searchTextView.visibility = View.VISIBLE;
            nothingFoundItem?.setVisibly(viewBinding.constraintLayout)
            if(data.firstOrNull{ it.anime.cover == null }!=null) {
                viewBinding.listView.visibility = View.VISIBLE;
                viewBinding.itemsRecycleView.visibility = View.INVISIBLE;

                val strings = arrayOfNulls<CharSequence>(data.size)
                if (searchItems?.size != data.size) searchItems = arrayOfNulls(data.size)
                for ((j,json) in data.withIndex()) {
                    strings[j] = SpannableStringBuilder().apply{
                        italic { color(resources.getColor(R.color.color_search_list_view)) {
                            append(json.anime.title)
                        }}
                        if(json.anime.year!=0)
                            append(" - ").bold { append(json.anime.year.toString()) }
                    }
                    searchItems!![j] = json.anime.getPath();
                }
                searchViewArrayAdapter.apply {
                    clear();
                    addAll(*strings);
                    notifyDataSetChanged();
                }
            }
            else {
                viewBinding.listView.visibility = View.INVISIBLE
                viewBinding.itemsRecycleView.visibility = View.VISIBLE
                animePreviewItem.apply {
                    clear()
                    addItems(data)
                }
            }
        }
    }
    override fun onSuccessBig(data: ArrayList<OneAnime.OneAnimeWithId>, count:Int) = onSuccessLittle(data,count);

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelableArrayList(Config.LAST_SEARCH,this.lastSearchArray)
        if(this::searchAdapter.isInitialized) outState.putString(Config.SEARCH_Q, searchAdapter.lastSearch)
        outState.putInt(Config.SEARCH_C,lastCount)
    }
    private fun onRestoreInstanceState(savedState:Bundle){
        lastSearchArray = savedState.getParcelableArrayList(Config.LAST_SEARCH)
        lastCount = savedState.getInt(Config.SEARCH_C);
        if(lastSearchArray!=null&&lastCount!=-1)this.onSuccessLittle(lastSearchArray!!,lastCount)
        if (this::searchAdapter.isInitialized)
            searchAdapter.lastSearch = savedState.getString(Config.SEARCH_Q)
    }

    enum class CameFrom{
        HOME_FRAGMENT,
        FAVORITES,
        DOWNLOADS
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _viewBinding = null
    }
}