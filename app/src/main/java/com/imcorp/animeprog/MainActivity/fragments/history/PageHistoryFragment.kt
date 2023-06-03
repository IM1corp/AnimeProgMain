package com.imcorp.animeprog.MainActivity.fragments.history

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.Adapters.NothingFoundItem
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelAbs
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelHistory
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.OneAnime
import com.imcorp.animeprog.databinding.MainHistoryFragmentBinding
//import kotlinx.android.synthetic.main.main_history_fragment.*
import java.util.*


class PageHistoryFragment : SimpleFragment<MainHistoryFragmentBinding>(R.layout.main_history_fragment, false, { MainHistoryFragmentBinding.bind(it)}) {
    override val viewModel by navGraphViewModels<SaveStateModelHistory>(R.id.nav_graph)
    companion object {
        const val SCROLL_UPDATE = 200
        const val COUNT_LOAD_HISTORY = 25
    }
    private val nothingFoundItem by lazy {NothingFoundItem(activity as MyApp,secondString = R.string.history_nothing_found)}
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateMenuSelectionItem(activity as MainActivity, R.id.pageHistoryFragment)
        viewBinding.removeAllItems.setOnClickListener {
            showNothingFound()
            with(activity as MainActivity) {
                notificator.showHistoryDeleted(it,
                        /*onDismiss=*/dataBase.history::deleteAllHistory) {
                    /*onCancelClick=*/
                    showNothingFound(false)
                }
            }


        }
        this.initInterface()
        addValues(true)
    }
    private fun showNothingFound(show:Boolean=true){
        viewBinding.historyView.visibility = if(show) GONE else VISIBLE
        viewBinding.recyclerView.visibility = if(show) GONE else VISIBLE

        nothingFoundItem.setVisibly(view as ViewGroup,show=show)
    }
    private fun initInterface() {
        with(this.viewBinding.recyclerView) {
            isNestedScrollingEnabled = true
            layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = HistoryRecyclerAdapter(this){ _, anime->
                (requireActivity() as MainActivity).showOneAnimeWindow(anime.value)
            }.apply {
                setOnItemDeleteListener{_, anime ->
                    (requireActivity() as MainActivity).dataBase.history.deleteHistoryRow(anime.key.time)
                    try {
                        viewBinding.numberTextView.text = (Integer.parseInt(viewBinding.numberTextView.text.toString())-1).toString()
                        if(viewBinding.numberTextView.text=="0")showNothingFound()
                    }catch (e:NumberFormatException){
                        Log.e(Config.LOG_TAG,e.message,e)
                    }
                }
//                addOnScrollListener(object : RecyclerView.OnScrollListener() {
//                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                        if(canScroll && !recyclerView.canScrollVertically(SCROLL_UPDATE)){
//                            thread?.cancel()
//                            thread = GlobalScope.launch(Dispatchers.IO){ addValues() }
//                        }
//                    }
//                })
                setOnLoadMoreItems{index -> //LoadMoreItems
                    addValues(needIndex=index)
                }
            }

        }
    }
    private fun addValues(clear:Boolean=false, needIndex:Int=-1):Unit = with(activity as MyApp){
        val count:Int? = if(clear) dataBase.history.count else null
        val items : ArrayList<MutableMap.MutableEntry<Date, OneAnime>> =
                dataBase.history.getHistory(if(needIndex==-1) COUNT_LOAD_HISTORY else needIndex-offset+COUNT_LOAD_HISTORY, offset)

            count?.let{viewBinding.numberTextView.text = it.toString() }
            with(viewBinding.recyclerView.adapter as HistoryRecyclerAdapter){
                if(clear) {
                    this.clear()
                    countOfItems = count!!
                }

                this.addItems(items)
                if(clear)notifyDataSetChanged()

                //thread = null
            }
            if(count==0) showNothingFound()
    }

    private val offset : Int get() = (viewBinding.recyclerView.adapter!! as HistoryRecyclerAdapter).items.size

}
