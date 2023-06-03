package com.imcorp.animeprog.Default

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.imcorp.animeprog.R

class PreferenceProgress(context: Context, attrs: AttributeSet) : Preference(context,attrs) {
    private var onBind: ((View) -> Unit)? = null

    //    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0)
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : this(context, attrs, TypedArrayUtils.getAttr(context, R.attr.preferenceStyle,
//            R.attr.preferenceStyle), 0)
//    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int):super(context, attrs, defStyleAttr,defStyleRes){
//
//    }
    init{
        layoutResource = R.layout.preference_loading_placeholder
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        onBind?.invoke(holder.itemView)
        super.onBindViewHolder(holder)
    }
    fun addOnBindListener(function: (view:View)->Unit){
        this.onBind = function
    }
}