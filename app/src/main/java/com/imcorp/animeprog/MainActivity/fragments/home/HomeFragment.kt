package com.imcorp.animeprog.MainActivity.fragments.home

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.fragments.SimpleFragment
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelAbs
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelHome
import com.imcorp.animeprog.MainActivity.search.SearchFragment
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.mainPage.Container
import com.imcorp.animeprog.Requests.JsonObj.mainPage.MainPage
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList
import com.imcorp.animeprog.databinding.FragmentHomeBinding
import java.io.IOException
import java.io.InterruptedIOException
import java.io.InvalidClassException

class HomeFragment : SimpleFragment<FragmentHomeBinding>(R.layout.fragment_home, buildT = { FragmentHomeBinding.bind(it)}) {
    private val args: HomeFragmentArgs by navArgs()
    private var data: MainPage?=null; private set(v){field=v;updateData()}
    private var loadingThread:Thread? = null
    override val viewModel: SaveStateModelAbs by navGraphViewModels<SaveStateModelHome>(R.id.nav_graph)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args.urlYummyAnime?.let{
            (activity as? MainActivity)?.showOneAnimeWindow("${Config.YUMMY_ANIME_URL}/catalog/item/${it}")
        }?:args.urlAnimeGo?.let{
            (activity as? MainActivity)?.showOneAnimeWindow("${Config.ANIMEGO_URL}/anime/${it}")
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).updateFAB()
        /*with(this.recyclerViewA){
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
            adapter = HomeRecycleViewAdapter(activity as MyApp)
            (activity as MyApp).threadCallback.post{
                recycledViewPool.setMaxRecycledViews(1, 0);
            }
        }*/
        updateMenuSelectionItem(activity as MainActivity,R.id.homeFragment)
        viewBinding.retryButton.setOnClickListener{this.startLoading()}
        super.onViewCreated(view, savedInstanceState)
    }
    override fun saveInstanceState(outState: Bundle) {
        super.saveInstanceState(outState)
        with(outState) {
            putParcelable(Config.DATA_MAIN_PAGE, data)
            if (viewBinding.homeErrorTextView.visibility == View.VISIBLE)
                putCharSequence(Config.DATA_ERROR, viewBinding.homeErrorTextView.text)
        }
    }
    override fun restoreInstanceState(outState: Bundle, hostChanged: Boolean) : Unit = with(outState){
        if(!hostChanged){
            data = getParcelable(Config.DATA_MAIN_PAGE)
            if(data == null)
                startLoading()
            else showLoading(false)
        }
        else {
            startLoading()
        }
    }

    override fun initializeState(restore: Boolean) {
        if(!restore){
            startLoading()
        }
    }
    override fun searchClick() = findNavController().navigate(
            HomeFragmentDirections.actionHomeFragmentToSearchFragment(
                    SearchFragment.CameFrom.HOME_FRAGMENT.ordinal.toLong()
            ))
    override fun isSearching() = true

    override fun onDestroyView() {
        loadingThread?.interrupt()
        super.onDestroyView()
    }

    override fun onResume() {
        super.onResume()
        (context as? MainActivity)?.binding?.FAB?.visibility =View.VISIBLE
    }
    override fun onPause() {
        super.onPause()
        (context as? MainActivity)?.binding?.FAB?.visibility =View.GONE
    }
    private fun showLoading(loading:Boolean,error:CharSequence?=null) {
        (activity as? MyApp)?.runRunnableInUI {
            viewBinding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            if (error != null) {
                viewBinding.homeErrorTextView.visibility = View.VISIBLE
                viewBinding.homeErrorTextView.text = error
                viewBinding.retryButton.visibility = View.VISIBLE
            }
            else {
                viewBinding.retryButton.visibility = View.GONE
                viewBinding.homeErrorTextView.visibility = View.GONE
            }
            viewBinding.relativeLayout.visibility = if(loading)View.GONE else View.VISIBLE
        }
    }
    internal fun startLoading() {
        showLoading(true)
        loadingThread?.interrupt()
        loadingThread = Thread{
            try {
                (this.activity as? MyApp)?.run {
                    val dataObj = request.getMainPage(dataBase.settings.mainHost)//TODO:change add thread
                    (activity as? MyApp)?.threadCallback?.post {
                        data = dataObj
                        if (!isRemoving && isAdded)
                            showLoading(false)
                    }
                }
            }catch (e: InterruptedIOException){

            }catch (e:IOException){
                if(!isRemoving && isAdded)
                    (this.activity as? MyApp)?.threadCallback?.post {
                        showLoading(false, (this.activity as? MyApp)?.getString(R.string.no_internet))//TODO: show better error
                    }
                Log.e("LoadingDataException", e.message, e)
            }catch(e:Exception){
                Log.e("LoadingDataException", e.message, e)
                (this.activity as? MyApp)?.threadCallback?.post {
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
        loadingThread!!.start()

    }
    private fun updateData(){
        if(!this.isVisible)return
        viewBinding?.homeScrollView?.let {appendContainer->
            appendContainer.removeAllViews()
            data?.run {
                fun appendItem(model: HomeItemModel) = appendContainer.addView(model.itemView)
                fun getElement(item: Parcelable,bigRow:Boolean=false): HomeItemModel {
                    val thisView: View = LayoutInflater.from(context)
                            .inflate(R.layout.home_one_recycleview, appendContainer, false)
                    return HomeItemModel(thisView, activity as MainActivity).apply {
                        when (item) {
                            is RowList -> {
                                if (bigRow) bindWithBigRow(item)
                                else bindWithRowList(item)
                            }
                            is Container -> bindWithContainer(item)
                            else -> throw InvalidClassException("Invalid class found")
                        }
                    }
                }
                for(i in data){
                    val model = getElement(i)
                    appendItem(model)
                }
                bigRow?.let{ appendItem(getElement(it,bigRow=true)) }
            }
        }
    }
}