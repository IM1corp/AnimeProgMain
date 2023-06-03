package com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.controls

import android.view.LayoutInflater
import android.view.Menu
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.imcorp.animeprog.Default.MyApp
import com.imcorp.animeprog.OneAnimeActivity.Fragments.AnimeVideo.VideoPlayerControls
import com.imcorp.animeprog.R
import com.imcorp.animeprog.databinding.BottomMenuSelectSubtitlesBinding
import com.imcorp.animeprog.databinding.OneSubtitleItemBinding

//import kotlinx.android.synthetic.main.bottom_menu_select_subtitles.*
//import kotlinx.android.synthetic.main.bottom_sheet_menu.*
//import kotlinx.android.synthetic.main.bottom_sheet_menu.bottom_menu
//import kotlinx.android.synthetic.main.one_host_item.view.radioButton
//import kotlinx.android.synthetic.main.one_subtitle_item.view.*

class SelectSubtitleBottomMenu(private val fragment: VideoPlayerControls,
                               val theme:Int= R.style.BottomSheetMenu,
                               private val onClick: (pos:Int) -> Unit) : BottomSheetDialog(fragment.requireContext(),theme) {
    val binding = BottomMenuSelectSubtitlesBinding.inflate(LayoutInflater.from(context))
    init{
//        val view = BottomMenuSelectSubtitlesBinding.inflate(LayoutInflater.from(context))
//        val view = LayoutInflater.from(context).inflate(R.layout.bottom_menu_select_subtitles, bottom_menu)
        setContentView(binding.root)
        this.dismissWithAnimation=true
    }

    override fun onPanelClosed(featureId: Int, menu: Menu) {
        super.onPanelClosed(featureId, menu)
    }
    override fun show() {
        super.show()
        BottomSheetBehavior.from(
                this.findViewById<FrameLayout>(com.google.android.material.R.id.design_bottom_sheet)!!
        ).state = BottomSheetBehavior.STATE_EXPANDED
    }
    fun setEvents(): SelectSubtitleBottomMenu = binding.run{
        subtitlesRecyclerView.apply{
            adapter = SubtitlesItemsAdapter(this)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        }
        return this@SelectSubtitleBottomMenu
    }
    inner class SubtitlesItemsAdapter(private val recyclerView: RecyclerView) : RecyclerView.Adapter<SubtitlesItemsAdapter.Item>() {


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Item(
            OneSubtitleItemBinding.inflate(layoutInflater, parent, false)
//            layoutInflater.inflate(R.layout.one_subtitle_item, parent, false)
        )
        override fun onBindViewHolder(holder: Item, position: Int) = holder.bind(position)
        override fun getItemCount() = (fragment.fragment.subtitles?.size?:0)+1
        inner class Item(protected val v: OneSubtitleItemBinding): RecyclerView.ViewHolder(v.root) {
            fun bind(position: Int) {
                if(position == 0){
                    v.titleTextViewSub.setText(R.string.no_subtitle)
                    v.subImageView.setImageResource(R.drawable.ic_no_subtitles)
                }
                else v.titleTextViewSub.text = fragment.fragment.subtitles?.get(position-1)?.title ?: ""
                if (position - 1 == fragment.subtitleIndex) v.radioButton.isChecked = true
                else addCallback(position)
            }
            fun addCallback(position: Int){
                v.radioButton.setOnCheckedChangeListener { _, _ ->
                    clicked(position)
                }
                itemView.setOnClickListener { clicked(position) }
            }
            fun clicked(position: Int){
                (recyclerView.findViewHolderForLayoutPosition(fragment.subtitleIndex+1) as? Item)?.v?.radioButton?.isChecked = false
                //recyclerView.layoutManager?.findViewByPosition(fragment.subtitleIndex+1)?.radioButton?.isChecked = false
                fragment.subtitleIndex = position - 1
                v.radioButton.isChecked=true
                (fragment.activity as MyApp).threadCallback.postDelayed(
                        { this@SelectSubtitleBottomMenu.cancel();onClick(position - 1) },
                        fragment.requireContext().resources.getInteger(R.integer.hostChoiceWaitTime).toLong()
                )
                v.radioButton.isSelected = true
            }

        }
    }
}