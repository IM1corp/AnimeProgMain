package com.imcorp.animeprog.MainActivity.fragments.downloads

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.fragments.Adapters.NothingFoundItem
import com.imcorp.animeprog.R

open class SimpleDownloadFragment(secondString:Int=R.string.down_nothing_found) : Fragment(R.layout.fragment_current_downloads)  {
    private var isinstalled:Boolean =false
    private val nothingFoundItem by lazy {isinstalled=true; NothingFoundItem(requireActivity() as MyApp,secondString = secondString) }
    protected fun showNothingFound(visible:Boolean=true){
        if(!visible && !isinstalled)return
        this.view?.let {
            nothingFoundItem.setVisibly(it as ViewGroup, visible)
        }
    }

}