package com.imcorp.animeprog.MainActivity.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.imcorp.animeprog.Config
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.MainActivity
import com.imcorp.animeprog.MainActivity.savedInstanceState.SaveStateModelAbs


abstract class SimpleFragment<T: ViewBinding>(layout: Int = 0, private val rewriteBack:Boolean=true, private val buildT: (view: View)->T) : Fragment(layout) {
    open fun searchClick(){}
    open fun isSearching(): Boolean = false
    protected abstract val viewModel: SaveStateModelAbs

    val isRefreshing:Boolean get()=arguments?.run {
        getBoolean(Config.IS_REFRESH, false)
    }?:false
    private var _viewBinding: T? = null
    public val viewBinding: T get()= _viewBinding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _viewBinding = buildT(super.onCreateView(inflater, container, savedInstanceState)!!.rootView)

        return viewBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val ans = super.onViewCreated(view, savedInstanceState)

        if(isRefreshing) viewModel.data.value=null
        initializeState(
            viewModel.data.value != null
        )
        viewModel.data.value?.let{
            restoreInstanceState(it,it.getByte(Config.HOST_KEY) != activity.dataBase.settings.mainHost)
        }
        return ans
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(rewriteBack)
            requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().moveTaskToBack(true)
                }
            })

    }
    public val activity get() = super.requireActivity() as MyApp
    companion object{
        fun updateMenuSelectionItem(activity: MainActivity, id: Int) = with(activity.binding.navView){
            setCheckedItem(menu.findItem(id))
        }
    }
    override fun onDestroyView() {
        viewModel.data.value=(Bundle().also { saveInstanceState(it) })
        super.onDestroyView()
        _viewBinding = null
    }
    @CallSuper
    open fun saveInstanceState(outState: Bundle) {
        outState.putByte(Config.HOST_KEY, activity.dataBase.settings.mainHost)
    }

    /**
     * Restore instance state.
     * Called after initializeState
     * @param hostChanged - true if host been changed
     * @param outState - state
     */
    open fun restoreInstanceState(outState: Bundle, hostChanged: Boolean) {

    }

    /**
     * called BEFORE @link restoreInstanceState, inside onViewCreated
     * @param restore - true if restoreInstanceState will be called
     */
    open fun initializeState(restore: Boolean) {

    }

}
