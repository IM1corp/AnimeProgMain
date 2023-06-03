package com.imcorp.animeprog.MainActivity.fragments.Adapters

import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.NothingFoundItemBinding

class NothingFoundItem (private val context:MyApp,
                        @StringRes private val firstString:Int=R.string.fav_nothing_found_1,
                        @StringRes private val secondString:Int=R.string.fav_nothing_found_2,
                        @DrawableRes private val aniTanImgId:Int=R.drawable.ani_tan_not_found) {
    private var view: View?=null
    //private val viewCreated:Boolean get() = this.view?.parent!=null;
    private fun attach(attachTo: ViewGroup){
        view?.let{attachTo.removeView(it)}
        with(NothingFoundItemBinding.inflate(context.layoutInflater, attachTo, false)){
            view = this.root
            attachTo.addView(view)
            aniTanImageView.setImageResource(aniTanImgId)
            textView1.setText(firstString)
            textView2.setText(secondString)

        }
//        with(context.layoutInflater.inflate(R.layout.nothing_found_item,attachTo,false)){
//
//        }
    }
    public fun setVisibly(attachTo: ViewGroup,show:Boolean = false){
        if(show)attach(attachTo)
        view?.visibility = if(show)View.VISIBLE else View.GONE
    }
}