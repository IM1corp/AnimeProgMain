package com.imcorp.animeprog.MainActivity.fragments.home

import android.animation.Animator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.MainActivity.fragments.Adapters.smallCardItems.HorizontalAnimeCardsAdapter
import com.imcorp.animeprog.R
import com.imcorp.animeprog.Requests.JsonObj.mainPage.RowList
import com.imcorp.animeprog.databinding.ContainerElementBinding

class HomeContainerAdapter(private val activity: MyApp) : RecyclerView.Adapter<HomeContainerAdapter.OneRowItem>() {
    var data:ArrayList<RowList>? = null; set(value){
        field = value
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OneRowItem = OneRowItem(
        ContainerElementBinding.inflate(LayoutInflater.from(parent.context), parent,false)
    )
    override fun getItemCount(): Int = data?.size?:0
    override fun onBindViewHolder(holder: OneRowItem, position: Int) = holder.bind(data!![position])
    inner class OneRowItem(private val binding: ContainerElementBinding) : RecyclerView.ViewHolder(binding.root) {
        private var recyclerViewShown:Boolean=false
        private val animationDuration:Long get() = activity.resources.getInteger(R.integer.showContainerItemAnimation).toLong()
        //private val recycleViewHeight:Int get() = activity.resources.getInteger(R.integer.rowListItemHeight)
        private fun showRecycleView(rowList: RowList){
            recyclerViewShown = !recyclerViewShown
            with(binding){
                tintImageView.clearAnimation()
                tintImageView.animate()
                        .rotation(if(recyclerViewShown) -90f else 90f)
                        .setDuration(animationDuration)
                        .start()
                with(rowsRecycleView) {
                    clearAnimation()
                    if (recyclerViewShown) {
                        if(adapter==null) {
                            adapter = HorizontalAnimeCardsAdapter(activity, true).addItems(rowList.list)
                            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        }
                        visibility = View.VISIBLE
                        alpha = 0.0f
                        //TODO: animate scroll down effect
                        animate()//.translationY(height.toFloat())
                                .alpha(1.0f)
                                .setDuration(animationDuration)
                                .setListener(null)
                                .start()

                    }
                    else {
                        //(adapter as HorizontalAnimeCardsAdapter).clear()
                        animate()//.translationY(0f)
                                 .alpha(0.0f)
                                 .setListener(object :Animator.AnimatorListener{
                                    override fun onAnimationRepeat(animation: Animator) {}
                                    override fun onAnimationEnd(animation: Animator) {
                                        visibility = View.GONE
                                    }
                                    override fun onAnimationCancel(animation: Animator) {
                                        //visibility = View.GONE
                                    }
                                    override fun onAnimationStart(animation: Animator) {}

                                })
                                 .setDuration(animationDuration)
                                 .start()
                    }
                }
            }
        }
        fun bind(rowList: RowList) {
            binding.containerRow.setOnClickListener{showRecycleView(rowList)}
            binding.titleTextView.text = rowList.title.title
        }
    }
}